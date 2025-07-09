package com.cookandroid.tab2;



import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACTIVITY_RECOGNITION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    public SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(InputActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                //Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

                //finish();
            }



        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("사용 권한을 거부하는 경우 이 서비스를 사용할 수 없습니다.\n\n[설정] > [권한]에서 권한을 설정하십시오")
                .setPermissions(BLUETOOTH_CONNECT,ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        ACTIVITY_RECOGNITION,
                        REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .check();



        prefs = getSharedPreferences("Pref", MODE_PRIVATE);

        boolean isFirstRun = prefs.getBoolean("isFirstRun",true);

        if(isFirstRun){
            Log.d("메인@@@@@@@@@@@@", "앱이 처음 실행되었습니다.");
            Log.d("메인@@@@@@@@@@@@", "앱이 처음 실행되었습니다.");
            Log.d("메인@@@@@@@@@@@@", "앱이 처음 실행되었습니다.");

            prefs.edit().putBoolean("isFirstRun",false).apply();
        }




        /*PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                //Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

                //finish();
            }


        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("사용 권한을 거부하는 경우 이 서비스를 사용할 수 없습니다.\n\n[설정] > [권한]에서 권한을 설정하십시오")
                .setPermissions(ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        BLUETOOTH_SCAN)
                .check();*/





        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Page page = Page.fromMenuItemId(item.getItemId());
            if (page != null) {
                viewPager.setCurrentItem(page.getPosition());
                return true;
            }
            return false;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Page page = Page.fromPosition(position);
                if (page != null) {
                    bottomNavigationView.setSelectedItemId(page.getMenuItemId());
                }
            }
        });






    }


}