package com.example.goldenbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Button sendGPSbutton,receiveGPSButton;
    private static TextView textView,latitudeView;
    public EditText startTextView,destinationTextView;
    public static DatabaseReference mDatabase,mDatabase2;
    public String url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=127.1058342,37.359708&goal=129.075986,35.179470&option=trafast";
    public Geocode gc;
    Geocoder geocoder;
    float Lat, Lng;
    ToggleButton tb;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =0;
    static ArrayList<RouteFind.RouteGPS> arrayList2 = new ArrayList<>();
    private CarGPS cg=null;
    Date date;
    static String formatDate=null;
    String nowtime =null;
    static double startlatitude,startlongitude,destinationlatitude,destinationlongitude;
    boolean flag = false;
    static boolean checkflag = false;
    static String pathArray;
    LocationManager lmdestroy=null;
    DatabaseReference myRef;
    ImageButton toggle;
    static boolean onStartFlag=false;
    static String onstartLatitude=null,onstartLongitude=null, ondestinationLatitude = null, ondestinationLongitude=null,onStartTime=null,startAddress=null,destinationAddress= null;
    RelativeLayout layout2 = null;
    //LocationManager 객체를 얻어온다
    LocationManager lm2 = null;

    @Override
    protected void onStart(){

        Log.d("START","onStart()");
        onStartFlag=false;
        formatDate=null;
        mDatabase.child("destination").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("SNAP",dataSnapshot.getKey());
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.d("SNAP2",snapshot.getKey());
                    DataSnapshot snap = snapshot.child("finish");
                    if(snap.getValue().equals("false")){

                        onstartLatitude = snapshot.child("startLatitude").getValue().toString();
                        onstartLongitude = snapshot.child("startLongitude").getValue().toString();
                        ondestinationLatitude = snapshot.child("destinationLatitude").getValue().toString();
                        ondestinationLongitude = snapshot.child("destinationLongitude").getValue().toString();
                        startAddress = snapshot.child("startAddress").getValue().toString();
                        destinationAddress = snapshot.child("destinationAddress").getValue().toString();
                        onStartFlag=true;
                        onStartTime = snapshot.getKey();

                        Log.d("SNAP4","실행됨");
                        toggle.setSelected(true);
                        //layout.setBackgroundResource(R.color.red);
                        try{
                                flag = getRoute();

                                if (flag == true){

                                    layout2.setBackgroundResource(R.color.red);
                                    while (true) {
                                        //Log.d("TAGwhile", "wait..");
                                        if(checkflag==true){
                                            break;
                                        }
                                    }
                                    mDatabase.child(onStartTime).child("route").setValue(pathArray);
                                    Log.d("TAGPATH",pathArray);

                                    mDatabase.child("finish").child(onStartTime).removeValue();

                                    Log.d("GPS", "수신중...");
                                    // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                                    lm2.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                            100, // 통지사이의 최소 시간간격 (miliSecond)
                                            1, // 통지사이의 최소 변경거리 (m)
                                            mLocationListener);
                                    lm2.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                            100, // 통지사이의 최소 시간간격 (miliSecond)
                                            1, // 통지사이의 최소 변경거리 (m)
                                            mLocationListener);
                                }
                        }catch (SecurityException ex) {
                        }
                    }
                    Log.d("SNAP3",snap.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        super.onStart();
    }
    @Override
    protected void onPause(){
        Log.d("PAUSE","onPuase()");
        super.onPause();
    }
    @Override
    protected void onStop(){
        Log.d("STOP","onStop()");
        super.onStop();
    }
    @Override
    protected void onDestroy(){
        Log.d("DESTROY","onDestroy()");
        super.onDestroy();
    }
    @Override
    protected void onRestart(){
        Log.d("RESTART","onRestart()");
        super.onRestart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstlayout_activity);
        //setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference("code");
        sendGPSbutton = (Button)findViewById(R.id.button1);
        receiveGPSButton = (Button)findViewById(R.id.button2);
        textView = (TextView)findViewById(R.id.textView2);
        //latitudeView = (TextView)findViewById(R.id.latitudeView);
        startTextView = (EditText)findViewById(R.id.start);
        destinationTextView = (EditText)findViewById(R.id.destination);
        gc = new Geocode();
        final Geocoder geocoder =  new Geocoder(this);
        this.geocoder = geocoder;
        toggle = (ImageButton)findViewById(R.id.toggle);
        final RelativeLayout layout = (RelativeLayout)findViewById(R.id.plinear);
        layout2=layout;
        //LocationManager 객체를 얻어온다
        final LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm2 = lm;
        startTextView.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent){
                switch (i){
                    case KeyEvent.KEYCODE_ENTER:
                        destinationTextView.requestFocus();
                }
                return true;
            }
        });
        destinationTextView.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                switch (i) {
                    case KeyEvent.KEYCODE_ENTER:
                        InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

                        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                }
                return true;
            }
        });

        //ImageButton toggle = (ImageButton)findViewById(R.id.toggle);

        toggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View button) {
                button.setSelected(!button.isSelected());

                //
                Log.d("Log","try");
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //tv.setText("권한이 허용되지 않음");
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
                try{
                    if(button.isSelected()) {//여기는 경로 얻어서 파이어 베이스 올리는 구역
                        InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

                        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        if(formatDate ==null) {
                            long now = System.currentTimeMillis();
                            date = new Date(now);
                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmm");
                            formatDate = sdfNow.format(date);
                            Log.d("SNAP10",formatDate);
                            nowtime = formatDate;
                        }
                        flag = getRoute();

                        if (flag == true){
                            layout.setBackgroundResource(R.color.red);
                            while (true) {
                                //Log.d("TAGwhile", "wait..");
                                if(checkflag==true){
                                    break;
                                }
                            }
                            mDatabase.child(formatDate).child("route").setValue(pathArray);
                            Log.d("TAGPATH",pathArray);

                            mDatabase.child("finish").child(formatDate).removeValue();

                            Log.d("GPS", "수신중...");
                            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                    100, // 통지사이의 최소 시간간격 (miliSecond)
                                    1, // 통지사이의 최소 변경거리 (m)
                                    mLocationListener);
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                    100, // 통지사이의 최소 시간간격 (miliSecond)
                                    1, // 통지사이의 최소 변경거리 (m)
                                    mLocationListener);
                        }
                    } else {
                            lm.removeUpdates(mLocationListener);//  미수신할때는 반드시 자원해체를 해주어야 한다.
                            lm2.removeUpdates(mLocationListener);
                            checkflag = false;
                            if(onStartFlag==true)
                                mDatabase.child("finish").child(onStartTime).setValue("finished");
                            else
                                mDatabase.child("finish").child(nowtime).setValue("finished");
                            if(onStartFlag == true)
                                mDatabase.child("destination").child(onStartTime).child("finish").setValue("true");
                            else
                                mDatabase.child("destination").child(nowtime).child("finish").setValue("true");
                            Log.d("GPS", "위치정보 미수신중");
                            formatDate=null;
                            layout.setBackgroundResource(R.color.nonecolor);
                            onStartFlag=false;
                            flag=false;

                    }
                }catch (SecurityException ex) {
                }
            }
        });
    }



    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            /*locationSubject.onNext*/Log.d("GPS","위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n고도 : " + altitude + "\n정확도 : " + accuracy);
            if(onStartFlag==true)
                cg = new CarGPS(latitude,longitude,onStartTime);
            else
                cg = new CarGPS(latitude,longitude,formatDate);
            if(onStartFlag==true)
                mDatabase.child(onStartTime).child("emergencyCarLocation").setValue(cg);
            else
                mDatabase.child(nowtime).child("emergencyCarLocation").setValue(cg);
        }

        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };


    public boolean getRoute(){

        String start = startTextView.getText().toString();
        String destination = destinationTextView.getText().toString();
        HashMap<String,Double> hm = gc.addressTogps(geocoder, start, destination);
        if(hm != null) {
            //latitudeView.setText("출발지latitude : " + hm.get("startlatitude") + ", longitude : " + hm.get("startlongitude") + "\n" + "목적지latitude : " + hm.get("destinationlatitude") + ", longitude : " + hm.get("destinationlongitude"));
            destinationlongitude =  hm.get("destinationlongitude");
            destinationlatitude = hm.get("destinationlatitude");
            startlongitude =  hm.get("startlongitude");
            startlatitude = hm.get("startlatitude");
            HashMap<String, String> result = new HashMap<>();
            result.put("startLatitude", Double.toString(startlatitude));
            result.put("startLongitude",  Double.toString(startlongitude));
            result.put("destinationLatitude",  Double.toString(destinationlatitude));
            result.put("destinationLongitude",  Double.toString(destinationlongitude));
            result.put("startAddress",start);
            result.put("destinationAddress",destination);
            result.put("finish","false");


            mDatabase.child("destination").child(nowtime).setValue(result);
            new RouteFind().execute(hm);
            return true;
        }else if(onStartFlag==true){
            HashMap<String, String> result = new HashMap<>();
            result.put("startLatitude", onstartLatitude);
            result.put("startLongitude",  onstartLongitude);
            result.put("destinationLatitude", ondestinationLatitude);
            result.put("destinationLongitude", ondestinationLongitude);
            result.put("startAddress",startAddress);
            result.put("destinationAddress",destinationAddress);
            result.put("finish","false");
            HashMap<String,Double> hm2 = gc.addressTogps(geocoder, startAddress, destinationAddress);

            mDatabase.child("destination").child(onStartTime).setValue(result);

            new RouteFind().execute(hm2);
            return true;
        }else{
            Toast.makeText(this,"주소가 잘못되었습니다. 다시 입력해주세요",Toast.LENGTH_LONG).show();
            toggle.setSelected(false);
            return false;
        }
    }
}

