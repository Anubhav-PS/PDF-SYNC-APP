package com.anubhavps.pdfsync.models;

import com.anubhavps.pdfsync.enums.Sharing;
import com.google.firebase.Timestamp;

public class PDF {

    private String name;
    private String filename;
    private long size;
    private String url;
    private long uploadedOn;
    private boolean starred;
    private String sharing;
    private String commentsId;
    private boolean recycleBin;

    private String documentId;

    public PDF() {
    }

    public PDF(String filename, long size, String url, long uploadedOn) {
        this.filename = filename;
        this.size = size;
        this.url = url;
        this.uploadedOn = uploadedOn;
        this.name = User.getInstance().getName();
        this.starred = false;
        this.recycleBin = false;
        this.sharing = Sharing.PRIVATE.toString();
    }

    public PDF(String name, String filename, long size, String url, long uploadedOn, String documentId) {
        this.name = name;
        this.filename = filename;
        this.size = size;
        this.url = url;
        this.uploadedOn = uploadedOn;
        this.documentId = documentId;
        this.commentsId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public long getUploadedOn() {
        return uploadedOn;
    }

    public void setUploadedOn(long uploadedOn) {
        this.uploadedOn = uploadedOn;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public String getSharing() {
        return sharing;
    }

    public void setSharing(String sharing) {
        this.sharing = sharing;
    }

    public String getCommentsId() {
        return commentsId;
    }

    public void setCommentsId(String commentsId) {
        this.commentsId = commentsId;
    }

    public boolean isRecycleBin() {
        return recycleBin;
    }

    public void setRecycleBin(boolean recycleBin) {
        this.recycleBin = recycleBin;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
