package com.example.goldenbox;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Geocode {

    public HashMap addressTogps(Geocoder geocoder,String st , String ds){
        List<Address> listStart = null;
        List<Address> listDestination = null;
        String start = st;
        String destination = ds;
        HashMap<String,Double> map = new HashMap<String, Double>();
        try {
            listStart = geocoder.getFromLocationName(start,10); // 읽을 개수
            listDestination = geocoder.getFromLocationName(destination,10);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
            return null;
        }
        if(listStart.size() == 0 || listDestination.size()==0) {
            return null;
        }else{
            map.put("startlatitude",listStart.get(0).getLatitude());
            map.put("startlongitude",listStart.get(0).getLongitude());
            map.put("destinationlatitude",listDestination.get(0).getLatitude());
            map.put("destinationlongitude",listDestination.get(0).getLongitude());
            return map;
        }

    }
}
