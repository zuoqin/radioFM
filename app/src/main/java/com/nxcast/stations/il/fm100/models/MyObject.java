package com.nxcast.stations.il.fm100.models;

public class MyObject {

    private String myString;
    private String myImage;
    public MyObject() {
    }

    public MyObject(String myImage, String myString) {
        this.myImage = myImage;
        this.myString = myString;
    }

    public MyObject(String myImage) {
        this.myImage = myImage;
    }

    public String getMyImage() {
        return myImage;
    }

    public void setMyImage(String myImage) {
        this.myImage = myImage;
    }

    public String getMyString() {
        return myString;
    }

    public void setMyString(String myString) {
        this.myString = myString;
    }
}
