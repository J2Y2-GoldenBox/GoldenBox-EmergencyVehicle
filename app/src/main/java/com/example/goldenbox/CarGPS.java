package com.example.goldenbox;

public class CarGPS {
    public double latitude;
    public double longitude;
    public String caseNumber;
    public CarGPS(){}
    public CarGPS(double lat,double log,String t){
        this.latitude = lat;
        this.longitude = log;
        this.caseNumber =t;
    }
}
