package com.cookandroid.tab2;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACTIVITY_RECOGNITION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextHeight;
    private EditText editTextWeight;
    private EditText editTextGoalWeight;
    private Button buttonStart;

    private CalendarView calendarView;

    private String selectedDate;

    public SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        editTextName = findViewById(R.id.editTextName);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextGoalWeight = findViewById(R.id.editTextGoal);
        buttonStart = findViewById(R.id.buttonStart);
        calendarView = findViewById(R.id.calendarView);

        prefs = getSharedPreferences("Pref", MODE_PRIVATE);

        boolean isFirstRun = prefs.getBoolean("isFirstRun",true);
        if(isFirstRun) {
            Log.d("FirstRun", "앱이 처음 실행되었습니다.");
            Log.d("FirstRun", "앱이 처음 실행되었습니다.");
            Log.d("FirstRun", "앱이 처음 실행되었습니다.");


            PermissionListener permissionlistener = new PermissionListener() {
                @SuppressLint("BatteryLife")
                @Override
                public void onPermissionGranted() {
                    Toast.makeText(InputActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    //prefs.edit().putBoolean("isFirstRun",false).apply();
                    
                    // 배터리 최적화 중지
                    Intent i = new Intent();
                    String packageName = getPackageName();
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

                    if(pm.isIgnoringBatteryOptimizations(packageName)){
                        i.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    } else {
                        i.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        i.setData(Uri.parse("package:" + packageName));
                        startActivity(i);
                    }
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



            /*Intent newIntent = new Intent(MapsActivity.this, GuideActivity.class);
            startActivity(newIntent);
            prefs.edit().putBoolean("isFirstRun",false).apply();*/


            //prefs.edit().putBoolean("isFirstRun", false).apply();


        }else{
            Log.d("NotFirstRun", "이미 앱이 실행되었습니다.");
            Log.d("NotFirstRun", "이미 앱이 실행되었습니다.");
            Log.d("NotFirstRun", "이미 앱이 실행되었습니다.");

            //ViewPager2 viewPager = findViewById(R.id.view_pager);
            //viewPager.setCurrentItem(0);



            Intent intent = new Intent(InputActivity.this, MainActivity.class);
            startActivity(intent);

            //finish();




           /* // Home 프래그먼트를 생성하고 트랜잭션을 통해 화면에 표시
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Home homeFragment = new Home();
            fragmentTransaction.replace(R.id.nav_home, homeFragment); // R.id.fragment_container는 프래그먼트가 표시될 레이아웃의 ID입니다.
            fragmentTransaction.addToBackStack(null); // 뒤로 가기 버튼을 눌렀을 때 이전 상태로 되돌아가도록 스택에 추가
            fragmentTransaction.commit();*/
        }


        // 캘린더 뷰에서 날짜 선택 리스너 설정
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            }
        });

        // 사용자가 날짜를 선택하지 않았을 때 기본값으로 오늘 날짜를 설정
        if (selectedDate == null) {
            // 오늘 날짜를 가져옴
            Calendar defaultDate = Calendar.getInstance();
            Date today = defaultDate.getTime();

            // 오늘 날짜를 문자열로 변환하여 selectedDate에 저장
            selectedDate = DateFormat.getDateInstance().format(today);
        }

        // EditText의 TextWatcher 설정
        editTextHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 텍스트 변경 전에 실행할 작업
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트가 변경될 때마다 실행할 작업
                checkEditTextValues();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트 변경 후에 실행할 작업
            }
        });

        editTextWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkEditTextValues();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editTextGoalWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkEditTextValues();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });



        buttonStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String height = editTextHeight.getText().toString(); // 키
                String weight = editTextWeight.getText().toString();
                String goalWeight = editTextGoalWeight.getText().toString();

                long daysDifference = calculateDaysDifference(selectedDate)+1; // 클릭한 날짜와 현재 날짜 사이의 일 수 차이 계산(오늘포함)

                Log.v("test", String.valueOf(daysDifference));
                Log.v("test", String.valueOf(daysDifference));
                Log.v("test", String.valueOf(daysDifference));

                double ww = Double.parseDouble(weight) - Double.parseDouble(goalWeight); // 빼야하는 몸무게(kg)

                Log.d("#@@@@@@@@@@@@@@@@@@@@@@@@@@@", "여기 실행됨.");

                if(ww <= 0){

                    // 경고 대화 상자 띄우기
                    AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
                    builder.setTitle("알림"); // 다이얼로그 제목 설정
                    builder.setMessage("목표 몸무게를 확인해주세요."); // 다이얼로그 내용 설정

                    // 확인 버튼 클릭 리스너 설정
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 확인 버튼을 클릭하면 수행할 작업
                        }
                    });

                    // 다이얼로그 표시
                    builder.create().show();
                    return;


                }


                Log.v("빼야하는 Kg!@!@!@", String.valueOf(ww));

                double goal = (ww*70000.0)/Double.parseDouble(String.valueOf(daysDifference));

                Log.v("목표 걸음수!@!@!@", String.valueOf(goal));


                // SharedPreferences 객체 생성
                SharedPreferences.Editor editor = prefs.edit();

                // 데이터 저장
                editor.putString("닉네임", name);
                editor.putString("키", height);
                editor.putString("몸무게", weight);
                editor.putString("목표몸무게", goalWeight);
                editor.putString("일수", String.valueOf(daysDifference));
                editor.putString("목표걸음수", String.valueOf(goal));

                editor.apply();



                //DB 저장 !@!@@!@@!@!!@     원래 값이 있으면 그 값을 지우고 다시 덮어쓰기
                int date = (int) daysDifference;
                int goalWalk = (int) goal;
                UserRequest request = new UserRequest(name,Integer.parseInt(height),Integer.parseInt(weight),Integer.parseInt(goalWeight),date,goalWalk);

                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Call<Void> call = apiService.saveUser(request);

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(InputActivity.this, "데이터 저장 성공!", Toast.LENGTH_SHORT).show();
                        } else {
                            // 서버 응답 코드 및 오류 메시지 출력
                            Toast.makeText(InputActivity.this, "데이터 저장 실패: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(InputActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("API Call Failure", "Error: " + t.getMessage());
                    }
                });
