package reservas.presentation.recurso;

import reservas.data.interfaces.CategoriaRecursoRepositorio;
import reservas.data.interfaces.RecursoRepositorio;
import reservas.data.interfaces.ReservaRepositorio;
import reservas.logic.CategoriaRecurso;
import reservas.logic.DependenciaUtil;
import reservas.logic.Recurso;
import reservas.logic.ValidadorId;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecursoController {

    private final RecursoView view;
    private final RecursoModel model;
    private final RecursoRepositorio recursoRepositorio;
    private final CategoriaRecursoRepositorio categoriaRepositorio;
    private final ReservaRepositorio reservaRepositorio;

    public RecursoController(RecursoView view, RecursoModel model, RecursoRepositorio recursoRepositorio, CategoriaRecursoRepositorio categoriaRepositorio, ReservaRepositorio reservaRepositorio) {
        this.view = view;
        this.model = model;
        this.recursoRepositorio = recursoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.reservaRepositorio = reservaRepositorio;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        model.setCategorias(categoriaRepositorio.listarTodos());
        model.setRecursos(recursoRepositorio.listarTodos());
    }

    public void refrescar() {
        model.setCategorias(categoriaRepositorio.listarTodos());
    }

    public void guardar(Recurso seleccionado, String id, String descripcion, CategoriaRecurso categoria) {
        if (id == null || id.isBlank() || descripcion == null || descripcion.isBlank() || categoria == null) {
            model.setError("Completá id, descripción y categoría");
            return;
        }
        Optional<String> errorId = ValidadorId.validarIdParaCrear(
                seleccionado == null, recursoRepositorio.buscarPorId(id).isPresent(), id);
        if (errorId.isPresent()) {
            model.setError(errorId.get());
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
        if (DependenciaUtil.recursoTieneReservas(reservaRepositorio.listarTodas(), seleccionado.getId())) {
            model.setError("No se puede borrar: hay reservas asociadas a este recurso");
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
        model.setError("");
        List<Recurso> resultado = categoria == null
                ? recursoRepositorio.listarTodos()
                : recursoRepositorio.buscarPorCategoria(categoria.getId());
        model.setRecursos(resultado);
    }

    public void buscar(String texto) {
        model.setError("");
        String textoLimpio = texto == null ? "" : texto.trim();
        if (textoLimpio.isBlank()) {
            model.setRecursos(recursoRepositorio.listarTodos());
            return;
        }
        recursoRepositorio.buscarPorId(textoLimpio).ifPresentOrElse(
                r -> model.setRecursos(List.of(r)),
                () -> model.setRecursos(recursoRepositorio.buscarPorTexto(textoLimpio))
        );
    }
}