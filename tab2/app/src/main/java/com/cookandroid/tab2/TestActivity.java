package com.cookandroid.tab2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {
    private Button Btn_savetest; // 총걸음수 db에 저장 버튼 저장 날짜를 pk로 해서 한번만 저장 가능함
    private Button Btn_gettest; // 총걸음수,이름 출력 버튼
    private Button Btn_gettest2; // 유저(키,몸무게,닉네임등) 출력 버튼
    private TextView Tv_test;
    private TextView Tv_test2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Btn_savetest = findViewById(R.id.btn_savetest);
        Btn_gettest = findViewById(R.id.btn_gettest);
        Btn_gettest2 = findViewById(R.id.btn_gettest2);
        Tv_test = findViewById(R.id.tv_test);
        Tv_test2 = findViewById(R.id.tv_test2);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Btn_savetest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 임의의 수, 이를 알맞은 값으로 대체 해야함
                int totalStep = 15;
                String name = "김길동";

                // db에 총걸음수 저장하는 코드
                CalculateRequest request = new CalculateRequest(totalStep, name);
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




            }
        });
        Btn_gettest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 총걸음수 출력하는 코드
                Call<List<CalculateRequest>> cal = apiService.getAllCalculates();
                cal.enqueue(new Callback<List<CalculateRequest>>() {
                    @Override
                    public void onResponse(Call<List<CalculateRequest>> call, Response<List<CalculateRequest>> response) {
                        if (response.isSuccessful()) {
                            List<CalculateRequest> calculates = response.body();
                            StringBuilder result = new StringBuilder();
                            for (CalculateRequest calculate : calculates) {
                                // 가져온 Calculate 데이터 처리
                                int totalStep = calculate.getTotalStep();
                                String name = calculate.getName();
                                int day = calculate.getDay();
                                int month = calculate.getMonth();
                                int year = calculate.getYear();
                                result.append("Date: ").append(year).append("-").append(month).append("-").append(day)
                                        .append(", Total Steps: ").append(totalStep).append(", Name: ").append(name).append("\n");
                            }
                            // 결과를 TextView에 설정
                            Tv_test.setText(result.toString());
                        } else {
                            // 요청 실패 처리
                            Tv_test.setText("API 요청 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CalculateRequest>> call, Throwable t) {
                        // 네트워크 오류 또는 기타 이유로 인해 요청 실패
                        Tv_test.setText("API 요청 실패: " + t.getMessage());
                    }
                });
            }
        });



        Btn_gettest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // name에 따른 유저 정보를 조회
                String name = "김길동"; // 조회할 유저의 이름, 지금은 임의로, DB에 김길동 없으면 오류

                Call<UserRequest> call = apiService.getUserByName(name);
                call.enqueue(new Callback<UserRequest>() {
                    @Override
                    public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                        if (response.isSuccessful()) {
                            UserRequest user = response.body();
                            if (user != null) {
                                // 유저 정보 출력
                                StringBuilder result = new StringBuilder();
                                result.append("Name: ").append(user.getName()).append("\n");
                                result.append("Height: ").append(user.getHeight()).append("\n");
                                result.append("Weight: ").append(user.getWeight()).append("\n");
                                result.append("Goal Weight: ").append(user.getGoalWeight()).append("\n");
                                result.append("Date: ").append(user.getDate()).append("\n");
                                result.append("Goal Walk: ").append(user.getGoalWalk()).append("\n");




                                // 결과를 TextView에 설정
                                Tv_test2.setText(result.toString());
                            } else {
                                Tv_test2.setText("No user found with name: " + name);
                            }
                        } else {
                            // 요청 실패 처리
                            Tv_test2.setText("API 요청 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRequest> call, Throwable t) {
                        // 네트워크 오류 또는 기타 이유로 인해 요청 실패
                        Tv_test2.setText("API 요청 실패: " + t.getMessage());
                    }
                });




            }
        });
    }
}