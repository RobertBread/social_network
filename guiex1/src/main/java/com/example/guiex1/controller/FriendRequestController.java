package com.example.guiex1.controller;

import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.domain.ValidationException;
import com.example.guiex1.services.UtilizatorService;
import com.example.guiex1.utils.Status;
import com.example.guiex1.utils.events.Event;
import com.example.guiex1.utils.events.UtilizatorEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendRequestController implements Observer<Event>{
    UtilizatorService service;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();

    Stage dialogStage;
    Utilizator utilizator;

    @FXML
    TableView<Utilizator> tableView;
    @FXML
    TableColumn<Utilizator,String> FirstName;
    @FXML
    TableColumn<Utilizator,String> LastName;
    @FXML
    TableColumn<Utilizator,String> Date;


    public void setService(UtilizatorService service, Stage stage, Utilizator u) {
        this.service = service;
        this.dialogStage = stage;
        this.utilizator = u;
        service.addObserver(this);
        initModel();
    }

    private void initModel() {
//        Iterable<Tuple<Utilizator,Status>> messages = utilizator.getFriends();
//        for(Tuple<Utilizator,Status> u : utilizator.getFriends()){
//            System.out.println(u.getLeft() + " " + u.getRight());
//        }
//        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false)
//                .filter(u-> Status.PENDING.equals(u.getRight())).map(u->u.getLeft())
//                .collect(Collectors.toList());
        model.setAll(service.getFriendRequests(utilizator.getId()));
    }

    @FXML
    public void initialize() {
        FirstName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        LastName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        Date.setCellValueFactory(cellData-> {
                    Utilizator user = cellData.getValue();
                    Tuple<Long,Long> idF = new Tuple<>(user.getId(),utilizator.getId());
                    Prietenie friend = service.findFriendship(idF);
                    return new SimpleStringProperty(friend.getDate().toString());
                }
        );
        tableView.setItems(model);
    }

    @Override
    public void update(Event event) {
        this.utilizator = service.searchUser(this.utilizator.getId());
        initModel();
    }

    public void handleDecline(){
        Utilizator user=(Utilizator) tableView.getSelectionModel().getSelectedItem();
        if(user!=null) {
            Tuple<Long, Long> idF = new Tuple<>(utilizator.getId(), user.getId());
            service.deleteFriendship(idF);
        }
        else{
            MessageAlert.showErrorMessage(null,"Nimic selectat de dat decline");
        }
    }

    public void handleAdd(ActionEvent actionEvent){
        Utilizator user=(Utilizator) tableView.getSelectionModel().getSelectedItem();
        if(user!=null) {
            try {
                Tuple<Long, Long> idF = new Tuple<>(utilizator.getId(), user.getId());
                Prietenie p = new Prietenie(utilizator.getId(),user.getId());
                service.addFriendship(p);
            }
            catch(ValidationException e){
                MessageAlert.showErrorMessage(null,e.getMessage());
            }
        }
        else{
            MessageAlert.showErrorMessage(null,"Nimic selectat de dat decline");
        }
    }
}