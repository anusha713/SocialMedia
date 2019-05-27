package com.example.socialmedia;

public class AllPosts
{
    //this class contains methods for fields that will be displayed when a post is displayed
    String uid;
    String postpic;
    String caption;
    String profilepic;
    String username;

    public AllPosts()  //default constructor
    {

    }

    public AllPosts(String uid, String postpic, String caption, String profilepic, String username)
    {   //non-default constructor using arguments
        this.uid = uid;
        this.postpic = postpic;
        this.caption = caption;
        this.profilepic = profilepic;
        this.username = username;
    }

    //getter and setter methods
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostpic() {
        return postpic;
    }

    public void setPostpic(String postpic) {
        this.postpic = postpic;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

