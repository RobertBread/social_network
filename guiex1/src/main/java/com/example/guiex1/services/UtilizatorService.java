package com.example.guiex1.services;



import com.example.guiex1.controller.AddFriendController;
import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.Utilizator;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.repository.dbrepo.FriendshipDbRepository;
import com.example.guiex1.repository.dbrepo.MessageDbRepository;
import com.example.guiex1.repository.dbrepo.UtilizatorDbRepository;
import com.example.guiex1.utils.Status;
import com.example.guiex1.utils.events.*;
import com.example.guiex1.utils.observer.Observable;
import com.example.guiex1.utils.observer.Observer;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import com.example.guiex1.utils.paging.Page;
import com.example.guiex1.utils.paging.Pageable;
import javafx.scene.image.Image;

public class UtilizatorService implements Observable<Event> {
    private UtilizatorDbRepository repo;
    private FriendshipDbRepository repofriend;
    private MessageDbRepository repoMessage;
    private List<Observer<Event>> observers=new ArrayList<>();

    public UtilizatorService(UtilizatorDbRepository repo, FriendshipDbRepository repofriend, MessageDbRepository repoMessage) {
        this.repo = repo;
        this.repofriend = repofriend;
        this.repoMessage = repoMessage;
    }


    public Utilizator addUtilizator(Utilizator user, String s) {
        if(repo.save(user,s).isEmpty()){
            UtilizatorEntityChangeEvent event = new UtilizatorEntityChangeEvent(ChangeEventType.ADD, user);
            notifyObservers(event);
            return null;
        }
        return user;
    }

    public String getHashedPasswordFromDB(Long userID) {
        String sql = "SELECT hashedPassword FROM passwords WHERE userID = ?";
        String hashedPassword = null;

        // Conectează-te la baza de date
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/MAP", "postgres", "1234");
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Setează parametrii pentru interogare
            ps.setLong(1, userID);

            // Execută interogarea
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hashedPassword = rs.getString("hashedPassword");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hashedPassword; // Returnează parola hash-uită (sau null dacă nu există)
    }

