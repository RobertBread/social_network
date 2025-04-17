package com.example.guiex1.controller;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.services.UtilizatorService;
import com.example.guiex1.utils.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

import static java.awt.SystemColor.text;

public class EmailController{
    UtilizatorService service;
    Utilizator user;
    ObservableList<Utilizator> model1 = FXCollections.observableArrayList();
    ObservableList<Utilizator> model2 = FXCollections.observableArrayList();

    Stage dialogStage;
    Utilizator utilizator;

    @FXML
    private TableView<Utilizator> tableFriends;
    @FXML
    private TableColumn<Utilizator, String> columnFirstName1;
    @FXML
    private TableColumn<Utilizator, String> columnLastName1;
    @FXML
    private TableView<Utilizator> tableRecipients;
    @FXML
    private TableColumn<Utilizator, String> columnFirstName2;
    @FXML
    private TableColumn<Utilizator, String> columnLastName2;
    @FXML
    private TextField txtMessage;

    public void setService(UtilizatorService service, Stage stage, Utilizator u) {
        this.service = service;
        this.dialogStage=stage;
        this.utilizator =u;
        initModel();
    }

    private void initModel() {
        model1.setAll(service.getFriends(utilizator.getId()));
    }

    @FXML
    public void initialize() {
        columnFirstName1.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        columnLastName1.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));

        tableFriends.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Single-click
                Utilizator selected = tableFriends.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Move item to target table
                    model1.remove(selected);
                    model2.add(selected);
                }
            }
        });

        tableRecipients.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Single-click
                Utilizator selected = tableRecipients.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Move item to target table
                    model2.remove(selected);
                    model1.add(selected);
                }
            }
        });

        tableFriends.setItems(model1);
        columnFirstName2.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        columnLastName2.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        tableRecipients.setItems(model2);
    }

    public void handleSend(){
        service.addMessage(new Message(utilizator, model2,txtMessage.getText(), LocalDateTime.now(),null));
    }
}
