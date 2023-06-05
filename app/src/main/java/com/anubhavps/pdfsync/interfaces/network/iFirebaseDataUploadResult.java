package com.anubhavps.pdfsync.interfaces.network;

public interface iFirebaseDataUploadResult {
    void onDataUploaded();
    void onDataUploadFailed(Exception e);

    void onPdfExists(boolean exists);


}
