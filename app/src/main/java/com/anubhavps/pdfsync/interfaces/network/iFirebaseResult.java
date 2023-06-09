package com.anubhavps.pdfsync.interfaces.network;

import com.google.android.gms.tasks.Task;

public interface iFirebaseResult {

    void onErrorReported(Exception e);
    void onSuccess(Task<Void> task);

    void onSuccess(String message);

}
