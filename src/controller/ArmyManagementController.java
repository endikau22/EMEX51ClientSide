/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import businessLogic.ArmyFactory;
import businessLogic.ArmyInterface;
import businessLogic.BusinessLogicException;
import businessLogic.SectorFactory;
import businessLogic.SectorInterface;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import model.Army;
import model.Employee;
import model.Sector;
import model.User;

/**
 *
 * @author xabig
 */
public class ArmyManagementController {

    /**
     * Logger para trazar los pasos del código.
     */
    private static final Logger LOGGER = Logger.getLogger("grupog5.signinsignupapplication.cliente.controlador.FXMLDocumentControllerSignIn");

    /**
     * Una ventana sobre la que se coloca una escena.
     */
    private Stage stage;

    User user;

    ArmyInterface armyInt = ArmyFactory.getArmyImp();

    SectorInterface sectorInt = SectorFactory.getSector();

    ObservableList armys;

    Sector sector;

    @FXML
    private TextField textfieldBuscar;

    @FXML
    private Button buttonBuscar;

    @FXML
    private ComboBox comboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TableView tableView;

    @FXML
    private TableColumn<Army, String> tableColumnNombre;

    @FXML
    private TableColumn<Army, Integer> tableColumnMunicion;

    @FXML
    private TableColumn<Army, Date> tableColumnFechaLlegada;

    @FXML
    private TableColumn<Sector, String> tableColumnSector;

    @FXML
    private TextField textfieldNombre;

    @FXML
    private TextField textfieldMunicion;

    @FXML
    private Button buttonAniadir;

    @FXML
    private Button buttonBorrar;

    @FXML
    private DatePicker datePickerAniadir;

    @FXML
    private Label labelError;

