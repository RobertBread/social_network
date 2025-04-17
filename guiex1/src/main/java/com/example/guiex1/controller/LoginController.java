package com.example.guiex1.controller;

import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.domain.ValidationException;
import com.example.guiex1.services.UtilizatorService;
import com.example.guiex1.utils.Hashing;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

import static com.example.guiex1.utils.Hashing.hashPassword;

public class LoginController {
    public UtilizatorService service;
    Stage loginStage;

    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField passwordField;

    public void setService(UtilizatorService service, Stage loginStage) {
        this.service = service;
        this.loginStage = loginStage;
    }
    public void handleLogin(ActionEvent actionEvent) throws Exception {
        String password = passwordField.getText();
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        if(firstName.equals("admin") || password.equals("admin")) {
                try {
                    // Încarcă view-ul pentru prieteni
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("../views/utilizator-view.fxml"));

                    AnchorPane root = loader.load();

                    // Creează un dialog Stage.
                    Stage dialogStage = new Stage();
                    dialogStage.setTitle("Admin");
                    dialogStage.initModality(Modality.WINDOW_MODAL);

                    Scene scene = new Scene(root);
                    dialogStage.setScene(scene);

                    // Setează serviciul și utilizatorul în controller
                    UtilizatorController userController = loader.getController();
                    userController.setUtilizatorService(service);

                    dialogStage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
        }
        Utilizator utilizator = service.searchName(firstName, lastName);
        if (utilizator == null) {
            MessageAlert.showErrorMessage(null, "Utilizatorul nu este înregistrat!");
        } else {
            if(service.getHashedPasswordFromDB(utilizator.getId()).equals(hashPassword(password)))
                try {
                    String hashedInputPassword = hashPassword(passwordField.getText());
                        try {
                            // Încarcă view-ul pentru prieteni
                            FXMLLoader loader = new FXMLLoader();
                            loader.setLocation(getClass().getResource("../views/friends-view.fxml"));

                            AnchorPane root = loader.load();

                            // Creează un dialog Stage.
                            Stage dialogStage = new Stage();
                            dialogStage.setTitle("Friends");
                            dialogStage.initModality(Modality.WINDOW_MODAL);

                            Scene scene = new Scene(root);
                            dialogStage.setScene(scene);

                            // Setează serviciul și utilizatorul în controller
                            FriendController friendController = loader.getController();
                            friendController.setService(service, dialogStage, utilizator);

                            dialogStage.show();

                        } catch (IOException e) {
                            MessageAlert.showErrorMessage(null, "Eroare la încărcarea ferestrei Friends!");
                            e.printStackTrace();
                        }
                } catch (Exception e) {
                    MessageAlert.showErrorMessage(null, "Eroare la hashing-ul parolei!");
                    e.printStackTrace();
                }
            else{
                MessageAlert.showErrorMessage(null, "Parola incorecta!");
            }
        }

    }

    public void handleRegister(ActionEvent actionEvent) {
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String password = passwordField.getText();
        if(firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            MessageAlert.showErrorMessage(null, "Campuri goale!");
            return;
        }
        if(service.searchName(firstName, lastName) == null) {
            Utilizator utilizator = new Utilizator(firstName, lastName);
            service.addUtilizator(utilizator,password);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Register","Utilizatorul a fost inregistrat cu succes!");
        }
        else {
            MessageAlert.showErrorMessage(null, "Utilizatorul deja exista!");
        }
    }

    public void handleChange(ActionEvent actionEvent) throws Exception {
        Utilizator utilizator = service.searchName(txtFirstName.getText(), txtLastName.getText());
        String password = passwordField.getText();
        if (utilizator == null) {
            MessageAlert.showErrorMessage(null, "Utilizatorul nu este înregistrat!");
        } else {
            if (service.getHashedPasswordFromDB(utilizator.getId()).equals(hashPassword(password)))
                try {
                    // create a new stage for the popup dialog.
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("../views/change-view.fxml"));

                    AnchorPane root = (AnchorPane) loader.load();

                    // Create the dialog Stage.
                    Stage dialogStage = new Stage();
                    dialogStage.setTitle("Change");
                    dialogStage.initModality(Modality.WINDOW_MODAL);
                    //dialogStage.initOwner(primaryStage);
                    Scene scene = new Scene(root);
                    dialogStage.setScene(scene);

                    RegisterController registerController = loader.getController();
                    registerController.setService(service, dialogStage, utilizator);

                    dialogStage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            else{
                MessageAlert.showErrorMessage(null, "Parola incorecta!");
            }
        }
    }
}
