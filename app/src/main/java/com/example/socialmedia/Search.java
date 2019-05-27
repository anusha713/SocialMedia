package com.example.socialmedia;

public class Search
{
    public String profilePic;
    public String username;

    public Search()
    {

    }

    public Search(String profilePic, String username) {
        this.profilePic = profilePic;
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
