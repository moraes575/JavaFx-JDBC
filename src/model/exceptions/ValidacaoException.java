package model.exceptions;

import java.util.HashMap;
import java.util.Map;

public class ValidacaoException extends RuntimeException {

    private Map<String, String> erros = new HashMap<>();

    public ValidacaoException(String msg) {
        super(msg);
    }

    public Map<String, String> getErros() {
        return erros;
    }

    public void adicionarErro(String nomeCampo, String msgErro) {
        erros.put(nomeCampo, msgErro);
    }

}
