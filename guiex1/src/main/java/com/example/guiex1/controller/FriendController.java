package com.example.guiex1.controller;

import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.repository.dbrepo.MessageDbRepository;
import com.example.guiex1.services.UtilizatorService;
import com.example.guiex1.utils.Status;
import com.example.guiex1.utils.events.Event;
import com.example.guiex1.utils.events.FriendshipEntityChangeEvent;
import com.example.guiex1.utils.events.UtilizatorEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.example.guiex1.utils.paging.Page;
import com.example.guiex1.utils.paging.Pageable;

public class FriendController implements Observer<Event> {
    UtilizatorService service;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();

    Stage dialogStage;
    Utilizator utilizator;

    private int pageSize = 5;
    private int currentPage = 0;
    private int totalNumberOfElements = 0;

    @FXML
    TableView<Utilizator> tableView;
    @FXML
    TableColumn<Utilizator,String> FirstName;
    @FXML
    TableColumn<Utilizator,String> LastName;
    @FXML
    Label labelNume;
    @FXML
    Label labelPage;
    @FXML
    ComboBox<Integer> comboPage;
    @FXML
    Button buttonPrevious;
    @FXML
    Button buttonNext;

    public void setService(UtilizatorService service, Stage stage, Utilizator u) {
        this.service = service;
        this.dialogStage=stage;
        this.utilizator =u;
        labelNume.setText(utilizator.getFirstName()+" "+utilizator.getLastName());
        service.addObserver(this);
        comboPage.getItems().addAll(1,5,10,20);
        initModel();
    }

    @Override
    public void update(Event event) {
        this.utilizator = service.searchUser(this.utilizator.getId());
        if(event.getClass()==FriendshipEntityChangeEvent.class) {
            if(((FriendshipEntityChangeEvent) event).getData().getId().getRight().equals(utilizator.getId())) {
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "New ","Ai o cerere de prietenie noua");
            }
        }
        initModel();
        Scene scene = comboPage.getScene();
        scene.getStylesheets().add(
                getClass().getResource("/com/example/guiex1/css/style_add_friend.css").toExternalForm()
        );
    }

    private void initModel() {
        Page<Utilizator> page = service.findAllOnPage(new Pageable(pageSize,currentPage), utilizator.getId());
        // after delete, the number of pages might decrease
        int maxPage = (int) Math.ceil((double) page.getTotalNumberOfElements() / pageSize) - 1;
        if (maxPage == -1) {
            maxPage = 0;
        }
        if (currentPage > maxPage) {
            currentPage = maxPage;
            page = service.findAllOnPage(new Pageable(pageSize,currentPage), utilizator.getId());
        }
        totalNumberOfElements = page.getTotalNumberOfElements();
        buttonPrevious.setDisable(currentPage == 0);
        buttonNext.setDisable((currentPage + 1) * pageSize >= totalNumberOfElements);
        List<Utilizator> friends = StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(friends);
        labelPage.setText("Page " + (currentPage + 1) + " of " + (maxPage + 1));
    }

    @FXML
    public void initialize() {

        FirstName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        LastName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        tableView.setItems(model);
        comboPage.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = Integer.parseInt(newValue.toString());
                initModel();
            }
        });
    }

    public void handleRemove(ActionEvent actionEvent){
        Utilizator user=(Utilizator) tableView.getSelectionModel().getSelectedItem();
        if (user!=null) {
            Tuple<Long,Long> idF = new Tuple<>(this.utilizator.getId(), user.getId());
            Prietenie deleted= service.deleteFriendship(idF);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Delete Friendship","Userul a fost sters de la prieteni");
        }
        else MessageAlert.showErrorMessage(null, "NU ati selectat nici un prieten");
    }

    public void handleAdd(ActionEvent actionEvent){
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/add-friend-view.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Friend");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            AddFriendController AddFriendController = loader.getController();
            AddFriendController.setService(service, dialogStage, this.utilizator);

            dialogStage.show();

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void handleOpenChat(ActionEvent actionEvent){
        Utilizator friend=(Utilizator) tableView.getSelectionModel().getSelectedItem();
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
            messageController.setService(service, this.utilizator,friend);

            dialogStage.show();

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRequest(){

        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/friend-requests-view.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Friend Requests");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            FriendRequestController friendRequestController = loader.getController();
            friendRequestController.setService(service, dialogStage, this.utilizator);

            dialogStage.show();

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void handleEmail(){
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/email-view.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Send email");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            EmailController emailController = loader.getController();
            emailController.setService(service, dialogStage, this.utilizator);

            dialogStage.show();

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void handleNext(ActionEvent actionEvent) {
        currentPage ++;
        initModel();
    }

    public void handlePrevious(ActionEvent actionEvent) {
        currentPage --;
        initModel();
    }

    public void handleUploadPfp() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Poți folosi Stage-ul principal sau un alt element pentru a deschide dialogul
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            service.updateImage(utilizator.getId(), selectedFile);
        }
    }

    public void handleOpenProfile(){
        if(tableView.getSelectionModel().getSelectedItem()!=null)
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/profile-view.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Profile");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            ProfileController profileController = loader.getController();
            profileController.setService(service, tableView.getSelectionModel().getSelectedItem(),utilizator);

            dialogStage.show();

        } catch ( IOException e) {
            e.printStackTrace();
        }
        else {
            MessageAlert.showErrorMessage(null, "Nu ai selectat niciuni profil");
        }
    }

    public void handleOpenMyProfile(){
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/profile-view.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Profile");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            ProfileController profileController = loader.getController();
            profileController.setService(service, this.utilizator,utilizator);

            dialogStage.show();

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

}
