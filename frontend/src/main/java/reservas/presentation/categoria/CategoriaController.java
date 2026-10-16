package reservas.presentation.categoria;

import reservas.logic.CategoriaRecurso;
import reservas.proxy.BackendProxy;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;

public class CategoriaController {
    private final CategoriaView view;
    private final CategoriaModel model;
    private final BackendProxy proxy;

    public CategoriaController(CategoriaView view, CategoriaModel model, BackendProxy proxy) {
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
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void guardar(CategoriaRecurso seleccionada, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            model.setError("Completá la descripción");
            return;
        }

        String id = seleccionada != null ? seleccionada.getId() : null;
        try {
            proxy.guardarCategoria(new CategoriaRecurso(id, descripcion));
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }

        model.setError("");
        cargarDatos();
        view.limpiarFormulario();
    }

    public void borrar(CategoriaRecurso seleccionada) {
        if (seleccionada == null) {
            model.setError("Seleccioná una categoría de la tabla");
            return;
        }
        try {
            proxy.borrarCategoria(seleccionada.getId());
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }
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
        try {
            model.setCategorias(proxy.buscarCategorias(texto));
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }
}