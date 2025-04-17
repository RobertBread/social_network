package com.example.guiex1.controller;

import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.domain.ValidationException;
import com.example.guiex1.services.UtilizatorService;
import com.example.guiex1.utils.events.Event;
import com.example.guiex1.utils.events.FriendshipEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class AddFriendController implements Observer<Event> {

    List<Utilizator> userList;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();

    @FXML
    private TextField first_name;
    @FXML
    private TextField last_name;
    @FXML
    private TableView<Utilizator> tableView;
    @FXML
    private TableColumn<Utilizator, String> columnFirstName;
    @FXML
    private TableColumn<Utilizator, String> columnLastName;

    private UtilizatorService service;
    Stage dialogStage;
    Utilizator utilizator;

    @FXML
    private void initialize() {
        columnFirstName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        columnLastName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        tableView.setItems(model);

        first_name.textProperty().addListener((observable, oldValue, newValue) -> initModel());

        last_name.textProperty().addListener((observable, oldValue, newValue) -> initModel());

    }

    public void setService(UtilizatorService service,  Stage stage, Utilizator u) {
        this.service = service;
        this.dialogStage=stage;
        this.utilizator =u;
        this.service.addObserver(this);
        userList = service.getCandidateFriends(utilizator.getId());
        initModel();
    }

    private void initModel() {
//        Iterable<Tuple<Utilizator, Status>> messages = utilizator.getFriends();
//        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false)
//                .filter(u->Status.ACTIVE.equals(u.getRight())).map(u->u.getLeft())
//                .collect(Collectors.toList());
        model.setAll(userList.stream().
                filter(u->u.getFirstName().contains(first_name.getText()))
                .filter(u->u.getLastName().contains(last_name.getText()))
                .toList());
    }

    @FXML
    public void handleAdd() {
        Utilizator friend = tableView.getSelectionModel().getSelectedItem();
        try {
            if (friend == null) {
                MessageAlert.showErrorMessage(null, "Nu ai selectat pe nimeni");
            } else {
                Long id1 = utilizator.getId();
                Long id2 = friend.getId();
                service.addFriendship(new Prietenie(id1, id2));
            }
        }catch (ValidationException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

    }

    @Override
    public void update(Event event) {
        userList = service.getCandidateFriends(utilizator.getId());
        initModel();
    }
}
