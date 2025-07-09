package com.cookandroid.tab2;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Notification notification;

    NotificationCompat.Builder notificationBuilder;

    public SharedPreferences prefs;

    private Handler handler;
    private Runnable midnightRunnable;

    ApiService apiService = ApiClient.getClient().create(ApiService.class);
    

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("테스트 중")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis()) // Timestamp 표시를 위해
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 0 }) // 진동을 사용하지 않도록 설정
                .build();



        startForeground(1, notification);

        // 서비스가 시작될 때 호출되는 메서드
        Log.d(TAG, "Foreground service started.");

        // 여기서 필요한 작업을 수행
        // 예: 걸음 수 센서 데이터 처리

        prefs = getSharedPreferences("Pref", MODE_PRIVATE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    Log.e("Service", "서비스가 실행 중입니다...");

                    String totalStebCount = prefs.getString("총걸음수", "0");
                    String totalDt = prefs.getString("총이동거리", "0");
                    String totalCal = prefs.getString("칼로리", "0");
                    String totalTime = prefs.getString("측정시간", "0");



                   /* Log.v("Service", "@@@@@@@@@@@");
                    Log.v("Service", "총 걸음수 : "+ totalStebCount);
                    Log.v("Service", "총 거리 : "+ totalDt);
                    Log.v("Service", "총 칼로리 : "+ totalCal);
                    Log.v("Service", "총 시간 : "+ totalTime);
                    Log.v("Service", "@@@@@@@@@@@");

*/

                    notification = new NotificationCompat.Builder(ForegroundService.this, CHANNEL_ID)
                            .setContentTitle("Foreground Service")
                            .setContentText(totalStebCount)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setWhen(System.currentTimeMillis()) // Timestamp 표시를 위해
                            .setContentIntent(pendingIntent)
                            .setVibrate(new long[] { 0 }) // 진동을 사용하지 않도록 설정
                            .build();

                    // 기존에 생성된 Notification을 업데이트
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    if (notificationManager != null) {
                        notificationManager.notify(1, notification);
                    }



                    // DB 저장
                    // 닉 이랑 날짜 같으면 걸음수만 UPDATE하는 SQL문
                    // 닉 이랑 날짜 안 같으면 데이터 추가


                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        // 자정마다 작업 수행을 위한 Runnable 설정
        handler = new Handler();
        midnightRunnable = new Runnable() {
            @Override
            public void run() {
                saveAndResetData();

                // 다음 자정까지의 시간을 계산하여 Runnable을 다시 실행
                handler.postDelayed(this, calculateNextMidnightDelay());
            }
        };

        // 자정까지의 시간 계산하여 첫 실행 예약
        handler.postDelayed(midnightRunnable, calculateNextMidnightDelay());




        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Foreground service stopped.");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            serviceChannel.enableVibration(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void saveAndResetData() {
        String nickName = prefs.getString("닉네임", "0");
        String totalStepCount = prefs.getString("총걸음수", "0");
        String totalDt = prefs.getString("총이동거리", "0");
        String totalCal = prefs.getString("칼로리", "0");
        String totalTime = prefs.getString("측정시간", "0");

        // 여기에 DB 저장 작업을 추가하세요.
        // 예: 걸음수, 거리, 칼로리, 시간을 DB에 저장
        Log.d(TAG, "자정 데이터 저장: 걸음수=" + totalStepCount + ", 거리=" + totalDt + ", 칼로리=" + totalCal + ", 시간=" + totalTime);

        //DB에 총 걸음수 저장
        int totalStep = Integer.parseInt(totalStepCount);

        // db에 총걸음수 저장하는 코드
        CalculateRequest request = new CalculateRequest(totalStep, nickName);
        Call<Void> call = apiService.saveCalculate(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                } else {
                    Log.e("API Error", "Response Code: " + response.code() + ", Message: " + response.message());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API Error Body", "Error Body: " + errorBody);
                        // 추가적인 에러 메시지 출력
                        if (errorBody.contains("Not Found")) {
                            Log.e("API Error Detail", "The requested resource was not found on the server.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 네트워크 오류 또는 기타 이유로 인해 요청 실패
                Log.e("API Call Failure", "Error: " + t.getMessage());
            }
        });


        // 초기화
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("총걸음수", "0");
        editor.putString("총이동거리", "0");
        editor.putString("칼로리", "0");
        editor.putString("측정시간", "0");
        editor.apply();
    }

    private long calculateNextMidnightDelay() {
        long currentTime = System.currentTimeMillis();
        long nextMidnight = (currentTime / 86400000 + 1) * 86400000;
        return nextMidnight - currentTime;
    }




}
