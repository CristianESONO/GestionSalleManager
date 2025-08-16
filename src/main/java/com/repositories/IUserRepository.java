package com.repositories;

import java.util.List;

import com.entities.Client;
import com.entities.User;

public interface IUserRepository {
    public User findUserByLoginAndPassword(String login,String password);
    // Dans com.repositories.IUserRepository.java
    public User findByEmail(String email);
    public List<User> findAll();
    void addUser(User newUser);
    void updateUser(User user);
    void delete(int id);
    Client findById(int id);
    public boolean existsByName(String name);
     /**
     * Checks if a user with the given ID exists in the repository.
     * @param id The ID of the user to check.
     * @return true if a user with the specified ID exists, false otherwise.
     */
    public boolean existsById(int id); // Nouvelle méthode ajout
}
