package com.anubhavps.pdfsync.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String comment;
    private String by;
    private Timestamp time;
    private String documentId;

    public Comment() {
    }

    public Comment(String comment, String by, Timestamp time, String documentId) {
        this.comment = comment;
        this.by = by;
        this.time = time;
        this.documentId = documentId;
    }

    public Comment(String comment, String by) {
        this.comment = comment;
        this.by = by;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
