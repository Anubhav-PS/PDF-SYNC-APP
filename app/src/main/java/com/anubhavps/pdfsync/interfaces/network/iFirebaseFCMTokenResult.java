package com.anubhavps.pdfsync.interfaces.network;

import com.google.android.gms.tasks.Task;

public interface iFirebaseFCMTokenResult {

    void fcmTokenGenerated(Task<String> task);

    void fcmTokenUploaded();

    void fcmTokenFailed(Exception e);


}
