package reservas.presentation.funcionario;

import reservas.data.interfaces.AdministradorRepositorio;
import reservas.data.interfaces.FuncionarioRepositorio;
import reservas.data.interfaces.ReservaRepositorio;
import reservas.logic.Funcionario;
import reservas.logic.ValidadorId;
import reservas.util.PdfUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FuncionarioController {

    private final FuncionarioView view;
    private final FuncionarioModel model;
    private final FuncionarioRepositorio funcionarioRepositorio;
    private final AdministradorRepositorio administradorRepositorio;
    private final ReservaRepositorio reservaRepositorio;

    public FuncionarioController(FuncionarioView view, FuncionarioModel model, FuncionarioRepositorio funcionarioRepositorio, AdministradorRepositorio administradorRepositorio, ReservaRepositorio reservaRepositorio) {
        this.view = view;
        this.model = model;
        this.funcionarioRepositorio = funcionarioRepositorio;
        this.administradorRepositorio = administradorRepositorio;
        this.reservaRepositorio = reservaRepositorio;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        model.setFuncionarios(funcionarioRepositorio.listarTodos());
    }

    public void refrescar() {
        cargarDatos();
    }

    public void guardar(Funcionario seleccionado, String id, String nombre, String telefono) {
        if (id == null || id.isBlank() || nombre == null || nombre.isBlank() || telefono == null || telefono.isBlank()) {
            model.setError("Completá id, nombre y teléfono");
            return;
        }
        boolean idYaExiste = funcionarioRepositorio.buscarPorId(id).isPresent()
                || administradorRepositorio.buscarPorId(id).isPresent();
        Optional<String> errorId = ValidadorId.validarIdParaCrear(
                seleccionado == null, idYaExiste, id);
        if (errorId.isPresent()) {
            model.setError(errorId.get());
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
        if (!reservaRepositorio.listarPorFuncionario(seleccionado.getId()).isEmpty()) {
            model.setError("No se puede borrar: el funcionario tiene reservas asociadas");
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
        model.setError("");
        String textoLimpio = texto == null ? "" : texto.trim();
        if (textoLimpio.isBlank()) {
            model.setFuncionarios(funcionarioRepositorio.listarTodos());
            return;
        }
        funcionarioRepositorio.buscarPorId(textoLimpio).ifPresentOrElse(
                f -> model.setFuncionarios(List.of(f)),
                () -> model.setFuncionarios(funcionarioRepositorio.buscarPorNombre(textoLimpio))
        );
    }
}