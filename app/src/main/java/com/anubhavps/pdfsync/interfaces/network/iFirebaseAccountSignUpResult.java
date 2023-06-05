package com.anubhavps.pdfsync.interfaces.network;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public interface iFirebaseAccountSignUpResult extends iFirebaseResult{

    void onAccountCreated(Task<AuthResult> task);
    void onVerificationMailSent();

    void onUsernameAvailable();

    void onUsernameTaken();


}
