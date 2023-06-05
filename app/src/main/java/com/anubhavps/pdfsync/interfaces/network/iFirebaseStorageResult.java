package com.anubhavps.pdfsync.interfaces.network;

import com.anubhavps.pdfsync.enums.Sharing;
import com.anubhavps.pdfsync.models.PDF;

public interface iFirebaseStorageResult {

    void onPdfUploadFailed(Exception e);

    void onPdfDownloadUrlFailed(Exception e);

    void uploadingPdfProgress(double progress);

    void onPdfSuccessfullyUploadedToStorage(PDF pdf);

}
