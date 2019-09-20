package com.example.goldenbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Button sendGPSbutton,receiveGPSButton;
    private static TextView textView,latitudeView;
    public EditText startTextView,destinationTextView;
    public DatabaseReference mDatabase,mDatabase2;
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
    static String pathArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstlayout_activity);
        //setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase2 = FirebaseDatabase.getInstance().getReference();
        sendGPSbutton = (Button)findViewById(R.id.button1);
        receiveGPSButton = (Button)findViewById(R.id.button2);
        textView = (TextView)findViewById(R.id.textView2);
        latitudeView = (TextView)findViewById(R.id.latitudeView);
        startTextView = (EditText)findViewById(R.id.start);
        destinationTextView = (EditText)findViewById(R.id.destination);
        gc = new Geocode();
        final Geocoder geocoder =  new Geocoder(this);
        this.geocoder = geocoder;

        //LocationManager 객체를 얻어온다
        final LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        ImageButton toggle = (ImageButton)findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View button) {
                button.setSelected(!button.isSelected());
                if(formatDate ==null) {
                    long now = System.currentTimeMillis();
                    date = new Date(now);
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmm");
                    formatDate = sdfNow.format(date);
                    nowtime = formatDate;
                }
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
                        flag = getRoute();
                        if (flag == true){
                            while (arrayList2.size() == 0) {
                                //Log.d("TAGwhile", "wait..");
                            }
                            mDatabase.child(formatDate).child("route").setValue(pathArray);

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
                        if(flag == true) {
                            formatDate = null;
                            mDatabase.child("finish").child(nowtime).setValue("finished");
                            Log.d("GPS", "위치정보 미수신중");
                            lm.removeUpdates(mLocationListener);//  미수신할때는 반드시 자원해체를 해주어야 한다.
                        }
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
            cg = new CarGPS(latitude,longitude,formatDate);
            mDatabase.child(formatDate).child("emergencyCarLocation").setValue(cg);
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
        HashMap<String,Double> hm = gc.addressTogps(geocoder, destination, start);
        if(hm != null) {
            latitudeView.setText("출발지latitude : " + hm.get("startlatitude") + ", longitude : " + hm.get("startlongitude") + "\n" + "목적지latitude : " + hm.get("destinationlatitude") + ", longitude : " + hm.get("destinationlongitude"));
            destinationlongitude =  hm.get("destinationlongitude");
            destinationlatitude = hm.get("destinationlatitude");
            startlongitude =  hm.get("startlongitude");
            startlatitude = hm.get("startlatitude");
            HashMap<String, Double> result = new HashMap<>();
            result.put("startLatitude", startlatitude);
            result.put("startLongitude", startlongitude);
            result.put("destinationLatitude", destinationlatitude);
            result.put("destinationLongitude", destinationlongitude);

            mDatabase.child("destination").child(formatDate).setValue(result);
            new RouteFind().execute(hm);
            return true;
        }else{
            Toast.makeText(this,"주소가 잘못되었습니다. 다시 입력해주세요",Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
