package reservas.util;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfUtil {

    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void generar(String titulo, List<String> encabezados, List<List<String>> filas) throws Exception {
        generar(titulo, encabezados, filas, null);
    }

    /** igual, pero con un gráfico (PNG) debajo de la tabla */
    public static void generar(String titulo, List<String> encabezados, List<List<String>> filas, byte[] grafico) throws Exception {
        File dir = new File("reportes");
        dir.mkdirs();

        String nombre = titulo.replaceAll("[^a-zA-Z0-9-]", "_")
                + "_" + LocalDateTime.now().format(SELLO) + ".pdf";
        File archivo = new File(dir, nombre);

        Document documento = new Document(new PdfDocument(new PdfWriter(archivo)));
        documento.add(new Paragraph(titulo).setBold().setFontSize(14));

        // todo el ancho de la hoja, y sin cortar palabras en el guion: si no, "2026-11-04" sale en dos renglones
        Table tabla = new Table(encabezados.size()).useAllAvailableWidth();
        for (String enc : encabezados) {
            tabla.addHeaderCell(new Cell().add(celda(enc).setBold()));
        }
        for (List<String> fila : filas) {
            for (String celda : fila) {
                tabla.addCell(new Cell().add(celda(celda != null ? celda : "")));
            }
        }
        documento.add(tabla);
        if (grafico != null) {
            Image imagen = new Image(ImageDataFactory.create(grafico));
            imagen.scaleToFit(480, 320);
            imagen.setMarginTop(15);
            documento.add(imagen);
        }
        documento.close();

        Desktop.getDesktop().open(archivo);
    }

    // solo se corta de renglón en los espacios (iText también corta en los guiones)
    private static Paragraph celda(String texto) {
        return new Paragraph(texto).setSplitCharacters((linea, i) ->
                linea.get(i).hasValidUnicode() && Character.isWhitespace(linea.get(i).getUnicode()));
    }
}