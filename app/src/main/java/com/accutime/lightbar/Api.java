package com.accutime.lightbar;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Api {

    String BASE_URL_DEFAULT_HTTP = "http://localhost";
    String BASE_URL_SECURE_HTTPS = "https://localhost";

    String RestModeDefaultHTTP = "DefaultHTTP";
    String RestModeSecureHTTPS = "SecureHTTPS";


//    @GET("/prox?mode=oneshot")
//    @Streaming
//    Call<ResponseBody> getProxStream();

    @POST("/lightbar")
    Call<ResponseBody> getLightBarColor( @Body JSONObject postData );

}
