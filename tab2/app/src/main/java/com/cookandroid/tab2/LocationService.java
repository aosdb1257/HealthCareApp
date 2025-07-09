package com.cookandroid.tab2;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends IntentService {

    private static final String TAG = "LocationService";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public LocationService() {
        super("LocationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();
        startLocationUpdates();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // 이 메서드에서는 작업을 수행하고 작업이 완료되면 자동으로 서비스가 종료됩니다.
        // 이 예제에서는 위치 업데이트를 계속 수신하는 것으로 간주합니다.
        // 위치 업데이트는 LocationCallback을 통해 처리됩니다.
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // 여기에서 위치 업데이트를 처리합니다.
                    // 새로운 위치 정보를 사용하여 필요한 작업을 수행할 수 있습니다.
                    Log.d(TAG, "New Location: " + location.getLatitude() + ", " + location.getLongitude());
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 위치 업데이트 간격: 5초
        locationRequest.setFastestInterval(2000); // 가장 빠른 위치 업데이트 간격: 2초
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 위치 업데이트를 중지합니다.
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
