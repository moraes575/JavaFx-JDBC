package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entidades.Vendedor;
import model.exceptions.ValidacaoException;
import model.servicos.VendedorServico;

public class VendedorFormularioController implements Initializable {

    private Vendedor entidade;

    private VendedorServico servico;

    private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtNome;

    @FXML
    private Label lblErro;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnCancelar;

    public void setVendedor(Vendedor entidade) {
        this.entidade = entidade;
    }

    public void setVendedorServico(VendedorServico servico) {
        this.servico = servico;
    }

    public void subscribeDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.add(listener);
    }

    @FXML
    public void onBtnSalvarAction(ActionEvent event) {
        if (entidade == null) {
            throw new IllegalStateException("Entidade está nula");
        }
        if (servico == null) {
            throw new IllegalStateException("Serviço está nulo");
        }
        try {
            entidade = getFormData();
            servico.insertOrUpdate(entidade);
            notifyDataChangeListeners();
            Utils.currentStage(event).close();

        } catch (ValidacaoException e) {
            setErroMensagens(e.getErros());

        } catch (DbException e) {
            Alerts.showAlert("Erro ao salvar objeto", null, e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    public void onBtnCancelarAction(ActionEvent event) {
        Utils.currentStage(event).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeNodes();
    }

    private void initializeNodes() {
        Constraints.setTextFieldInteger(txtID);
        Constraints.setTextFieldMaxLength(txtNome, 30);
    }

    public void updateFormData() {
        if (entidade == null) {
            throw new IllegalStateException("Entidade está nula");
        }
        txtID.setText(String.valueOf(entidade.getId()));
        txtNome.setText(entidade.getNome());
    }

    private Vendedor getFormData() {

        Vendedor obj = new Vendedor();
        ValidacaoException excecao = new ValidacaoException("Erro de validação");

        obj.setId(Utils.tryParseToInt(txtID.getText()));

        if (txtNome.getText() == null || txtNome.getText().trim().equals("")) {
            excecao.adicionarErro("nome", "Campo não pode estar vazio");
        }
        obj.setNome(txtNome.getText());

        if (excecao.getErros().size() > 0) {
            throw excecao;
        }

        return obj;

    }

    private void notifyDataChangeListeners() {

        for (DataChangeListener listener : dataChangeListeners) {
            listener.onDataChanged();
        }

    }

    private void setErroMensagens(Map<String, String> erros) {

        Set<String> campos = erros.keySet();

        if (campos.contains("nome")) {
            lblErro.setText(erros.get("nome"));
        }

    }

}
