package com.example.guiex1.controller;

import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.services.UtilizatorService;
import com.example.guiex1.utils.events.Event;
import com.example.guiex1.utils.observer.Observer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.IOException;

public class ProfileController implements Observer<Event> {
    UtilizatorService service;
    Utilizator user;
    Utilizator opener;

    @FXML
    private ImageView imageView;
    @FXML
    private Label labelNume;
    @FXML
    private Label labelFriends;
    @FXML
    private Button buttonRemove;
    @FXML
    private ImageView imageMessage;

    public void setService(UtilizatorService service, Utilizator user, Utilizator opener) {
        this.service = service;
        this.user = user;
        this.opener = opener;
        if(user == opener){
            buttonRemove.setDisable(true);
        }

        service.addObserver(this);
        labelNume.setText(user.getFirstName() + " " + user.getLastName());
        Integer friends = this.service.getFriends(user.getId()).size();
        labelFriends.setText(String.valueOf(friends));
        Image image = service.getImage(user.getId());
        if(image != null) {
            imageView.setImage(image);
        }
        else {
            imageView.setImage(new Image("file:" + "C:\\Users\\staic\\Desktop\\Laborator MAP\\lab6-exemplu-GUI\\guiex1\\src\\main\\resources\\com\\example\\guiex1\\images\\default-pfp.jpg"));
        }

        imageMessage.setImage(new Image("file:" + "C:\\Users\\staic\\Desktop\\2747981-200.png"));

        imageMessage.setOnMouseClicked(event -> {
            handleOpenMessage();
        });
    }

    @Override
    public void update(Event e) {
        Integer friends = this.service.getFriends(user.getId()).size();
        labelFriends.setText(String.valueOf(friends));
        Image image = service.getImage(user.getId());
        if(image != null) {
            imageView.setImage(image);
        }
        else {
            imageView.setImage(new Image("file:" + "C:\\Users\\staic\\Desktop\\Laborator MAP\\lab6-exemplu-GUI\\guiex1\\src\\main\\resources\\com\\example\\guiex1\\images\\default-pfp.jpg"));
        }
    }

    public void handleOpenMessage(){
        Utilizator friend=user;
        if(friend==null){
            MessageAlert.showErrorMessage(null,"Vorbesti singur");
            return;
        }
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/message-view.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("ChatGPT");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            MessageController messageController = loader.getController();
            messageController.setService(service, opener,user);

            dialogStage.show();

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRemove(){
        Tuple<Long,Long> idF = new Tuple<>(this.opener.getId(), user.getId());
        service.deleteFriendship(idF);
        buttonRemove.setDisable(true);
    }
}
