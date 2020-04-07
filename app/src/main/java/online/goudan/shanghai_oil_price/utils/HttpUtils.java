package online.goudan.shanghai_oil_price.utils;

import android.app.DownloadManager;
import android.util.Log;

import java.io.IOException;

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
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            Log.i(TAG, "doGet: " + response.code());
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";


    }

}
