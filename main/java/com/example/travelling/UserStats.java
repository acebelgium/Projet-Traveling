package com.example.travelling;

public class UserStats {
    private String level;
    private int treesSaved;
    private String userName;

    public UserStats(String level, int treesSaved, String userName) {
        this.level = level;
        this.treesSaved = treesSaved;
        this.userName = userName;
    }

    public String getLevel() { return level; }
    public int getTreesSaved() { return treesSaved; }
    public String getUserName() { return userName; }
}
