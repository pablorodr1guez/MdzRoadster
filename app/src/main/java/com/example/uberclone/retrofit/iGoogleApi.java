package com.example.uberclone.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface iGoogleApi {

    @GET
    Call<String> getDirections(@Url String url);

}
