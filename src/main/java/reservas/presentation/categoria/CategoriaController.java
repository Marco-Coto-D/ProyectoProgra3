package reservas.presentation.categoria;

import reservas.data.interfaces.CategoriaRecursoRepositorio;
import reservas.data.interfaces.RecursoRepositorio;
import reservas.logic.CategoriaRecurso;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoriaController {

    private final CategoriaView view;
    private final CategoriaModel model;
    private final CategoriaRecursoRepositorio categoriaRepositorio;
    private final RecursoRepositorio recursoRepositorio;

    public CategoriaController(CategoriaView view, CategoriaModel model, CategoriaRecursoRepositorio categoriaRepositorio, RecursoRepositorio recursoRepositorio) {
        this.view = view;
        this.model = model;
        this.categoriaRepositorio = categoriaRepositorio;
        this.recursoRepositorio = recursoRepositorio;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        model.setCategorias(categoriaRepositorio.listarTodos());
    }

    public void guardar(CategoriaRecurso seleccionada, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            model.setError("Completá la descripción");
            return;
        }

        String id = seleccionada != null ? seleccionada.getId() : generarId();
        CategoriaRecurso categoria = new CategoriaRecurso(id, descripcion);
        categoriaRepositorio.guardar(categoria);

        model.setError("");
        cargarDatos();
        view.limpiarFormulario();
    }

    private String generarId() {
        Pattern patron = Pattern.compile("CAT-(\\d+)");
        int maximo = categoriaRepositorio.listarTodos().stream()
                .mapToInt(c -> {
                    Matcher m = patron.matcher(c.getId());
                    return m.matches() ? Integer.parseInt(m.group(1)) : 0;
                })
                .max()
                .orElse(0);
        return String.format("CAT-%03d", maximo + 1);
    }

    public void borrar(CategoriaRecurso seleccionada) {
        if (seleccionada == null) {
            model.setError("Seleccioná una categoría de la tabla");
            return;
        }
        if (!recursoRepositorio.buscarPorCategoria(seleccionada.getId()).isEmpty()) {
            model.setError("No se puede borrar: hay recursos asociados a esta categoría");
            return;
        }
        categoriaRepositorio.borrar(seleccionada.getId());
        model.setError("");
        cargarDatos();
    }

    public void print() {
        if (model.getCategorias().isEmpty()) {
            model.setError("No hay categorías para exportar");
            return;
        }
        List<String> encabezados = List.of("Id", "Descripción");
        List<List<String>> filas = new ArrayList<>();
        for (CategoriaRecurso c : model.getCategorias()) {
            filas.add(List.of(c.getId(), c.getDescripcion()));
        }
        try {
            PdfUtil.generar("Categorías de Recurso", encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }

    public void buscar(String texto) {
        model.setError("");
        String textoLimpio = texto == null ? "" : texto.trim();
        if (textoLimpio.isBlank()) {
            model.setCategorias(categoriaRepositorio.listarTodos());
            return;
        }
        categoriaRepositorio.buscarPorId(textoLimpio).ifPresentOrElse(
                c -> model.setCategorias(List.of(c)),
                () -> model.setCategorias(categoriaRepositorio.buscarPorDescripcion(textoLimpio))
        );
    }
}