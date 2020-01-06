package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entidades.Departamento;
import model.entidades.Vendedor;
import model.exceptions.ValidacaoException;
import model.servicos.DepartamentoServico;
import model.servicos.VendedorServico;

public class VendedorFormularioController implements Initializable {
    
    private Vendedor entidade;
    
    private VendedorServico servico;
    
    private DepartamentoServico departamentoServico;
    
    private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
    
    @FXML
    private TextField txtID;
    
    @FXML
    private TextField txtNome;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private DatePicker dpDataNascimento;
    
    @FXML
    private TextField txtSalarioBase;
    
    @FXML
    private ComboBox<Departamento> comboBoxDepartamento;
    
    @FXML
    private Label lblErroNome;
    
    @FXML
    private Label lblErroEmail;
    
    @FXML
    private Label lblErroDataNascimento;
    
    @FXML
    private Label lblErroSalarioBase;
    
    @FXML
    private Button btnSalvar;
    
    @FXML
    private Button btnCancelar;
    
    private ObservableList<Departamento> obsLista;
    
    public void setVendedor(Vendedor entidade) {
        this.entidade = entidade;
    }
    
    public void setServicos(VendedorServico servico, DepartamentoServico departamentoServico) {
        this.servico = servico;
        this.departamentoServico = departamentoServico;
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
        Constraints.setTextFieldMaxLength(txtNome, 70);
        Constraints.setTextFieldDouble(txtSalarioBase);
        Constraints.setTextFieldMaxLength(txtEmail, 100);
        Utils.formatDatePicker(dpDataNascimento, "dd/MM/yyyy");
        
        initializeComboBoxDepartamento();
    }
    
    public void updateFormData() {
        if (entidade == null) {
            throw new IllegalStateException("Entidade está nula");
        }
        txtID.setText(String.valueOf(entidade.getId()));
        txtNome.setText(entidade.getNome());
        txtEmail.setText(entidade.getEmail());
        Locale.setDefault(Locale.US);
        txtSalarioBase.setText(String.format("%.2f", entidade.getSalarioBase()));
        if (entidade.getDataNascimento() != null) {
            dpDataNascimento.setValue(LocalDate.ofInstant(entidade.getDataNascimento().toInstant(), ZoneId.systemDefault()));
        }
        
        if (entidade.getDepartamento() == null) {
            comboBoxDepartamento.getSelectionModel().selectFirst();
        } else {
            comboBoxDepartamento.setValue(entidade.getDepartamento());
        }
    }
    
    public void loadAssociatedObjects() {
        if (departamentoServico == null) {
            throw new IllegalStateException("DepartamenttoServico está nulo");
        }
        List<Departamento> lista = departamentoServico.selectAll();
        obsLista = FXCollections.observableArrayList(lista);
        comboBoxDepartamento.setItems(obsLista);
    }
    
    private Vendedor getFormData() {
        
        Vendedor obj = new Vendedor();
        ValidacaoException excecao = new ValidacaoException("Erro de validação");
        
        obj.setId(Utils.tryParseToInt(txtID.getText()));
        
        if (txtNome.getText() == null || txtNome.getText().trim().equals("")) {
            excecao.adicionarErro("nome", "Campo não pode estar vazio");
        }
        obj.setNome(txtNome.getText());
        
        if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
            excecao.adicionarErro("email", "Campo não pode estar vazio");
        }
        obj.setEmail(txtEmail.getText());
        
        if (dpDataNascimento.getValue() == null) {
            excecao.adicionarErro("dataNascimento", "Campo não pode estar vazio");
        } else {
            Instant instant = Instant.from(dpDataNascimento.getValue().atStartOfDay(ZoneId.systemDefault()));
            obj.setDataNascimento(Date.from(instant));
        }
        
        if (txtSalarioBase.getText() == null || txtSalarioBase.getText().trim().equals("")) {
            excecao.adicionarErro("salarioBase", "Campo não pode estar vazio");
        }
        obj.setSalarioBase(Utils.tryParseToDouble(txtSalarioBase.getText()));
        
        obj.setDepartamento(comboBoxDepartamento.getValue());
        
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
        
        lblErroNome.setText(campos.contains("nome") ? erros.get("nome") : "");
        lblErroEmail.setText(campos.contains("email") ? erros.get("email") : "");
        lblErroDataNascimento.setText(campos.contains("dataNascimento") ? erros.get("dataNascimento") : "");
        lblErroSalarioBase.setText(campos.contains("salarioBase") ? erros.get("salarioBase") : "");
        
    }
    
    private void initializeComboBoxDepartamento() {
        Callback<ListView<Departamento>, ListCell<Departamento>> factory = lv -> new ListCell<Departamento>() {
            @Override
            protected void updateItem(Departamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNome());
            }
        };
        comboBoxDepartamento.setCellFactory(factory);
        comboBoxDepartamento.setButtonCell(factory.call(null));
    }
    
}
