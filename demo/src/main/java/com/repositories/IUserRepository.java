package com.repositories;

import java.util.List;

import com.entities.User;

public interface IUserRepository {
    public User findUserByLoginAndPassword(String login,String password);
    public List<User> findAll();
    void addUser(User newUser);
    void updateUser(User user);
    void delete(int id);
    public boolean existsByName(String name);
}
