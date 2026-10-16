package reservas.presentation.login;

import reservas.presentation.AbstractModel;

public class CambioClaveLoginModel extends AbstractModel {

    public static final String ERROR = "error";

    private String error = "";

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        firePropertyChange(ERROR);
    }
}
