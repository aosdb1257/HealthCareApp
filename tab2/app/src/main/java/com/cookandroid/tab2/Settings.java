package com.cookandroid.tab2;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Settings extends Fragment {
    private Button buttonReset;
    private Button btnTest;

    private TextView tv_nickname;
    private TextView tv_height;
    private TextView tv_weight;
    private TextView tv_goal_weight;
    private TextView tv_days_left;

    public SharedPreferences prefs;


    private static final String FIRST_RUN_KEY = "isFirstRun";



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_layout, container, false);

        // 텍스트뷰 초기화
        tv_nickname = view.findViewById(R.id.tv_nickname);
        tv_height = view.findViewById(R.id.tv_height);
        tv_weight = view.findViewById(R.id.tv_weight);
        tv_goal_weight = view.findViewById(R.id.tv_goal_weight);
        tv_days_left = view.findViewById(R.id.tv_days_left);

        prefs = requireContext().getSharedPreferences("Pref", MODE_PRIVATE);



        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        String nickName = prefs.getString("닉네임", "0");


        Call<UserRequest> call = apiService.getUserByName(nickName);
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


                        // 초기 데이터 설정
                        updateTextViews(user.getName(), user.getHeight(), user.getWeight(),
                                user.getGoalWeight(), user.getDate());


                        // 결과를 TextView에 설정
                        //Tv_test2.setText(result.toString());
                    } else {
                        //Tv_test2.setText("No user found with name: " + name);
                    }
                } else {
                    // 요청 실패 처리
                    //Tv_test2.setText("API 요청 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserRequest> call, Throwable t) {
                // 네트워크 오류 또는 기타 이유로 인해 요청 실패
                //Tv_test2.setText("API 요청 실패: " + t.getMessage());
            }
        });




        buttonReset = view.findViewById(R.id.buttonReset);
        // 버튼 클릭 시 입력 화면 불러오기
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SharedPreferences에서 FirstRun 플래그를 true로 설정하여 다시 실행되도록 합니다.
                SharedPreferences settings = requireActivity().getSharedPreferences("Pref", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(FIRST_RUN_KEY, true);
                editor.apply();



                Intent resetIntent = new Intent(getActivity(), InputActivity.class);
                startActivity(resetIntent);
            }
        });

        /*// db api 테스트를 위한 삭제해도됨
        btnTest = view.findViewById(R.id.btn_test);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TestActivity.class);
                startActivity(intent);
            }
        });*/
        return  view;

    }

    private void updateTextViews(String nickname, int height, int weight, int goalWeight, int daysLeft) {
        tv_nickname.setText("닉네임: " + nickname);
        tv_height.setText("키: " + height + "cm");
        tv_weight.setText("몸무게: " + weight + "kg");
        tv_goal_weight.setText("목표 몸무게: " + goalWeight + "kg");
        tv_days_left.setText("남은 기간: " + daysLeft + "일");
    }
}