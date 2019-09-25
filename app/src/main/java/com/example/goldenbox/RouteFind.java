package com.example.goldenbox;

import android.net.ParseException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class RouteFind extends AsyncTask<HashMap,String,String> {
    public String defaulturl = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?";
    ArrayList<RouteGPS> arrayList = new ArrayList<>();
    public DatabaseReference mDatabase;
    //private DatabaseReference mDatabase=null;
    public static class RouteGPS{
        double latitude;
        double longitude;
        public RouteGPS(){
            latitude=0;
            longitude=0;
        }
        public RouteGPS(double lat,double lng){
            this.latitude=lat;
            this.longitude=lng;
        }
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(HashMap... strings) {

        Log.d("start",strings[0].get("startlatitude").toString());
        Log.d("start",strings[0].get("startlongitude").toString());
        Log.d("destination",strings[0].get("destinationlatitude").toString());
        Log.d("destination",strings[0].get("destinationlongitude").toString());

        searchRoute(strings[0].get("startlatitude"), strings[0].get("startlongitude"),strings[0].get("destinationlatitude"),strings[0].get("destinationlongitude"));

        return null;
    }
    public void searchRoute(Object startlatitdue, Object startlongitude,Object destinationlatitude,Object destinationlongitude){
        Double startlat = (Double)startlatitdue;
        Double startlng = (Double)startlongitude;
        Double destinationlat = (Double)destinationlatitude;
        Double destinationlng = (Double)destinationlongitude;
        String start = "start="+startlng.toString()+","+startlat.toString();
        String destination = "goal="+destinationlng.toString() + ","+ destinationlat.toString();
        String url = defaulturl+start+"&"+destination+"&option=trafast";
        Log.d("TAG", url);
        String json = request(url);
        readJson(json);
    }
    private void readJson(String json){
        try{
            JSONObject responseJSON = new JSONObject(json);
            String route = responseJSON.getString("route");
            Log.d("ROUTE",route);
            JSONObject routeJSON = new JSONObject(route);
            String trafast = routeJSON.getString("trafast");
            Log.d("TRAFAST",trafast);
            JSONArray trafastArray = new JSONArray(trafast);
            Log.d("TAG",Integer.toString(trafastArray.length()));
            JSONObject jsonObject=trafastArray.getJSONObject(0);
            Log.d("TAG",jsonObject.toString());
            String path = jsonObject.getString("path");

            Log.d("PATH",path.toString());
            JSONArray pathArray = new JSONArray(path);
            RouteGPS[] rg = new RouteGPS[pathArray.length()];
            for(int i=0;i<pathArray.length();i++){
                JSONArray pathLATLNG = pathArray.getJSONArray(i);
                Double lng = pathLATLNG.getDouble(0);
                Double lat = pathLATLNG.getDouble(1);
                Log.d("LNG",lng.toString()+" , "+lat.toString());
                rg[i] =new RouteGPS(lat,lng);
                arrayList.add(rg[i]);

                MainActivity.pathArray = path;
                MainActivity.checkflag = true;
            }
            Log.d("TAGRouteFind",Integer.toString(arrayList.size()));
            //MainActivity.arrayList2 = arrayList;


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private String request(String URL){
        StringBuilder output = new StringBuilder();
        StringBuffer response=null;
        try{
            URL url = new URL(URL);
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            if(conn != null){
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID","9kmmkhgbf4");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY","8QCYLtkcY0ExjWt2y7o9FNzuWhd26TwkHXAXaK3U");
                conn.setRequestProperty("Accept-Charest","utf-8");

                int resCode = conn.getResponseCode();
                Log.d("resCode",String.valueOf(resCode));
                BufferedReader reader;
                if(resCode == 200){
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                }else{
                    Log.d("TAG","here10");
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                String inputLine;
                response = new StringBuffer();
                while((inputLine = reader.readLine()) != null){
                    response.append(inputLine);
                }
                reader.close();
                Log.d("TAG",response.toString());
            }
        }catch (Exception ex){
            Log.e("SampleHttp","Exception in processing response.",ex);
            ex.printStackTrace();
        }
        Log.d("TAG","here6");
        Log.d("TAG",output.toString());
        return response.toString();
    }
}
