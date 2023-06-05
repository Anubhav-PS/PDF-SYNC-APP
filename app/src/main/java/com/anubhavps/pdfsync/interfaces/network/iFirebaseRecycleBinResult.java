package com.anubhavps.pdfsync.interfaces.network;

public interface iFirebaseRecycleBinResult extends iFirebaseResult {

    void onAddedToRecycleBin();

    void onRemovedFromRecycleBin();

}
