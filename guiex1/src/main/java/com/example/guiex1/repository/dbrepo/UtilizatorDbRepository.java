package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.domain.Validator;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.utils.Status;
import javafx.scene.image.Image;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.guiex1.utils.Hashing.hashPassword;

public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private String url;
    private String username;
    private String password;
    private Validator<Utilizator> validator;

    public UtilizatorDbRepository(String url, String username, String password, Validator<Utilizator> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * @param id - long, the id of a user to found
     * @return Optional<User> - the user with the given id
     *                        -Optional.empty() otherwise
     */
    @Override
    public Optional<Utilizator> findOne(Long id) {
        Utilizator user;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from users U where U.id = '%d'", id))) {
            if(resultSet.next()){
                user = createUserFromResultSet(resultSet);
                return Optional.ofNullable(user);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    private Utilizator createUserFromResultSet(ResultSet resultSet) {
        try {
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");

            Long idd = resultSet.getLong("id");
            Utilizator user = new Utilizator(firstName, lastName);
            user.setId(idd);
            return user;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Utilizator user = createUserFromResultSet(resultSet);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity, String parola) {
        String sql = "insert into users (first_name, last_name) values (?, ?)";  // Inserare utilizator
        String sql1 = "insert into passwords (userID, HashedPassword) values (?, ?)";  // Inserare parola

        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS); // Aici solicităm returnarea ID-ului generat
             PreparedStatement ps1 = connection.prepareStatement(sql1)) {

            // Inserare utilizator în tabela `users`
            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                // Obține ID-ul utilizatorului inserat
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long userId = generatedKeys.getLong(1);  // ID-ul generat de baza de date
                        entity.setId(userId);  // Setează ID-ul în obiectul `entity`

                        // Hash-uiește parola
                        String hashedPassword = hashPassword(parola);

                        // Inserare parola hashuită în tabela `passwords`
                        ps1.setLong(1, userId);  // Folosește ID-ul generat
                        ps1.setString(2, hashedPassword);
                        ps1.executeUpdate();
                    }
                }
            }

            return Optional.empty();  // Returnează utilizatorul cu ID-ul setat

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Optional.of(entity);
    }



    @Override
    public Optional<Utilizator> delete(Long id) {
        String sql = "delete from users where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Optional<Utilizator> user = findOne(id);
            if(!user.isEmpty()) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Utilizator> updatePassword(Utilizator user, String s){
        if (user == null) {
            throw new IllegalArgumentException("Entity must not be null!");
        }

        // Validează obiectul utilizator
        validator.validate(user);

        // Interogare pentru a actualiza parola în tabelul 'passwords'
        String sql = "UPDATE passwords SET HashedPassword = ? " +
                "WHERE userID = (SELECT id FROM users WHERE first_name = ? AND last_name = ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Setează parametrii pentru actualizarea parolei
            ps.setString(1, s);  // Parola hashuită
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());

            // Execută interogarea și verifică dacă a fost afectat un rând
            if (ps.executeUpdate() > 0) {
                return Optional.of(user); // Parola a fost actualizată
            }

            // Dacă nu a fost afectat niciun rând (utilizator inexistent)
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace(); // Debugging
            throw new RuntimeException("Eroare la actualizarea parolei: " + e.getMessage());
        }
    }


    @Override
    public Optional<Utilizator> update(Utilizator user) {
        if(user == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(user);
        String sql = "update users set first_name = ?, last_name = ? where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1,user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setLong(3, user.getId());
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void updateProfilePicture(Long idUser,File file) {
        String sql;
        if(getImageFromDatabase(idUser) != null)
            sql = "UPDATE pfps SET DATA = ? WHERE id_user = ?";
        else
            sql = "INSERT INTO pfps(DATA, id_user) VALUES(?,?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             FileInputStream fis = new FileInputStream(file);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int image = (int) file.length();
            statement.setBinaryStream(1, fis, (int) file.length());
            statement.setLong(2, idUser);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Image saved successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Image getImageFromDatabase(Long id_user) {

        String sql = "SELECT data FROM pfps WHERE id_user = ?";
        Image image = null;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id_user);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                byte[] imageData = resultSet.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                image = new Image(bis);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return image;
    }
}
