package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.domain.Validator;
import com.example.guiex1.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDbRepository implements Repository<Long, Message> {

    private String url;
    private String username;
    private String password;

    public MessageDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Optional<Message> delete(Long id) {
        return Optional.empty();
    }

    public Optional<Message> update(Message m) {
        return Optional.empty();
    }


    public Optional<Message> findOne(Long id) {
        Message msg;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE id = ?")){
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                msg = createMessageFromResultSet(resultSet);
                return Optional.ofNullable(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        return null;
    }



    public Iterable<Message> findConvo(Long id1,Long id2) {
        Set<Message> messages = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE id1 = ? AND id2 = ? OR id1 = ? AND id2 = ? ORDER BY date ASC");) {
            statement.setLong(1, id1);
            statement.setLong(2, id2);
            statement.setLong(3, id2);
            statement.setLong(4, id1);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Message message = createMessageFromResultSet(resultSet);
                if(message.getReply()!=null){
                    Message reply = findOne(message.getReply().getId()).get();
                    message.setReply(reply);
                }
                messages.add(message);
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    private Message createMessageFromResultSet(ResultSet resultSet) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statementU1 = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
             PreparedStatement statementU2 = connection.prepareStatement("SELECT * FROM users WHERE id = ?");)
        {
            statementU1.setLong(1, resultSet.getLong("id1"));
            statementU2.setLong(1, resultSet.getLong("id2"));
            ResultSet resultSetU1 = statementU1.executeQuery();
            ResultSet resultSetU2 = statementU2.executeQuery();
            Long id = resultSet.getLong("id");

            Utilizator u1 = null;
            Utilizator u2 = null;

            if(resultSetU1.next()) {
                u1 = new Utilizator(resultSetU1.getString("first_name"), resultSetU1.getString("last_name"));
                u1.setId(resultSetU1.getLong("id"));
            }
            if(resultSetU2.next()) {
                u2 = new Utilizator(resultSetU2.getString("first_name"), resultSetU2.getString("last_name"));
                u2.setId(resultSetU2.getLong("id"));
            }
            Message replyMsg = new Message();
            Long id2 = resultSet.getLong("id2");
            Timestamp date = resultSet.getTimestamp("date");
            LocalDateTime localDateTime = date.toLocalDateTime();
            Long reply = resultSet.getLong("reply");
            if(resultSet.wasNull())
                reply = null;
            else{
                replyMsg.setId(reply);
            }
            String text = resultSet.getString("message");
            Message msg = new Message(u1, List.of(u2),text,localDateTime,reply==null ? null : replyMsg);
            msg.setId(id);
            return msg;
        } catch (SQLException e) {
            return null;
        }
    }


    @Override
    public Optional<Message> save(Message entity, String s) {
        entity.getTo().forEach(user->{
            String sql = "insert into messages (id1, id2,message,date,reply) values (?, ?,?,?,?)";
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setLong(1, entity.getFrom().getId());
                ps.setLong(2, user.getId());
                ps.setString(3, entity.getMessage());
                ps.setTimestamp(4, java.sql.Timestamp.valueOf(entity.getDate()));
                if(entity.getReply()!=null){
                    ps.setLong(5, entity.getReply().getId());
                }
                else{
                    ps.setNull(5, Types.INTEGER);
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                //e.printStackTrace();
            }
        });
        return Optional.empty();
    }
}