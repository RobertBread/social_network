package com.example.guiex1.domain;

import com.example.guiex1.utils.Status;

import java.util.List;
import java.util.Objects;

public class Utilizator extends Entity<Long>{
    private String firstName;
    private String lastName;
    private List<Tuple<Utilizator, Status>> friends;
    private String parola;
    public Utilizator(){}

    public Utilizator(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Utilizator(String firstName, String lastName, List<Tuple<Utilizator, Status>> friends) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.friends = friends;
    }

    public Utilizator(String firstName, String lastName, String parola) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.parola = parola;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public List<Tuple<Utilizator, Status>> getFriends() {
        return friends;
    }

    public void setFriends(List<Tuple<Utilizator, Status>> friends) {
        this.friends = friends;
    }

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    @Override
    public String toString() {
        return "Utilizator{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", friends=" + friends + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getFriends());
    }
}