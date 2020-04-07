package online.goudan.shanghai_oil_price.utils;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author 刘成龙
 * @date 2020/4/7
 */
public class HttpUtils {
    private RequestConfig getConfig() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(500)
                .setSocketTimeout(10000)
                .build();

        return requestConfig;
    }

    /**
     * 使用get 请求获取页面
     *
     * @param url
     * @return
     */
    public String doGet(String url) {

        try {

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }
}
