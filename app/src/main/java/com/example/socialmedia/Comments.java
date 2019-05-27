package com.example.socialmedia;

public class Comments
{
    //fields
    public String username;  //comment will only display username of commenter and text of comment
    public String text;

    public Comments()  //default constructor
    {

    }

    public Comments(String username, String text)
    {
        //non-default constructor using arguments
        this.username = username;
        this.text = text;
    }

    //getter and setter methods
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
