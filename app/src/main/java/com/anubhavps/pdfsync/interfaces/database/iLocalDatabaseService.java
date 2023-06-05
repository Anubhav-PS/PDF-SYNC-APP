package com.anubhavps.pdfsync.interfaces.database;

import com.anubhavps.pdfsync.models.User;

public interface iLocalDatabaseService {

    void addUser(User user);

    User getUser();

    void updateUser(User user);

    boolean deleteUser();

}
