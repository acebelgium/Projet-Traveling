package com.example.traveling.share;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Comment {
    private String userName;
    private String text;
    private long timestamp;

    public Comment() {} // Nécessaire pour Firestore

    public Comment(String userName, String text, long timestamp) {
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUserName() { return userName; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE);
        return sdf.format(new Date(timestamp));
    }
}
