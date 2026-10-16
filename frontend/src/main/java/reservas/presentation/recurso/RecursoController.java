package reservas.presentation.recurso;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.proxy.BackendProxy;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;

public class RecursoController {
    private final RecursoView view;
    private final RecursoModel model;
    private final BackendProxy proxy;

    public RecursoController(RecursoView view, RecursoModel model, BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            model.setCategorias(proxy.listarCategorias());
            model.setRecursos(proxy.listarRecursos());
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void refrescar() {
        try {
            model.setCategorias(proxy.listarCategorias());
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void guardar(Recurso seleccionado, String id, String descripcion, CategoriaRecurso categoria) {
        if (id == null || id.isBlank() || descripcion == null || descripcion.isBlank() || categoria == null) {
            model.setError("Completá id, descripción y categoría");
            return;
        }
        try {
            proxy.guardarRecurso(new Recurso(id, descripcion, categoria), seleccionado == null);
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }

        model.setError("");
        cargarDatos();
        view.limpiarFormulario();
    }

    public void borrar(Recurso seleccionado) {
        if (seleccionado == null) {
            model.setError("Seleccioná un recurso de la tabla");
            return;
        }
        try {
            proxy.borrarRecurso(seleccionado.getId());
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }
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
        model.setError("");
        try {
            model.setRecursos(proxy.buscarRecursosPorCategoria(categoria == null ? null : categoria.getId()));
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void buscar(String texto) {
        model.setError("");
        try {
            model.setRecursos(proxy.buscarRecursos(texto));
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }
}