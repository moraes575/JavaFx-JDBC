package gui;

import aplicacao.Programa;
import db.DbException;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entidades.Vendedor;
import model.servicos.VendedorServico;

public class VendedorListaController implements Initializable, DataChangeListener {

    private VendedorServico servico;

    @FXML
    private TableView<Vendedor> tableViewVendedor;

    @FXML
    private TableColumn<Vendedor, Integer> tableColumnID;

    @FXML
    private TableColumn<Vendedor, String> tableColumnNome;

    @FXML
    private TableColumn<Vendedor, String> tableColumnEmail;

    @FXML
    private TableColumn<Vendedor, Date> tableColumnDataNascimento;

    @FXML
    private TableColumn<Vendedor, Double> tableColumnSalarioBase;

    @FXML
    private TableColumn<Vendedor, Vendedor> tableColumnEditar;

    @FXML
    private TableColumn<Vendedor, Vendedor> tableColumnRemover;

    @FXML
    private Button btnNovo;

    private ObservableList<Vendedor> obsLista;

    @FXML
    public void onBtnNovoAction(ActionEvent event) {
        Stage parentStage = Utils.currentStage(event);
        Vendedor obj = new Vendedor();
        createDialogForm(obj, parentStage, "/gui/VendedorFormulario.fxml");
    }

    public void setVendedorServico(VendedorServico servico) {
        this.servico = servico;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeNodes();

    }

    private void initializeNodes() {
        tableColumnID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnDataNascimento.setCellValueFactory(new PropertyValueFactory<>("dataNascimento"));
        Utils.formatTableColumnDate(tableColumnDataNascimento, "dd/MM/yyyy");
        tableColumnSalarioBase.setCellValueFactory(new PropertyValueFactory<>("salarioBase"));
        Utils.formatTableColumnDouble(tableColumnSalarioBase, 2);

        Stage stage = (Stage) Programa.getMainScene().getWindow();
        tableViewVendedor.prefHeightProperty().bind(stage.heightProperty());
    }

    public void updateTableView() {
        if (servico == null) {
            throw new IllegalStateException("Serviço estava nulo");
        }

        List<Vendedor> lista = servico.selectAll();
        obsLista = FXCollections.observableArrayList(lista);
        tableViewVendedor.setItems(obsLista);
        initEditButtons();
        initRemoveButtons();
    }

    private void createDialogForm(Vendedor obj, Stage parentStage, String nomeAbsoluto) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(nomeAbsoluto));
            Pane pane = loader.load();

            VendedorFormularioController controller = loader.getController();
            controller.setVendedor(obj);
            controller.setVendedorServico(new VendedorServico());
            controller.subscribeDataChangeListener(this);
            controller.updateFormData();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Digite os dados do vendedor");
            dialogStage.setScene(new Scene(pane));
            dialogStage.setResizable(false);
            dialogStage.initOwner(parentStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();

        } catch (IOException e) {
            Alerts.showAlert("IO Exception", "Erro ao carregar a tela", e.getMessage(), AlertType.ERROR);
        }
    }

    @Override
    public void onDataChanged() {
        updateTableView();
    }

    private void initEditButtons() {

        tableColumnEditar.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnEditar.setCellFactory(param -> new TableCell<Vendedor, Vendedor>() {
            private final Button button = new Button("Editar");

            @Override
            protected void updateItem(Vendedor obj, boolean empty) {
                super.updateItem(obj, empty);

                if (obj == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(button);
                button.setOnAction(
                        event -> createDialogForm(
                                obj, Utils.currentStage(event), "/gui/VendedorFormulario.fxml"));
            }
        });
    }

    private void initRemoveButtons() {
        tableColumnRemover.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnRemover.setCellFactory(param -> new TableCell<Vendedor, Vendedor>() {
            private final Button button = new Button("Remover");

            @Override
            protected void updateItem(Vendedor obj, boolean empty) {
                super.updateItem(obj, empty);
                if (obj == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(button);
                button.setOnAction(event -> removerEntidade(obj));
            }
        });
    }

    private void removerEntidade(Vendedor obj) {
        Optional<ButtonType> resultado = Alerts.showConfirmation("Confirmação", "Tem certeza que deseja apagar?");

        if (resultado.get() == ButtonType.OK) {
            if (servico == null) {
                throw new IllegalStateException("Serviço está nulo");
            }
            try {
                servico.delete(obj);
                updateTableView();
            } catch (DbIntegrityException | DbException e) {
                Alerts.showAlert("Erro ao remover", null, e.getMessage(), AlertType.ERROR);
            }

        }
    }

}
