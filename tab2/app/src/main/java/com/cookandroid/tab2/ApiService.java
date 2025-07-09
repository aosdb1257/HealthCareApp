package com.cookandroid.tab2;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/user")
    Call<Void> saveUser(@Body UserRequest request);
    @POST("/calculate")
    Call<Void> saveCalculate(@Body CalculateRequest calculateRequest);
    @GET("/calculate2")
    Call<List<CalculateRequest>> getAllCalculates();
    @GET("/user/{name}")
    Call<UserRequest> getUserByName(@Path("name") String name);
}
