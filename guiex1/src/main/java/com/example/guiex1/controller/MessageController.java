package com.example.guiex1.controller;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.services.UtilizatorService;
import com.example.guiex1.utils.events.Event;
import com.example.guiex1.utils.events.MessageEntityChangeEvent;
import com.example.guiex1.utils.observer.Observer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.stream;

public class MessageController implements Observer<Event> {
    UtilizatorService service;
    Utilizator utilizator;
    Utilizator friend;
    ObservableList<Message> model = FXCollections.observableArrayList();
    @FXML
    private Label labelFriend;
    @FXML
    private TextField txtMessage;
    @FXML
    private TableView<Message> tableView;
    @FXML
    private TableColumn<Message, String> columnDate;
    @FXML
    private TableColumn<Message, String> columnMessage;
    @FXML
    private TableColumn<Message,String> columnReply;

    public void setService(UtilizatorService service, Utilizator utilizator, Utilizator friend) {
        this.service = service;
        this.utilizator = utilizator;
        this.friend = friend;
        service.addObserver(this);
        labelFriend.setText("Chating with "+friend.getFirstName()+" "+friend.getLastName());
        initModel();
    }

    private void initModel() {
        Iterable<Message> messages = service.getConvo(utilizator.getId(),friend.getId());
        List<Message> sorted = StreamSupport.stream(messages.spliterator(),false)
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList());
//        Message last = service.getChat(utilizator.getId(),friend.getId());
//        model.clear();
//        if(last!=null) {
//            while (last.getReply() != null) {
//                model.add(last);
//                last = last.getReply();
//            }
//            model.add(last);
//        }
        model.setAll(sorted);
    }

    @FXML
    public void initialize() {
        columnMessage.setCellValueFactory(new PropertyValueFactory<Message, String>("message"));
        columnReply.setCellValueFactory(cellData -> {
            Message reply = cellData.getValue().getReply();
            return new SimpleStringProperty(reply != null ? reply.getMessage() : "No Reply");
        });
        columnDate.setCellValueFactory(new PropertyValueFactory<Message, String>("date"));

        // Set row factory to color rows based on condition
        tableView.setRowFactory(tv -> new TableRow<Message>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle(""); // Reset style for empty or null rows
                } else {
                    // Check condition and set row color
                    if (item.getFrom().getId().equals(utilizator.getId())) {
                        setStyle("-fx-background-color: #075e54;"); // Color row yellow if from the same user
                    } else {

                    }
                }
            }
        });

        tableView.setItems(model); // Set items for the TableView
    }


    @Override
    public void update(Event event) {
        if(event.getClass()== MessageEntityChangeEvent.class){
            Message msg = (Message) ((MessageEntityChangeEvent) event).getData();
            initModel();
        }
    }

    public void handleSendMessage(){
        Message reply=(Message) tableView.getSelectionModel().getSelectedItem();
        String text = txtMessage.getText();
        Message msg = new Message(utilizator,List.of(friend),text, LocalDateTime.now(),reply);
        service.addMessage(msg);
        txtMessage.clear();
    }
}