//                call.enqueue(new Callback<Void>() {
//                    @Override
//                    public void onResponse(Call<Void> call, Response<Void> response) {
//                        if (response.isSuccessful()) {
//                            Toast.makeText(InputActivity.this, "데이터 저장 성공!", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // 서버 응답 코드 및 오류 메시지 출력
//                            Toast.makeText(InputActivity.this, "데이터 저장 실패: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
//                            Log.e("API Error", "Response Code: " + response.code() + ", Message: " + response.message());
//                            try {
//                                Log.e("API Error Body", "Error Body: " + response.errorBody().string());
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
////                    @Override
////                    public void onResponse(Call<Void> call, Response<Void> response) {
////                        if (response.isSuccessful()) {
////                            Toast.makeText(InputActivity.this, "데이터 저장 성공!", Toast.LENGTH_SHORT).show();
////                        } else {
////                            Toast.makeText(InputActivity.this, "데이터 저장 실패", Toast.LENGTH_SHORT).show();
////                        }
////                    }
//                    @Override
//                    public void onFailure(Call<Void> call, Throwable t) {
//                        Toast.makeText(InputActivity.this, "에러: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

                // 변경사항 저장
                /*editor.apply();*/

                //String weight222 = prefs.getString("몸무게", "");

                //Log.v("불러온 값!@!@!@", weight222);


                Intent intent = new Intent(InputActivity.this, MainActivity.class);
                intent.putExtra("닉네임", name);
                intent.putExtra("키", height);
                intent.putExtra("몸무게", weight);
                intent.putExtra("목표몸무게", goalWeight);
                intent.putExtra("일수", String.valueOf(daysDifference));
                intent.putExtra("kg", String.valueOf(goal));
                startActivity(intent);



                finish();
            }
        });





    }

    // EditText 값 검사 및 버튼 상태 변경 메서드
    private void checkEditTextValues() {
        String height = editTextHeight.getText().toString();
        String weight = editTextWeight.getText().toString();
        String goalWeight = editTextGoalWeight.getText().toString();

        // EditText 값이 비어있는지 확인하여 버튼 상태 변경
        if (height.trim().isEmpty() || weight.trim().isEmpty() || goalWeight.trim().isEmpty()) {
            buttonStart.setEnabled(false);
        } else {
            buttonStart.setEnabled(true);
        }


    }

    private long calculateDaysDifference(String selectedDateStr) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date selectedDate = sdf.parse(selectedDateStr);

            Calendar calendar = Calendar.getInstance();
            Date currentDate = sdf.parse(sdf.format(calendar.getTime()));

            long differenceMillis = selectedDate.getTime() - currentDate.getTime();
            return differenceMillis / (1000 * 60 * 60 * 24); // 밀리초를 일로 변환하여 반환
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }




    }









}

