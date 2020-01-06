package gui;

import aplicacao.Programa;
import db.DbException;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import java.io.IOException;
import java.net.URL;
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
import model.entidades.Departamento;
import model.servicos.DepartamentoServico;

public class DepartamentoListaController implements Initializable, DataChangeListener {

    private DepartamentoServico servico;

    @FXML
    private TableView<Departamento> tableViewDepartamento;

    @FXML
    private TableColumn<Departamento, Integer> tableColumnID;

    @FXML
    private TableColumn<Departamento, String> tableColumnNome;

    @FXML
    private TableColumn<Departamento, Departamento> tableColumnEditar;

    @FXML
    private TableColumn<Departamento, Departamento> tableColumnRemover;

    @FXML
    private Button btnNovo;

    private ObservableList<Departamento> obsLista;

    @FXML
    public void onBtnNovoAction(ActionEvent event) {
        Stage parentStage = Utils.currentStage(event);
        Departamento obj = new Departamento();
        createDialogForm(obj, parentStage, "/gui/DepartamentoFormulario.fxml");
    }

    public void setDepartamentoServico(DepartamentoServico servico) {
        this.servico = servico;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeNodes();

    }

    private void initializeNodes() {
        tableColumnID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        Stage stage = (Stage) Programa.getMainScene().getWindow();
        tableViewDepartamento.prefHeightProperty().bind(stage.heightProperty());
    }

    public void updateTableView() {
        if (servico == null) {
            throw new IllegalStateException("Serviço estava nulo");
        }

        List<Departamento> lista = servico.selectAll();
        obsLista = FXCollections.observableArrayList(lista);
        tableViewDepartamento.setItems(obsLista);
        initEditButtons();
        initRemoveButtons();
    }

    private void createDialogForm(Departamento obj, Stage parentStage, String nomeAbsoluto) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(nomeAbsoluto));
            Pane pane = loader.load();

            DepartamentoFormularioController controller = loader.getController();
            controller.setDepartamento(obj);
            controller.setDepartamentoServico(new DepartamentoServico());
            controller.subscribeDataChangeListener(this);
            controller.updateFormData();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Digite os dados do departamento");
            dialogStage.setScene(new Scene(pane));
            dialogStage.setResizable(false);
            dialogStage.initOwner(parentStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alerts.showAlert("IO Exception", "Erro ao carregar a tela", e.getMessage(), AlertType.ERROR);
        }
    }

    @Override
    public void onDataChanged() {
        updateTableView();
    }

    private void initEditButtons() {

        tableColumnEditar.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnEditar.setCellFactory(param -> new TableCell<Departamento, Departamento>() {
            private final Button button = new Button("Editar");

            @Override
            protected void updateItem(Departamento obj, boolean empty) {
                super.updateItem(obj, empty);

                if (obj == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(button);
                button.setOnAction(
                        event -> createDialogForm(
                                obj, Utils.currentStage(event), "/gui/DepartamentoFormulario.fxml"));
            }
        });
    }

    private void initRemoveButtons() {
        tableColumnRemover.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnRemover.setCellFactory(param -> new TableCell<Departamento, Departamento>() {
            private final Button button = new Button("Remover");

            @Override
            protected void updateItem(Departamento obj, boolean empty) {
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

    private void removerEntidade(Departamento obj) {
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
