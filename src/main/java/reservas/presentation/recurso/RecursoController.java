package reservas.presentation.recurso;

import reservas.data.interfaces.CategoriaRecursoRepositorio;
import reservas.data.interfaces.RecursoRepositorio;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;

public class RecursoController {

    private final RecursoView view;
    private final RecursoModel model;
    private final RecursoRepositorio recursoRepositorio;
    private final CategoriaRecursoRepositorio categoriaRepositorio;

    public RecursoController(RecursoView view, RecursoModel model, RecursoRepositorio recursoRepositorio, CategoriaRecursoRepositorio categoriaRepositorio) {
        this.view = view;
        this.model = model;
        this.recursoRepositorio = recursoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        model.setCategorias(categoriaRepositorio.listarTodos());
        model.setRecursos(recursoRepositorio.listarTodos());
    }

    public void guardar(String id, String descripcion, CategoriaRecurso categoria) {
        if (id == null || id.isBlank() || descripcion == null || descripcion.isBlank() || categoria == null) {
            model.setError("Completá id, descripción y categoría");
            return;
        }

        Recurso recurso = new Recurso(id, descripcion, categoria);
        recursoRepositorio.guardar(recurso);

        model.setError("");
        cargarDatos();
        view.limpiarFormulario();
    }

    public void borrar(Recurso seleccionado) {
        if (seleccionado == null) {
            model.setError("Seleccioná un recurso de la tabla");
            return;
        }
        recursoRepositorio.borrar(seleccionado.getId());
        model.setError("");
        cargarDatos();
    }

    public void print() {
        if (model.getRecursos().isEmpty()) {
            model.setError("No hay recursos para exportar");
            return;
        }
        List<String> encabezados = List.of("Id", "Descripción", "Categoría");
        List<List<String>> filas = new ArrayList<>();
        for (Recurso r : model.getRecursos()) {
            filas.add(List.of(r.getId(), r.getDescripcion(), r.getCategoria().getDescripcion()));
        }
        try {
            PdfUtil.generar("Recursos", encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }

    public void buscarPorCategoria(CategoriaRecurso categoria) {
        List<Recurso> resultado = categoria == null
                ? recursoRepositorio.listarTodos()
                : recursoRepositorio.buscarPorCategoria(categoria.getId());
        model.setRecursos(resultado);
    }
}