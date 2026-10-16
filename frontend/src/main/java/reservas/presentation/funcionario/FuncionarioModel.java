package reservas.presentation.funcionario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reservas.logic.Funcionario;
import reservas.presentation.AbstractModel;

import java.util.List;

public class FuncionarioModel extends AbstractModel {

    public static final String ERROR = "error";

    private final ObservableList<Funcionario> funcionarios = FXCollections.observableArrayList();
    private String error = "";

    public ObservableList<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> lista) {
        funcionarios.setAll(lista);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        firePropertyChange(ERROR);
    }
}