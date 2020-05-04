package com.example.uberclone.models;

public class Client {



    String name;
    String email;
    String id;
    String image;

    public Client(){

    }


    public Client(String name, String email, String id) {

        this.name = name;
        this.email = email;
        this.id = id;
    }

    public Client(String name, String email, String id, String image) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.image = image;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
