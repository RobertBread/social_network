package com.example.guiex1.domain;

import com.example.guiex1.domain.Prietenie;
import com.example.guiex1.domain.ValidationException;
import com.example.guiex1.domain.Validator;

import java.util.function.Predicate;

public class FriendshipValidator implements Validator<Prietenie> {
    @Override
    /***
     * Metoda de validare al unui friendship
     * @param entity Friendshipul care va fi validat
     * @throws ValidationException daca friendship-ul este intre un utilizator si el insusi
     */
    public void validate(Prietenie entity) throws ValidationException {
        Predicate<Prietenie> p = (Prietenie f) -> f.getId().getLeft().equals(f.getId().getRight());
        if(p.test(entity)) {
            throw new ValidationException("Friendship IDs must not be the same");
        }
    }
}