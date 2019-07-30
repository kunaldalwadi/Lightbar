package com.accutime.lightbar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    public Api apiobj;
    private static final String TAG = "MainActivity";
    Context context = MainActivity.this;
    private JSONObject postData;
    Button mBtnGo;
    TextView mHexCode;
    int mDefaultColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDefaultColor = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);
        mBtnGo = findViewById(R.id.btn_go);
        mHexCode = findViewById(R.id.tv_hex_code);

        apiobj = createApiObject();


        mBtnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });
    }


    public void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Toast.makeText(context, "Color not selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor = (color);
                String s1 = Integer.toHexString(color);
                String substring = s1.substring(2);
                Log.d(TAG, "onOk: " + color);
                Log.d(TAG, "onOk toHEXString --------: " + s1);
                Log.d(TAG, "onOk toHEXString substringgggg ------------: " + substring);
                mHexCode.setText(substring);
                sendCall(substring);

            }
        });
        colorPicker.show();
    }

    public void sendCall(String substring) {

        // Make sure these are lowercase!
        final String mode = "on";
        final String colour = "none";
        final String brightness = "100";
        final String rgbvalue = substring;

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonPostData = createJsonPostData(colour, mode, rgbvalue, brightness);
                setLightbar(jsonPostData);
            }
        }).start();




    }

    private JSONObject createJsonPostData(String colour, String mode, String rgbvalue, String
            brightness) {

        JSONObject jsonPostData = new JSONObject();

        Log.d(TAG, "mode: " + mode);
        Log.d(TAG, "colour: " + colour);
        Log.d(TAG, "brightness: " + brightness);
        Log.d(TAG, "rgbvalue: " + rgbvalue);
        try {
            // Make sure these are lowercase!

            if (!mode.isEmpty()) {
                jsonPostData.put("mode", mode.toLowerCase());
            }
            if (!colour.isEmpty()) {
                jsonPostData.put("colour", colour.toLowerCase());
            }
            if (!brightness.isEmpty()) {
                jsonPostData.put("brightness", brightness.toLowerCase());
            }
            if (!rgbvalue.isEmpty()) {
                jsonPostData.put("rgbvalue", rgbvalue.toLowerCase());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error adding the data.");
        }

        return jsonPostData;
    }

    public void setLightbar(final JSONObject jsonParams) {
        Log.i(TAG, "setLightbar: " + jsonParams);

        apiobj.getLightBarColor(jsonParams).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                    Log.i(TAG, "Success: " + jsonParams.toString());

                } else {

                    try {

                        showFailureMessage(response.errorBody().string());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showFailureMessage(t.getMessage());
            }

            private void showFailureMessage(String message) {
                Log.e(TAG, "Failure: \n" + message);
            }
        });
    }


    /**
     * Create a static API using retrofit, which is included in build.gradle
     * RetrofitClient creates a usable object out of this interface
     */
    public Api createApiObject() {
        Gson gson = new GsonBuilder().create();

        OkHttpClient okHttpClient = getOkHttpClient();

        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(Api.BASE_URL_SECURE_HTTPS)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        return retrofit.create(Api.class);
    }

    /**
     * Create an OkHTTPClient for retrofit to use
     * Add certificates to allow for HTTPS
     */
    private OkHttpClient getOkHttpClient() {

        try {
            /*
             * Create a trust manager that does not validate certificate chains.
             */
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }};

            /*
             * Install the trust manager
             */
            final SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, trustAllCerts,
                    new java.security.SecureRandom());

            /*
             * Create an ssl socket factory with our manager
             */
            final SSLSocketFactory sslSocketFactory = sslContext
                    .getSocketFactory();

            return new OkHttpClient()
                    .newBuilder()
                    .readTimeout(20000, TimeUnit.MILLISECONDS)
                    .writeTimeout(20000, TimeUnit.MILLISECONDS)
                    .sslSocketFactory(sslSocketFactory)
                    .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
