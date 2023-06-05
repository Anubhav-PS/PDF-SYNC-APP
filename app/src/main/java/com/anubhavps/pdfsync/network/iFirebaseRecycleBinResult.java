package com.anubhavps.pdfsync.network;

public interface iFirebaseRecycleBinResult extends iFirebaseResult {

    void onAddedToRecycleBin();

    void onRemovedFromRecycleBin();

}
