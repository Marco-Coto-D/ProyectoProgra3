package reservas.presentation.funcionario;

import reservas.data.interfaces.FuncionarioRepositorio;
import reservas.logic.Funcionario;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioController {

    private final FuncionarioView view;
    private final FuncionarioModel model;
    private final FuncionarioRepositorio funcionarioRepositorio;

    public FuncionarioController(FuncionarioView view, FuncionarioModel model, FuncionarioRepositorio funcionarioRepositorio) {
        this.view = view;
        this.model = model;
        this.funcionarioRepositorio = funcionarioRepositorio;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        model.setFuncionarios(funcionarioRepositorio.listarTodos());
    }

    public void guardar(Funcionario seleccionado, String id, String nombre, String telefono) {
        if (id == null || id.isBlank() || nombre == null || nombre.isBlank() || telefono == null || telefono.isBlank()) {
            model.setError("Completá id, nombre y teléfono");
            return;
        }


        String clave = seleccionado != null ? seleccionado.getClave() : id;

        Funcionario funcionario = new Funcionario(id, clave, nombre, telefono);
        funcionarioRepositorio.guardar(funcionario);

        model.setError("");
        cargarDatos();
        view.limpiarFormulario();
    }

    public void borrar(Funcionario seleccionado) {
        if (seleccionado == null) {
            model.setError("Seleccioná un funcionario de la tabla");
            return;
        }
        funcionarioRepositorio.borrar(seleccionado.getId());
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
        if (texto == null || texto.isBlank()) {
            model.setFuncionarios(funcionarioRepositorio.listarTodos());
            return;
        }
        funcionarioRepositorio.buscarPorId(texto).ifPresentOrElse(
                f -> model.setFuncionarios(List.of(f)),
                () -> model.setFuncionarios(funcionarioRepositorio.buscarPorNombre(texto))
        );
    }
}