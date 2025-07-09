package com.cookandroid.tab2;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACTIVITY_RECOGNITION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static android.content.Context.MODE_PRIVATE;
import static android.text.format.DateUtils.formatElapsedTime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends Fragment implements SensorEventListener {
    private static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    private BluetoothSPP bt;

    private TextView temp;
    private TextView textView;
    private TextView hr;
    private TextView stepCountView;
    private TextView distanceCountView;
    private TextView caloriesBurnedView;
    private TextView elapsedTimeView;
    private TextView textViewGoalHeight;
    private ProgressBar progressBar;
    private Button btnConnect;
    private Button btnSend;
    private Button toggleButton;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private int stepCount = 0;
    private float totalDistance = 0;
    private int totalSteps = 0;
    private float stepLength = 0.78f; // 걸음 길이 (미터 단위)
    private Handler handler;
    private Runnable distanceUpdater;
    private OnDataPointListener onStepDataPointListener;
    private OnDataPointListener onDistanceDataPointListener;
    private boolean isTracking = false;
    private BarChart barChart;
    public SharedPreferences prefs;
    private ImageView stepGifImageView;
    private SharedViewModel sharedViewModel;
    private boolean isGifVisible = false;

    private static final float METRIC_RUNNING_FACTOR = 1.027f; // 걷기의 경우 1.027
    private static final float KILOGRAMS_TO_POUNDS = 2.20462f; // 킬로그램을 파운드로 변환하기 위한 상수
    private static final float RUNNING_VO2_MET = 3.5f; // 대사율 산소 소비의 최소값 (VO2)
    private static final float RUNNING_KCAL_PER_HOUR_PER_KG = 1.036f; // 킬로그램당 시간당 소모 칼로리

    float weightKG = 0.0f; // 입력받은 몸무게

    private double totalCaloriesBurned = 0.0;
    private long startTime = 0; // 측정 시간

    long totalElapsedTime = 0; //토탈 측정 시간

    private boolean isTimerRunning = false; // 걸음 측정 여부

    long currentTime = 0;
    long elapsedTimeInSeconds = 0;

    long timeThreshold = 3; // 정지로 판단할 시간 임계값 (밀리초 단위)

    float sec = 0.0F;

    private FitnessOptions fitnessOptions;

    private boolean start = false; // 걸음 측정 여부

    private int imageIndex = 0;
    private int[] images = {
            R.drawable.walking_image2,
            R.drawable.clap_image,
            R.drawable.run_image,
            R.drawable.walking_image2,
            R.drawable.clap_image
    };

    ApiService apiService = ApiClient.getClient().create(ApiService.class);

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_layout, container, false);

        temp = view.findViewById(R.id.temp);
        hr = view.findViewById(R.id.hr);
        btnConnect = view.findViewById(R.id.btnConnect);
        //btnSend = view.findViewById(R.id.btnSend);
        stepCountView = view.findViewById(R.id.stepCountView);
        distanceCountView = view.findViewById(R.id.distance_count);
        progressBar = view.findViewById(R.id.progressBar);
        //toggleButton = view.findViewById(R.id.toggle_button);
        barChart = view.findViewById(R.id.barChart);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.setStepGifImageView(stepGifImageView);

       // otherClassInstance = new StepCounterWorker(); // OtherClass의 인스턴스 생성

        prefs = requireContext().getSharedPreferences("Pref", MODE_PRIVATE);

        // 이 부분 DB에 저장!@!@!@@!@!@!@!@
        String height2 = prefs.getString("키", "000");
        String weight2 = prefs.getString("몸무게", "0");
        String goalWeight2 = prefs.getString("목표몸무게", "0");
        String daysDifferenceStr2 = prefs.getString("일수", "0");
        int daysDifference2 = Integer.parseInt(daysDifferenceStr2); // 문자열로 저장된 값을 다시 숫자로 변환

        String goalStr2 = prefs.getString("목표걸음수", "10");
        double goal2 = Double.parseDouble(goalStr2); // 문자열로 저장된 값을 다시 숫자로 변환

        int intGoal = (int) Math.round(goal2);

        Log.v("@목표 걸음수@@@@@", String.valueOf(intGoal));

        textViewGoalHeight = view.findViewById(R.id.goalText);
        textViewGoalHeight.setText("목표\n" + intGoal + " 걸음");
        progressBar.setMax(intGoal);

        // 현재 날짜와 요일
        textView = view.findViewById(R.id.textView);
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일, (E)", Locale.getDefault());
        String currentDateAndDay = sdf.format(new Date());
        textView.setText(currentDateAndDay);

        //칼로리 계산
        caloriesBurnedView = view.findViewById(R.id.calories_count);

        //측정 시간
        elapsedTimeView = view.findViewById(R.id.time_count);

        weightKG = Float.parseFloat(weight2);

        Log.i(TAG, height2);

        bt = new BluetoothSPP(getContext());

            /*if (!bt.isBluetoothAvailable()) {
                Toast.makeText(getContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return view;
            }*/

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // 포그라운드 서비스 시작 버튼 설정
        Button startServiceButton = view.findViewById(R.id.startServiceButton);
        startServiceButton.setOnClickListener(v -> {
            start =true;
            setupGoogleFit();
            Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
            serviceIntent.putExtra("inputExtra", "Foreground Service is running");
            ContextCompat.startForegroundService(requireContext(), serviceIntent);
        });

        // 포그라운드 서비스 중지 버튼 설정
        Button stopServiceButton = view.findViewById(R.id.stopServiceButton);
        stopServiceButton.setOnClickListener(v -> {
            start =false;
            unregisterFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);
            Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
            requireContext().stopService(serviceIntent);
        });

        if (stepCounterSensor == null) {
            Toast.makeText(getContext(), "걸음 센서 없음", Toast.LENGTH_SHORT).show();
        }

        textViewGoalHeight = view.findViewById(R.id.goalText);
        Intent intent2 = getActivity().getIntent();
        //double dsteb = 0.0;
        if (intent2 != null) {
            /*String steb = intent2.getStringExtra("kg");
            if(steb != null){
                dsteb = Double.parseDouble(steb);
            }else {
                dsteb = 0.0;
            }
            int isteb = (int) Math.round(dsteb); // DB
            textViewGoalHeight.setText("목표\n" + isteb + " 걸음");

            progressBar.setMax(isteb);
            progressBar.setProgress(totalSteps);*/

        }



        stepGifImageView = view.findViewById(R.id.stepGifImageView);

        setupBluetooth();
        setupGoogleFit();

        handler = new Handler(Looper.getMainLooper());
        /*distanceUpdater = new Runnable() {
            @Override
            public void run() {
                *//*readDistanceData();
                handler.postDelayed(this, 1000);*//*
            }
        };*/

        //시작하기
        /*toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isListenerRegistered){
                    registerFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);
                }
                //readStepData();
                toggleButton.setVisibility(View.GONE);


                *//*if (!isTracking) {
                    if (!isListenerRegistered){
                        registerFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);
                    }
                    readStepData();
                    toggleButton.setText("일시정지");
                    Log.i(TAG, "시작!@!@!@!!@!@!@!@@!");
                    isTracking =true;
                }else{
                    stopTracking();
                    //unregisterFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);
                    toggleButton.setText("시작");
                    Log.i(TAG, "일시정지!@!@!@!!@!@!@!@@!");
                    isTracking =false;
                }*//*
            }
        });*/

        return view;
    }
    private void setupBluetooth() {
        // 블루투스 연결 권한 확인
        if (ContextCompat.checkSelfPermission(getContext(), BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 처리
            // 예를 들어, 사용자에게 권한을 요청하거나 다른 조치를 취할 수 있음
            // 여기서는 권한이 없다는 메시지를 출력하고 메서드를 종료
            Toast.makeText(getContext(), "@@Bluetooth Connect permission not granted", Toast.LENGTH_SHORT).show();
        }
        bt.setOnDataReceivedListener((data, message) -> {
            String[] values = message.split(",");
            if (values.length > 0) {
                String temperature = values[0];
                temp.setText("체온: " + temperature + "'C");

            }

            if (values.length > 1) {
                String heartRate = values[1];
                hr.setText("심박수: " + heartRate);

            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getContext(), "Connected to " + name + "\n" + address, Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getContext(), "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getContext(), "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        btnConnect.setOnClickListener(v -> {
            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                bt.disconnect();
            } else {
                Intent intent = new Intent(getContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });
    }

    private void setupGoogleFit() {
        fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        //.addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(getContext()),
                    fitnessOptions);
        } else {

            Log.i(TAG, "구독까지 실행됨1@@@@");
            subscribe();
        }
    }
    private void subscribe() {
        Fitness.getRecordingClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "구독까지 실행됨2@@@@@");
                            //readStepData();
                            registerFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);

                        } else {
                            Log.w(TAG, "There was a problem subscribing to steps.", task.getException());
                        }
                    }
                });
        /*Fitness.getRecordingClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .subscribe(DataType.TYPE_DISTANCE_DELTA)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Successfully subscribed to distance!");
                            readDistanceData();
                            registerFitnessDataListener(DataType.TYPE_DISTANCE_DELTA);
                        } else {
                            Log.w(TAG, "There was a problem subscribing to distance.", task.getException());
                        }
                    }
                });*/
    }
    private void unsubscribe() {
        /*Fitness.getRecordingClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "구독 취소가 성공적으로 완료되었습니다.");
                        } else {
                            Log.w(TAG, "구독 취소 중 문제가 발생했습니다.", task.getException());
                        }
                    }
                });*/

        Fitness.getRecordingClient(requireContext(), Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(requireContext())))
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(unused ->
                        Log.i(TAG,"구독 취소가 성공적으로 완료되었습니다."))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "구독 취소 중 문제가 발생했습니다.");
                    // Retry the unsubscribe request.
                });
    }
    private void readStepData() {
        Fitness.getHistoryClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        totalSteps = dataSet.isEmpty() ? 0 :
                                dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        stepCountView.setText(String.valueOf(totalSteps));
                        Log.v(TAG, " 걸음수!@!@!@!@!@!@!@");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting the step count.", e);
                    }
                });
    }
    /*private void readDistanceData() {
        Fitness.getHistoryClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        if (!dataSet.isEmpty()) {
                            totalDistance = dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                        }
                        distanceCountView.setText(String.valueOf(totalDistance));
                        Log.v(TAG, " 거리!@!@!@!@!@!@!@");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting the distance.", e);
                    }
                });
    }*/
    private boolean isListenerRegistered = false;

    /*private void startTracking() {
        isTracking = true;
        isPaused = true;

        toggleButton.setText("일시정지");
        if (!isListenerRegistered) {
            registerFitnessDataListener(DataType.TYPE_DISTANCE_DELTA);
            registerFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);
            isListenerRegistered = true;
        }

        //handler.post(distanceUpdater);

        *//*if (onStepDataPointListener != null) {
            Fitness.getSensorsClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                    .add(
                            new SensorRequest.Builder()
                                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                    .setDataType(DataType.TYPE_DISTANCE_DELTA)
                                    .setSamplingRate(1, TimeUnit.SECONDS)
                                    .setFastestRate(1, TimeUnit.SECONDS)
                                    .build(),
                            onStepDataPointListener)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "Step Listener registered successfully!");
                            } else {
                                Log.e(TAG, "Step Listener not registered.", task.getException());
                            }
                        }
                    });
        }*//*
    }*/
    private void stopTracking() {
        //isTracking = false;
        //isPaused = false;
        //handler.removeMessages(0);
        //handler.removeCallbacks(distanceUpdater);
        //handler.removeCallbacksAndMessages(null);
        //toggleButton.setText("시작");
        //Log.i(TAG, "Stopped tracking distance.");
        /*if (isListenerRegistered) {
            unregisterFitnessDataListener(DataType.TYPE_DISTANCE_DELTA);
            unregisterFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);
            isListenerRegistered = false;
        }*/
        unregisterFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);

        if (onStepDataPointListener != null) {
            Fitness.getSensorsClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                    .remove(onStepDataPointListener)
                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful() && task.getResult()) {
                                Log.i(TAG, "Step Listener was removed!");
                            } else {
                                Log.i(TAG, "Step Listener was not removed.");
                            }
                        }
                    });
        }
    }
    private boolean isPaused = false;


    private void registerFitnessDataListener(DataType dataType) {


            Log.v(TAG, "현재 start : " + start);

            // SharedPreferences 객체 생성
            SharedPreferences.Editor editor = prefs.edit();
            prefs = requireContext().getSharedPreferences("Pref", MODE_PRIVATE);

            OnDataPointListener listener = dataPoint -> {
                Log.v(TAG, "DataPoint received: " + dataPoint);
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value value = dataPoint.getValue(field);
                    Log.v(TAG, "Field: " + field.getName() + " Value: " + value);

                    if (dataPoint.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                    /*totalDistance += value.asFloat();
                    distanceCountView.setText(String.valueOf(totalDistance));
                    Log.v(TAG, "리스너 거리!@!@!@!@!@!@!@");*/
                        Log.v(TAG, "총 거리 : " + String.valueOf(totalDistance));
                    } else if (dataPoint.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) { // else if 사용


                        Log.v(TAG, "실시간 걸음 수 : " + value.asInt());
                            if( value.asInt() <= 5) {
                                Log.v(TAG, "실시간 걸음 수 10 이하임@!  " + value.asInt());
                                totalSteps += value.asInt();
                                totalDistance += value.asInt() * stepLength; // 거리 계산
                                progressBar.setProgress(totalSteps);


                                // 걸음 수가 증가한 후에 시간을 시작하도록 수정
                                if (value.asInt() > 0 && !isTimerRunning) {
                                    startTime = System.currentTimeMillis();
                                    isTimerRunning = true;
                                }

                                // 시간 업데이트 @!@! 1초에 한번씩만 실행되게 코드 변경하기
                                if (isTimerRunning) {
                                    currentTime = System.currentTimeMillis();
                                    sec += (float) ((currentTime - startTime) / 1000.0);
                                    Log.v(TAG, "작동한 시간 몇초인지 : " + sec);

                                    if (sec >= 1) {
                                        totalElapsedTime += 1;
                                        Log.v(TAG, "시간 1초 플러스");
                                        sec -= 1;

                                    }

                                    if (elapsedTimeInSeconds > 0) {
                                        Log.v(TAG, "시간 1초 플러스");
                                        totalElapsedTime += elapsedTimeInSeconds;
                                    }

                                    startTime = currentTime;
                                }

                                if (sec >= timeThreshold) {
                                    // 센서 이벤트가 일정 시간 동안 없으면 정지 상태로 판단
                                    sec = 0; // 센서가 정지했으므로 이전 timestamp를 초기화
                                }

                                float a = Float.parseFloat(calculateCaloriesBurned(totalDistance, weightKG)); // 칼로리 계산


                                // 데이터 저장
                                editor.putString("총걸음수", String.valueOf(totalSteps));
                                editor.putString("총이동거리", String.valueOf(Integer.parseInt(String.valueOf(totalDistance))));
                                editor.putString("칼로리", String.valueOf(a));
                                editor.putString("측정시간", String.valueOf(totalElapsedTime));

                                // 변경사항 저장
                                editor.apply();


                                //
                                String totalStebCount = prefs.getString("총걸음수", "0");
                                String totalDt = prefs.getString("총이동거리", "0");
                                String totalCal = prefs.getString("칼로리", "0");
                                String totalTime = prefs.getString("측정시간", "0");


                                totalSteps = Integer.parseInt(totalStebCount);
                                totalDistance = Float.parseFloat(totalDt);
                                totalCaloriesBurned = Float.parseFloat(totalCal);
                                totalElapsedTime = Long.parseLong(totalTime);


                                stepCountView.setText(String.valueOf(totalSteps));
                                distanceCountView.setText(String.valueOf(totalDistance));

                                elapsedTimeView.setText(formatElapsedTime(totalElapsedTime));


                                //DB 저장




                                Log.v(TAG, "리스너 걸음수!@!@!@!@!@!@!@");

                            }
                    }
                }
            };

            if (dataType.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                onStepDataPointListener = listener;
                Log.i(TAG, "onStepDataPointListener assigned.");
            } else if (dataType.equals(DataType.TYPE_DISTANCE_DELTA)) {
                onDistanceDataPointListener = listener;
                Log.i(TAG, "onDistanceDataPointListener assigned.");
            }

        if(!start ) {
            Fitness.getSensorsClient(requireContext(), Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(requireContext())))
                    .remove(listener) // 센서 요청 제거
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "센서 일시정지 입니다");
                        } else {
                            Log.e(TAG, "Failed to stop sensor data collection.", task.getException());
                        }
                    });
        }else {
            Fitness.getSensorsClient(requireContext(), Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(requireContext())))
                    .add(new SensorRequest.Builder()
                            .setDataType(dataType)
                            .setSamplingRate(1, TimeUnit.SECONDS)
                            .setFastestRate(1, TimeUnit.SECONDS)
                            .build(), listener)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.v(TAG, dataType + " 리스너 연결!@!@!@!@!@");
                            //isListenerRegistered = true;
                        } else {
                            Log.e(TAG, "Sensor not registered.", task.getException());
                        }


                    });
        }
        }

    private void unregisterFitnessDataListener(DataType dataType) {
        // 리스너 객체 초기화
        OnDataPointListener listenerToRemove = null;

        // 구독한 데이터 유형에 따라 해당 리스너 할당
        if (dataType.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
            listenerToRemove = onStepDataPointListener;
        } else if (dataType.equals(DataType.TYPE_DISTANCE_DELTA)) {
            listenerToRemove = onDistanceDataPointListener;
        }

        // 리스너가 null이 아닌 경우에만 제거
        if (listenerToRemove != null) {
            // 센서 클라이언트에서 센서 데이터 요청 제거
            Fitness.getSensorsClient(requireContext(), GoogleSignIn.getLastSignedInAccount(requireContext()))
                    .remove(listenerToRemove)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Listener removed successfully!");
                            isListenerRegistered = false;
                        } else {
                            Log.e(TAG, "Listener removal failed.", task.getException());
                        }
                    });

            // 리스너 변수 초기화
            if (dataType.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                onStepDataPointListener = null;
            } else if (dataType.equals(DataType.TYPE_DISTANCE_DELTA)) {
                onDistanceDataPointListener = null;
            }
        } else {
            Log.e(TAG, "Listener is null. Cannot remove listener.");
        }
    }
    /*private void unregisterFitnessDataListener(DataType dataType) {
        OnDataPointListener listener = null;

        if (dataType.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
            listener = onStepDataPointListener;
            Log.i(TAG, "@@@@@@@@걸음 수 연결 끊기.");
        } *//*else if (dataType.equals(DataType.TYPE_DISTANCE_DELTA)) {
            listener = onDistanceDataPointListener;
            Log.i(TAG, "거리 연결 끊기.");
        }*//*

        if (listener == null) {
            Log.e(TAG, "Listener is null for dataType: " + dataType);
            return;
        }

        Log.v(TAG, dataType + " 리스너 null 아님");

        Fitness.getSensorsClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .remove(listener)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult()) {
                        Log.v(TAG, dataType + " Listener was removed!");
                    } else {
                        Log.i(TAG, dataType + " Listener was not removed.");
                    }
                });
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stepGifImageView.setImageResource(0);
        //handler.removeCallbacks(distanceUpdater);
        if (onStepDataPointListener != null) {
            Fitness.getSensorsClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                    .remove(onStepDataPointListener)
                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful() && task.getResult()) {
                                Log.i(TAG, "Step Listener was removed!");
                            } else {
                                Log.i(TAG, "Step Listener was not removed.");
                            }
                        }
                    });
        }
        if (onDistanceDataPointListener != null) {
            Fitness.getSensorsClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                    .remove(onDistanceDataPointListener)
                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful() && task.getResult()) {
                                Log.i(TAG, "Distance Listener was removed!");
                            } else {
                                Log.i(TAG, "Distance Listener was not removed.");
                            }
                        }
                    });
        }

        bt.stopService();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() { // 앱이 열릴때 마다
        super.onStart();
        //stopTracking();
        //registerFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);


        prefs = requireContext().getSharedPreferences("Pref", MODE_PRIVATE);

        String goalStr2 = prefs.getString("목표걸음수", "10");
        double goal2 = Double.parseDouble(goalStr2); // 문자열로 저장된 값을 다시 숫자로 변환

        int intGoal = (int) Math.round(goal2);
        progressBar.setMax(intGoal);

        imageIndex = prefs.getInt("이미지번호", 0);
        stepGifImageView.setVisibility(View.VISIBLE);
        Glide.with(this)
                .asGif()
                .load(images[imageIndex])
                .error(R.drawable.image)
                .into(stepGifImageView);


        //
        String totalStebCount = prefs.getString("총걸음수", "0");
        String totalDt = prefs.getString("총이동거리", "0");
        String totalCal = prefs.getString("칼로리", "0");
        String totalTime = prefs.getString("측정시간", "0");

        //DB 자정 마다 값 저장할꺼야 (totalStebCount) 이것만 일단 자정마다 저장되게

        /*if(자정이면){
            조건 닉네임, totalStebCount 이 값을 저장해야되

        }*/

        progressBar.setProgress(Integer.parseInt(totalStebCount));

        totalSteps = Integer.parseInt(totalStebCount);
        totalDistance = Float.parseFloat(totalDt);
        totalCaloriesBurned = Float.parseFloat(totalCal);
        totalElapsedTime = Long.parseLong(totalTime);

        stepCountView.setText(totalStebCount);
        distanceCountView.setText(totalDt);
        caloriesBurnedView.setText(totalCal);
        elapsedTimeView.setText(formatElapsedTime(totalElapsedTime));

        Log.v(TAG, "@@@@@@@@@@@");
        Log.v(TAG, "총 걸음수 : "+ totalStebCount);
        Log.v(TAG, "총 거리 : "+ totalDt);
        Log.v(TAG, "총 칼로리 : "+ totalCal);
        Log.v(TAG, "총 시간 : "+ totalTime);
        Log.v(TAG, "@@@@@@@@@@@");






        if (totalSteps >= intGoal) {

            imageIndex = (imageIndex + 1) % images.length;
            stepGifImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asGif()
                    .load(images[imageIndex])
                    .error(R.drawable.image)
                    .into(stepGifImageView);
            prefs = requireContext().getSharedPreferences("Pref", MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("이미지번호", imageIndex);
            editor.apply();


        }



        Call<List<CalculateRequest>> cal = apiService.getAllCalculates();
        cal.enqueue(new Callback<List<CalculateRequest>>() {
            @Override
            public void onResponse(Call<List<CalculateRequest>> call, Response<List<CalculateRequest>> response) {
                if (response.isSuccessful()) {
                    List<CalculateRequest> calculates = response.body();
                    StringBuilder result = new StringBuilder();

                    // 현재 날짜 계산
                    LocalDate today = LocalDate.now();
                    LocalDate yesterday = today.minusDays(1);
                    LocalDate twoDaysAgo = today.minusDays(2);
                    LocalDate threeDaysAgo = today.minusDays(3);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    String formattedToday = today.format(formatter);
                    String formattedYesterday = yesterday.format(formatter);
                    String formattedTwoDaysAgo = twoDaysAgo.format(formatter);
                    String formattedThreeDaysAgo = threeDaysAgo.format(formatter);

                    // 결과 출력
                    System.out.println("Today: " + formattedToday);
                    System.out.println("Yesterday: " + formattedYesterday);
                    System.out.println("Two Days Ago: " + formattedTwoDaysAgo);
                    System.out.println("Three Days Ago: " + formattedThreeDaysAgo);

                    // 걸음 수 데이터를 저장할 맵
                    Map<String, Integer> stepsMap = new HashMap<>();
                    stepsMap.put(formattedToday, 0);
                    stepsMap.put(formattedYesterday, 0);
                    stepsMap.put(formattedTwoDaysAgo, 0);
                    stepsMap.put(formattedThreeDaysAgo, 0);

                    // Calculate 데이터 처리
                    for (CalculateRequest calculate : calculates) {
                        int totalStep = calculate.getTotalStep();
                        String name = calculate.getName();
                        int day = calculate.getDay();
                        int month = calculate.getMonth();
                        int year = calculate.getYear();
                        String date = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);

                        // 날짜가 4일 이내인 데이터만 처리
                        if (stepsMap.containsKey(date)) {
                            stepsMap.put(date, totalStep);
                        }

                        result.append("Date: ").append(date)
                                .append(", Total Steps: ").append(totalStep)
                                .append(", Name: ").append(name).append("\n");
                    }

                    // 결과를 TextView에 설정
                   /* Tv_test.setText(result.toString());*/

                    // BarChart 데이터 설정
                    List<BarEntry> entries = new ArrayList<>();
                    entries.add(new BarEntry(1, stepsMap.getOrDefault(formattedThreeDaysAgo, 100))); // 3일 전
                    entries.add(new BarEntry(2, stepsMap.getOrDefault(formattedTwoDaysAgo, 200)));   // 2일 전
                    entries.add(new BarEntry(3, stepsMap.getOrDefault(formattedYesterday, 150)));    // 1일 전

                    BarDataSet dataSet = new BarDataSet(entries, "");
                    //dataSet.setDrawValues(false); // 바에 값 표시 비활성화
                    //dataSet.setColors(ColorTemplate.COLOR_NONE); // ColorTemplate.LIBERTY_COLORS, ColorTemplate.PASTEL_COLORS 등도 사용 가능

                    dataSet.setColors(Color.rgb(134, 168, 231));

                    dataSet.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return (String.valueOf((int) value)) ;
                        }
                    });

                    BarData barData = new BarData(dataSet);
                    barData.setBarWidth(0.5f);
                    barData.setValueTextSize(15);
                    barChart.setData(barData);
                    barChart.setFitBars(true); // 막대가 차트 내에 맞도록 설정
                    barChart.getDescription().setEnabled(false); // Description Label 비활성화

                    XAxis xAxis = barChart.getXAxis();
                    YAxis axisLeft = barChart.getAxisLeft();
                    axisLeft.setEnabled(false); // 좌측 Y축 레이블 비활성화
                    YAxis axisRight = barChart.getAxisRight();
                    axisRight.setEnabled(false); // 좌측 Y축 레이블 비활성화
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축을 하단에 위치시킴
                    xAxis.setGranularity(1f); // X축 레이블의 간격을 설정
                    xAxis.setLabelCount(3); // 레이블의 개수를 설정

                    xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"", "3일전", "2일전", "1일전"}));
                    xAxis.setTextSize(20f); // 크기 조절


                    axisLeft.setTextSize(12f); // 레이블의 크기 조절
                    axisLeft.setYOffset(-20f); // 레이블의 Y축 오프셋 조절

                    //axisLeft.setDrawLabels(false); // label 삭제

                    axisLeft.setDrawGridLines(false);
                    axisLeft.setDrawAxisLine(false);

                    axisRight.setDrawGridLines(false);
                    axisRight.setDrawAxisLine(false);

                    // x축 선 설정 (default = true)
                    xAxis.setDrawGridLines(false);

                    // 격자선 설정 (default = true)
                    xAxis.setDrawGridLines(false);


                    barChart.invalidate(); // 새로고침

                    // 확대 및 드래그 비활성화
                    barChart.setScaleEnabled(false);
                    barChart.setDragEnabled(false);

                } else {
                    // 요청 실패 처리
                    //Tv_test.setText("API 요청 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CalculateRequest>> call, Throwable t) {
                // 네트워크 오류 또는 기타 이유로 인해 요청 실패
                //Tv_test.setText("API 요청 실패: " + t.getMessage());
            }
        });


        /*List<BarEntry> entries = new ArrayList<>();

        // DB 데이터에서 입력해야함!! (3일간의 걸음 수)
        entries.add(new BarEntry(1, 3000)); // Day -3, 3000 steps
        entries.add(new BarEntry(2, 4500)); // Day -2, 4500 steps
        entries.add(new BarEntry(3, 5000)); // Day -1, 5000 steps


        *//*entries.add(new BarEntry(4, 7000)); // Day 4, 7000 steps
        entries.add(new BarEntry(5, 10000)); // Day 5, 10000 steps
        entries.add(new BarEntry(6, 8500)); // Day 6, 8500 steps
        entries.add(new BarEntry(7, 7500)); // Day 7, 7500 steps*//*

        BarDataSet dataSet = new BarDataSet(entries, "걸음 수");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate(); // 새로고침*/




        try {
            // 블루투스 권한 확인
            if (ContextCompat.checkSelfPermission(getContext(), BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없는 경우 처리
                // 여기서는 권한이 없다는 메시지를 출력하고 진행

                PermissionListener permissionlistener = new PermissionListener() {
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
                        .setPermissions(BLUETOOTH_CONNECT,ACCESS_FINE_LOCATION,
                                ACCESS_COARSE_LOCATION,
                                ACTIVITY_RECOGNITION,
                                REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                POST_NOTIFICATIONS)
                        .check();

                Toast.makeText(getContext(), "Bluetooth Connect permission not granted", Toast.LENGTH_SHORT).show();
            } else {
                // 권한이 있는 경우 또는 없어도 진행해야 하는 작업들을 여기에 추가
                if (!bt.isBluetoothEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
                } else {
                    if (!bt.isServiceAvailable()) {
                        bt.setupService();
                        bt.startService(BluetoothState.DEVICE_OTHER);
                        setup();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "!@@ " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        /*FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();*/

        /*// Google Fit 리스너 등록
        if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(requireContext()), fitnessOptions)) {
            registerFitnessDataListener(DataType.TYPE_STEP_COUNT_DELTA);
            //registerFitnessDataListener(DataType.TYPE_DISTANCE_DELTA);
        } else {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(requireContext()),
                    fitnessOptions);
        }*/
    }
    private void setup() {
        //btnSend.setOnClickListener(v -> bt.send("Text", true));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                bt.connect(data);
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getContext(), "Bluetooth was not enabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    // 칼로리 계산
    private String calculateCaloriesBurned(float distanceInMeters, float bodyWeightKg) {
        // 거리 (미터)를 마일로 변환
        float distanceInMiles = distanceInMeters / 1609.344f;

        // 체중을 파운드로 변환
        float bodyWeightPounds = bodyWeightKg * KILOGRAMS_TO_POUNDS;

        // 소비되는 칼로리 계산
        float caloriesBurned = METRIC_RUNNING_FACTOR * bodyWeightKg * distanceInMeters / 1000;

        // 텍스트뷰에 출력
        String formattedCalories = String.format("%.2f", caloriesBurned);
        caloriesBurnedView.setText(formattedCalories);

        return formattedCalories;
    }



}
