package com.example.guiex1.controller;

import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.services.UtilizatorService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.security.Provider;

public class AddUserController {

    private UtilizatorService service;
    Stage stage;

    @FXML
    private TextField first_name_field;
    @FXML
    private TextField last_name_field;
    @FXML
    private TextField passField;

    public void setService(UtilizatorService service, Stage stage) {
        this.service = service;
        this.stage = stage;
    }

    @FXML
    public void initialize(){
    }

    public void handleAdd(ActionEvent actionEvent) {
        String name = first_name_field.getText();
        String last_name = last_name_field.getText();
        String password = passField.getText();
        Utilizator user = new Utilizator(name, last_name);
        Utilizator u = service.searchName(name, last_name);
        if(user != null && u == null){
            service.addUtilizator(user, password);
        }
        else{
            MessageAlert.showErrorMessage(null,"Utilizatorul nu este valid sau exista deja!");
        }
    }
}