    public Utilizator deleteUtilizator(Long id){
        Optional<Utilizator> user=repo.delete(id);
        if (user.isPresent()) {
            notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.DELETE, user.get()));
            return user.get();
        }
        return null;
    }

    public Iterable<Utilizator> getAll(){
        return repo.findAll();
    }

    public Iterable<Prietenie> getAllFriendship(){
        return repofriend.findAll();
    }

    @Override
    public void addObserver(Observer<Event> e) {
        observers.add(e);

    }


    @Override
    public void removeObserver(Observer<Event> e) {
        //observers.remove(e);
    }

    @Override
    public void notifyObservers(Event t) {
        observers.stream().forEach(x->x.update(t));
    }


    public Utilizator updatePasswordUtilizator(Utilizator user, String s) {
        Optional<Utilizator> oldUser = repo.findOne(user.getId());
        if(oldUser.isPresent()){
            Optional<Utilizator> newUser = repo.updatePassword(user,s);
            if(newUser.isEmpty())
                notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.UPDATE, user, oldUser.get()));
            return newUser.orElse(null);
        }
        return oldUser.orElse(null);
    }
    public Utilizator updateUtilizator(Utilizator u) {
        Optional<Utilizator> oldUser=repo.findOne(u.getId());
        if(oldUser.isPresent()) {
            Optional<Utilizator> newUser=repo.update(u);
            if (newUser.isEmpty())
                notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.UPDATE, u, oldUser.get()));
            return newUser.orElse(null);
        }
        return oldUser.orElse(null);
    }

    public Prietenie addFriendship(Prietenie p) {
        String s = "1";
        Prietenie friendship = repofriend.findOne(p.getId()).orElse(null);
        if(friendship != null && friendship.getStatus().equals(Status.PENDING) && p.getId().getLeft().equals(friendship.getId().getRight())){
            repofriend.update(new Prietenie(friendship.getId().getLeft(),friendship.getId().getRight(),friendship.getDate(), Status.ACTIVE));
            UtilizatorEntityChangeEvent event = new UtilizatorEntityChangeEvent(ChangeEventType.UPDATE, null);
            notifyObservers(event);
            return null;
        }
        if(repofriend.save(p,s).isEmpty()){
            FriendshipEntityChangeEvent event = new FriendshipEntityChangeEvent(ChangeEventType.FRIEND, p);
            notifyObservers(event);
            return null;
        }
        return p;
    }


    public Prietenie deleteFriendship(Tuple<Long,Long> id) {
        Optional<Prietenie> friendship=repofriend.delete(id);
        if (friendship.isPresent()) {
            notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.DELETE, null));
            return friendship.get();
        }
        return null;
    }

    public Utilizator searchUser(Long id){
        return repo.findOne(id).orElse(null);
    }

    public Utilizator searchName(String firstName, String lastName){
        List<Utilizator> list = StreamSupport.stream(repo.findAll().spliterator(),false)
                .filter(u->u.getFirstName().equals(firstName)&&u.getLastName().equals(lastName))
                .collect(Collectors.toList());
        if(list.isEmpty()){
            return null;
        }
        else return list.get(0);
    }

    public Prietenie findFriendship(Tuple<Long,Long>id){
        return repofriend.findOne(id).orElse(null);
    }


    public Iterable<Message> getConvo(Long id1, Long id2){
        return repoMessage.findConvo(id1,id2);
    }

    public Message addMessage(Message m){
        if(repoMessage.save(m,"1").isEmpty()){
            MessageEntityChangeEvent event = new MessageEntityChangeEvent(ChangeEventType.ADD, m);
            notifyObservers(event);
            return null;
        }
        return m;
    }

    public List<Utilizator> getFriends(Long id){
        return StreamSupport.stream(repofriend.findAll(id).spliterator(),false)
                .map(u->u.getId().getRight()==id ? repo.findOne(u.getId().getLeft()).get() : repo.findOne(u.getId().getRight()).get())
                .collect(Collectors.toList());
    }

    public List<Utilizator> getFriendRequests(Long id){
        List<Utilizator> list =  StreamSupport.stream(repofriend.findAllRequests(id).spliterator(),false)
                .filter(u->u.getId().getRight()==id)
                .map(u->repo.findOne(u.getId().getLeft()).get())
                .collect(Collectors.toList());
        return list;
    }

    public List<Utilizator> getCandidateFriends(Long id) {
        List<Prietenie> friendships = StreamSupport.stream(repofriend.findAll().spliterator(),false)
                .toList();
        List<Utilizator> list =  StreamSupport.stream(repo.findAll().spliterator(),false)
                .filter(u->!u.getId().equals(id))
                .filter(u->friendships.stream().noneMatch(f->u.getId().equals(f.getId().getLeft())&&id.equals(f.getId().getRight()) || u.getId().equals(f.getId().getRight())&&id.equals(f.getId().getLeft())))
                .collect(Collectors.toList());
        return list;
    }

    public Page<Utilizator> findAllOnPage(Pageable pageable, Long id) {
        Page<Prietenie> page = null;
        try {
            page = repofriend.findAllOnPage(pageable, id);
        }catch(SQLException e){
            page = null;
        }
        return new Page<Utilizator>(StreamSupport.stream(page.getElementsOnPage().spliterator(),false)
                .map(u->u.getId().getRight()==id ? repo.findOne(u.getId().getLeft()).get() : repo.findOne(u.getId().getRight()).get())
                .collect(Collectors.toList()), page.getTotalNumberOfElements());
    }

    public void updateImage(Long idUser, File file){
        this.repo.updateProfilePicture(idUser,file);
        notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.UPDATE, null));
    }

    public Image getImage(Long idUser){
        return this.repo.getImageFromDatabase(idUser);
    }


}
