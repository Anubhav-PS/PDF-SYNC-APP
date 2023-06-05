package com.anubhavps.pdfsync.network;

public interface iFirebaseAccountSignInResult extends iFirebaseResult {

    void onEmailVerified(String user_UID);

    void onEmailNotVerified();

    void onResetPasswordSent();

    void onUserMailIdFetched(String userMailId);

    void onUsernameNotRegistered();

    void onBadResponse();


}
