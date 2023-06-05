package com.anubhavps.pdfsync.network;

public interface iFirebaseDataUploadResult {
    void onDataUploaded();
    void onDataUploadFailed(Exception e);

    void onPdfExists(boolean exists);


}
