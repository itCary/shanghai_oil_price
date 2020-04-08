package online.goudan.shanghai_oil_price.utils;

import android.util.Log;


import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 刘成龙
 * @date 2020/4/7
 */
public class HttpUtils {

    private String TAG = "HttpUtils";
    private OkHttpClient client = new OkHttpClient();

    public String doGet(String url) {
        Log.i(TAG, "doGet: url" +url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String responseStr = null;
        try {
            responseStr = client.newCall(request).execute().body().string();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;


    }


}
