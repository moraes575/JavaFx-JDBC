package gui;

import aplicacao.Programa;
import gui.util.Alerts;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import model.servicos.DepartamentoServico;
import model.servicos.VendedorServico;

public class MainViewController implements Initializable {

    @FXML
    private MenuItem menuItemVendedor;

    @FXML
    private MenuItem menuItemDepartamento;

    @FXML
    private MenuItem menuItemSobre;

    @FXML
    public void onMenuItemVendedorAction() {
        loadView("/gui/VendedorLista.fxml", (VendedorListaController controller) -> {
            controller.setVendedorServico(new VendedorServico());
            controller.updateTableView();
        });
    }

    @FXML
    public void onMenuItemDepartamentoAction() {
        loadView("/gui/DepartamentoLista.fxml", (DepartamentoListaController controller) -> {
            controller.setDepartamentoServico(new DepartamentoServico());
            controller.updateTableView();
        });
    }

    @FXML
    public void onMenuItemSobreAction() {
        loadView("/gui/Sobre.fxml", x -> {
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    private synchronized <T> void loadView(String nomeAbsoluto, Consumer<T> initalizingAction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(nomeAbsoluto));
            VBox newVbox = loader.load();

            Scene mainScene = Programa.getMainScene();
            VBox mainVBox = (VBox) ((ScrollPane) mainScene.getRoot()).getContent();

            Node mainMenu = mainVBox.getChildren().get(0);
            mainVBox.getChildren().clear();
            mainVBox.getChildren().add(mainMenu);
            mainVBox.getChildren().addAll(newVbox.getChildren());

            T controller = loader.getController();
            initalizingAction.accept(controller);
        } catch (IOException e) {
            Alerts.showAlert("IO Exception", "Erro carregando página", e.getMessage(), AlertType.ERROR);
        }

    }

}
