package com.turlygazhy.entity;

/**
 * Created by Yerassyl_Turlygazhy on 01-Mar-17.
 */
public class Thesis {
    private int id;
    private String bookName;
    private int userId;
    private String thesis;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setThesis(String thesis) {
        this.thesis = thesis;
    }

    public String getThesis() {
        return thesis;
    }
}
