package com.anubhavps.pdfsync.interfaces.network;

public interface iFirebaseSharePdfResult {
    void onPdfSharedSuccessful(String message);
    void onPdfShareFailed(String message);
    void onErrorReported(Exception e);
}
