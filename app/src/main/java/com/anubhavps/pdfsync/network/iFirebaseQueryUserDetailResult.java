package com.anubhavps.pdfsync.network;

public interface iFirebaseQueryUserDetailResult {

    void onUserDetailSearchFound(String name,String mail_ID,String user_UID,String username);

    void onUserDoesNotExists();

    void onSearchFailed(Exception e);

}
