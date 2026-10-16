package reservas.presentation.funcionario;

import reservas.logic.Funcionario;
import reservas.proxy.BackendProxy;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioController {
    private final FuncionarioView view;
    private final FuncionarioModel model;
    private final BackendProxy proxy;

    public FuncionarioController(FuncionarioView view, FuncionarioModel model, BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            model.setFuncionarios(proxy.listarFuncionarios());
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void refrescar() {
        cargarDatos();
    }

    public void guardar(Funcionario seleccionado, String id, String nombre, String telefono) {
        if (id == null || id.isBlank() || nombre == null || nombre.isBlank() || telefono == null || telefono.isBlank()) {
            model.setError("Completá id, nombre y teléfono");
            return;
        }
        String clave = seleccionado != null ? seleccionado.getClave() : null;
        try {
            proxy.guardarFuncionario(new Funcionario(id, clave, nombre, telefono), seleccionado == null);
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }

        model.setError("");
        cargarDatos();
        view.limpiarFormulario();
    }

    public void borrar(Funcionario seleccionado) {
        if (seleccionado == null) {
            model.setError("Seleccioná un funcionario de la tabla");
            return;
        }
        try {
            proxy.borrarFuncionario(seleccionado.getId());
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }
        model.setError("");
        cargarDatos();
    }

    public void print() {
        if (model.getFuncionarios().isEmpty()) {
            model.setError("No hay funcionarios para exportar");
            return;
        }
        List<String> encabezados = List.of("Id", "Nombre", "Teléfono");
        List<List<String>> filas = new ArrayList<>();
        for (Funcionario f : model.getFuncionarios()) {
            filas.add(List.of(f.getId(), f.getNombre(), f.getTelefono()));
        }
        try {
            PdfUtil.generar("Funcionarios", encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }

    public void buscar(String texto) {
        model.setError("");
        try {
            model.setFuncionarios(proxy.buscarFuncionarios(texto));
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }
}