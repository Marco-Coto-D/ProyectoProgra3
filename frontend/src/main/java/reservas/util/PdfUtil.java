package reservas.util;

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
        File dir = new File("reportes");
        dir.mkdirs();

        String nombre = titulo.replaceAll("[^a-zA-Z0-9-]", "_")
                + "_" + LocalDateTime.now().format(SELLO) + ".pdf";
        File archivo = new File(dir, nombre);

        Document documento = new Document(new PdfDocument(new PdfWriter(archivo)));
        documento.add(new Paragraph(titulo).setBold().setFontSize(14));

        Table tabla = new Table(encabezados.size());
        for (String enc : encabezados) {
            tabla.addHeaderCell(new Cell().add(new Paragraph(enc).setBold()));
        }
        for (List<String> fila : filas) {
            for (String celda : fila) {
                tabla.addCell(new Cell().add(new Paragraph(celda != null ? celda : "")));
            }
        }
        documento.add(tabla);
        documento.close();

        Desktop.getDesktop().open(archivo);
    }
}