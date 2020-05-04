package com.example.uberclone.retrofit;

import com.example.uberclone.models.FCMBody;
import com.example.uberclone.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface iFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAQcRBmnk:APA91bHz8kf67w9zCZRUz4tSJFMRMRxwrsjsnjRlO_AzLfIhvd1qrvqe4nNxcPIbGoS2mS5VevHlowUmjhl-IqF_8TlTqBCyF8HMItmOhkAYh8djH7DgXI-FtdouRHOCZ5v95MoP-DOV"
    })

    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