    @FXML
    private Button buttonVolver;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public void initStage(Parent root) {
        try {
            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.setTitle("Army Management");
            stage.setResizable(false);

            buttonAniadir.setDisable(true);
            buttonBorrar.setDisable(true);
            buttonBuscar.setDisable(true);
            datePicker.setEditable(false);
            datePickerAniadir.setEditable(false);

            ObservableList<String> cbOptions = FXCollections.observableArrayList();
            cbOptions.addAll("Nombre", "Municion", "Todos");
            comboBox.setItems(cbOptions);

            armys = FXCollections.observableArrayList(armyInt.getArmysBySector(sector));
            if (armys != null) {
                loadArmysTable(armys);
            }
            makeTableEditable();

            stage.show();

            textfieldMunicion.textProperty().addListener(this::municionListener);
            textfieldNombre.textProperty().addListener(this::nombreListener);
            buttonBuscar.setOnAction(this::clickBuscar);
            buttonAniadir.setOnAction(this::clickAniadir);
            buttonBorrar.setOnAction(this::clickBorrar);
            tableView.getSelectionModel().selectedItemProperty().addListener(this::clickTabla);
        } catch (BusinessLogicException ex) {
            Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void makeTableEditable() {
        tableColumnNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        tableColumnNombre.setOnEditCommit((CellEditEvent<Army, String> t) -> {
            try {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue());
                Army army = t.getRowValue();
                armyInt.editArmy(army);
            } catch (BusinessLogicException ex) {
                Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.info("Error al updatear en la lambda del nombre edit");
            }
        });
        tableColumnMunicion.setCellFactory(TextFieldTableCell.<Army, Integer>forTableColumn(new IntegerStringConverter()));
        tableColumnMunicion.setOnEditCommit((CellEditEvent<Army, Integer> t) -> {
            try {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setAmmunition(t.getNewValue());
                Army army = t.getRowValue();
                armyInt.editArmy(army);
            } catch (BusinessLogicException ex) {
                Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.info("Error al updatear en la lambda del nombre edit");
            }
        });

        tableView.setEditable(true);
    }

    private void loadArmysTable(ObservableList<Army> armys) {
        tableColumnNombre.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnMunicion.setCellValueFactory(new PropertyValueFactory<>("ammunition"));
        tableColumnFechaLlegada.setCellValueFactory(new PropertyValueFactory<>("arrivalDate"));
        tableColumnSector.setCellValueFactory(new PropertyValueFactory<>("sector"));

        tableView.setItems(armys);
    }

    private void clickBuscar(ActionEvent event) {
        try {
            if (comboBox.getValue().equals("Todos")) {
                armys = FXCollections.observableArrayList(armyInt.getAllArmys());
            } else if (comboBox.getValue().equals("Nombre")) {
                armys = FXCollections.observableArrayList(armyInt.getArmysByName(textfieldBuscar.getText().trim()));
            } else {
                armys = FXCollections.observableArrayList(armyInt.getArmysByAmmunition(Integer.parseInt(textfieldBuscar.getText().trim())));
            }
            tableView.setItems(armys);
        } catch (BusinessLogicException ex) {
            Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
            labelError.setText("Error when trying to Find Armys, try again later");
            labelError.setTextFill(Color.web("#ff0000"));
        }
    }

    private void clickAniadir(ActionEvent event) {
        if (datePickerAniadir.getValue().toString().compareToIgnoreCase("") != 0) {
            Boolean confirmar = mostrarAlertConfirmation("Añadir", "Estas seguro que quieres añadir?");
            if (confirmar) {
                try {
                    Army army = new Army();
                    army.setSector(sector);
                    army.setName(textfieldNombre.getText().trim());
                    army.setAmmunition(Integer.parseInt(textfieldMunicion.getText().trim()));
                    army.setArrivalDate(Date.from(datePickerAniadir.getValue().atStartOfDay().toInstant(ZoneOffset.UTC)));

                    armyInt.createArmy(army);

                    //Actualizar lista
                    textfieldNombre.setText("");
                    textfieldMunicion.setText("");
                    datePickerAniadir.getEditor().clear();

                    armys = FXCollections.observableArrayList(armyInt.getArmysBySector(sector));
                    tableView.setItems(armys);
                } catch (BusinessLogicException ex) {
                    Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
                    labelError.setText("Error when trying to Create an Army, try again later");
                    labelError.setTextFill(Color.web("#ff0000"));
                }
            }
        } else {
            mostrarAlertConfirmation("Espabila", "Introduce fecha");
        }
    }

    private void clickBorrar(ActionEvent event) {
        if (mostrarAlertConfirmation("Delete", "Are you sure you want to delete?")) {
            try {
                Army selectedArmy = ((Army) tableView.getSelectionModel()
                        .getSelectedItem());
                armyInt.deleteArmy(selectedArmy.getId().toString());

                armys = FXCollections.observableArrayList(armyInt.getArmysBySector(sector));
                tableView.setItems(armys);

                buttonBorrar.setDisable(true);
            } catch (BusinessLogicException ex) {
                Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
                labelError.setText("Error when trying to Delete an Army, try again later");
                labelError.setTextFill(Color.web("#ff0000"));
            }
        }
    }

    private void clickTabla(ObservableValue observable, Object oldValue, Object newValue) {
        if (newValue != null) {
            buttonBorrar.setDisable(false);
        } else {
            buttonBorrar.setDisable(true);
        }
    }

    @FXML
    private void cbListener(ActionEvent event) {
        buttonBuscar.setDisable(false);
        if (comboBox.getValue().equals("Todos")) {
            textfieldBuscar.setText("");
            textfieldBuscar.setDisable(true);
        } else {
            textfieldBuscar.setDisable(false);
        }
    }

    @FXML
    private void changeDatePicker(ActionEvent event) {
        try {
            armys = FXCollections.observableArrayList(armyInt.getArmysByDate(Date.from(datePicker.getValue().atStartOfDay().toInstant(ZoneOffset.UTC))));
            tableView.setItems(armys);
        } catch (BusinessLogicException ex) {
            Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void municionListener(ObservableValue observable, String oldValue, String newValue) {
        if (textfieldMunicion.getText().compareToIgnoreCase("") != 0
                && textfieldNombre.getText().compareToIgnoreCase("") != 0) {
            buttonAniadir.setDisable(false);
        } else {
            buttonAniadir.setDisable(true);
        }
    }

    private void nombreListener(ObservableValue observable, String oldValue, String newValue) {
        if (textfieldMunicion.getText().compareToIgnoreCase("") != 0
                && textfieldNombre.getText().compareToIgnoreCase("") != 0) {
            buttonAniadir.setDisable(false);
        } else {
            buttonAniadir.setDisable(true);
        }
    }

    /**
     * Metodo para volver a la ventana Sectors.
     *
     * @param event el evento
     */
    @FXML
    private void clickVolver(ActionEvent event) {
        try {
            LOGGER.log(Level.INFO, "Método clickVolver de la clase ArmyManagementController");
            FXMLLoader loader = new FXMLLoader(getClass().
                    getResource("/view/FXMLSectorManagement.fxml"));
            Parent root = (Parent) loader.load();
            SectorManagementController sectorManagementController = (SectorManagementController) loader.getController();
            sectorManagementController.setStage(stage);
            sectorManagementController.initStage(root);
        } catch (IOException ex) {
            Logger.getLogger(ArmyManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Ventana de alerta que sale para confirmar o negar el cambio de pantalla
     *
     * @param event Contiene la accion realizada con el boton.
     * @return Devuelve el resultado de la eleccion.
     */
    private boolean mostrarAlertConfirmation(String title, String contextText) {
        boolean confirm = false;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(contextText);
        Optional<ButtonType> resp = alert.showAndWait();

        if (resp.get() == ButtonType.OK) {
            confirm = true;
        }

        return confirm;
    }
}
