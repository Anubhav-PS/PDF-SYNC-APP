package com.anubhavps.pdfsync.interfaces.network;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public interface iFirebaseDocumentDownloadResult {


    void onDocumentDownloaded(Task<DocumentSnapshot> task);

    void onDocumentDownloadFailed(Exception e);

    void onDocumentNotExist();

}
