package com.cookandroid.tab2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class Map extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<LatLng> pathPoints;
    private boolean tracking;
    private float totalDistance;

    private Button startButton;
    private Button resetButton;
    private TextView distanceTextView;

    private static final float MIN_DISTANCE = 30; // 최소 거리 (미터)
    private boolean isCameraSetByUser = false; // 사용자가 지도를 조작했는지 여부를 추적하는 변수

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_layout, container, false);

        // 초기화
        pathPoints = new ArrayList<>();
        tracking = false;
        totalDistance = 0;

        startButton = view.findViewById(R.id.startButton);
        distanceTextView = view.findViewById(R.id.distanceTextView);
        resetButton = view.findViewById(R.id.resetButton); // 초기화 버튼과 연결

        // 시작/정지 버튼 클릭 이벤트 설정
        startButton.setOnClickListener(v -> {
            if (!isGpsEnabled()) {
                showGpsDisabledAlert();
            }
            tracking = !tracking;
            if (tracking) {
                startButton.setText("Stop");
                pathPoints.clear();
                totalDistance = 0;
            } else {
                startButton.setText("Start");
            }
        });

        // 초기화 버튼 클릭 이벤트 설정
        resetButton.setOnClickListener(v -> {
            pathPoints.clear();
            totalDistance = 0;
            distanceTextView.setText("이동 거리: 0 km");
            if (mMap != null) {
                mMap.clear(); // 지도에서 폴리라인 제거
            }
        });

        // 맵 프래그먼트 설정
        CustomMapFragment mapFragment = (CustomMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 위치 클라이언트 설정
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        createLocationRequest();
        createLocationCallback();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // GPS 상태 확인
        if (!isGpsEnabled()) {
            showGpsDisabledAlert();
        }

        mMap.setMyLocationEnabled(true);
        startLocationUpdates();

        // 사용자가 지도를 조작할 때 콜백 설정
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isCameraSetByUser = true;
            }
        });
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 위치 업데이트 간격 설정 (5초)
        locationRequest.setFastestInterval(2000); // 가장 빠른 위치 업데이트 간격 설정 (2초)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 높은 정확도 우선
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (tracking) {
                        if (!pathPoints.isEmpty()) {
                            Location prevLocation = new Location("");
                            prevLocation.setLatitude(pathPoints.get(pathPoints.size() - 1).latitude);
                            prevLocation.setLongitude(pathPoints.get(pathPoints.size() - 1).longitude);
                            float distance = prevLocation.distanceTo(location);
                            if (distance >= MIN_DISTANCE) {
                                pathPoints.add(newLatLng);
                                totalDistance += distance / 1000; // 거리 계산 (킬로미터)
                                distanceTextView.setText(String.format("계산한 거리: %.2f km", totalDistance));
                                updatePolyline();
                            }
                        } else {
                            pathPoints.add(newLatLng);
                        }
                    }
                    if (!isCameraSetByUser) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15));
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updatePolyline() {
        if (mMap != null) {
            mMap.clear();
            PolylineOptions polylineOptions = new PolylineOptions().addAll(pathPoints).width(10).color(Color.BLUE).clickable(true);
            mMap.addPolyline(polylineOptions);
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGpsDisabledAlert() {
        new AlertDialog.Builder(requireContext())
                .setTitle("GPS 비활성화")
                .setMessage("GPS를 활성화해야 지도 기능을 사용할 수 있습니다. 설정으로 이동하여 GPS를 활성화하시겠습니까?")
                .setPositiveButton("활성화", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    startButton.setText("Start");
                    pathPoints.clear();
                    totalDistance = 0;
                    dialog.dismiss();
                })
                .show();
    }

}
