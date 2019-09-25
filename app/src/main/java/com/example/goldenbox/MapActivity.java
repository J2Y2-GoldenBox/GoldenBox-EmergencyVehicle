package com.example.goldenbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Location destination;
    private ArrayList<Location> routes;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        destination = intent.getParcelableExtra("destination");
        routes = intent.getParcelableArrayListExtra("route");

        FragmentManager fragmentManager = getSupportFragmentManager();

        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);

        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        naverMap.setCameraPosition(new CameraPosition(new LatLng(routes.get(0)), 15));
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        Marker marker = new Marker(new LatLng(destination));
        marker.setIcon(MarkerIcons.RED);
        marker.setCaptionText("목적지");
        marker.setCaptionAlign(Align.Center);
        marker.setMap(naverMap);

        ArrayList<LatLng> latlngs = new ArrayList<>();
        for(int i = 0; i < routes.size(); i++){
            latlngs.add(new LatLng(routes.get(i)));
        }

        PathOverlay pathOverlay = new PathOverlay();
        pathOverlay.setCoords(latlngs);
        pathOverlay.setColor(Color.argb(255, 255, 150, 150));

        pathOverlay.setMap(naverMap);
    }
}
