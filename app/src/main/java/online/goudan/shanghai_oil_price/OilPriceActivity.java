package online.goudan.shanghai_oil_price;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import online.goudan.shanghai_oil_price.bean.City;
import online.goudan.shanghai_oil_price.bean.ImageBean;
import online.goudan.shanghai_oil_price.bean.WomanPic;
import online.goudan.shanghai_oil_price.utils.AndroidScheduler;
import online.goudan.shanghai_oil_price.utils.HttpUtils;
import online.goudan.shanghai_oil_price.utils.InitUils;

public class OilPriceActivity extends BaseActivity {
    List<String> mPermissionList = new ArrayList<>();
    Button btnRequestHttp;
    HttpUtils httpUtils;
    @BindView(R.id.iv_background)
    ImageView ivBackground;
    @BindView(R.id.nav_button)
    Button navButton;
    @BindView(R.id.title_city)
    TextView titleCity;
    @BindView(R.id.title_update_time)
    TextView titleUpdateTime;
    @BindView(R.id.ll_oil_price)
    LinearLayout llOilPrice;
    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private String oilCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oil_price);
        ButterKnife.bind(this);
        requestPermission();
        httpUtils = new HttpUtils();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //加载背景图片
        loadBackgroudnPic();
        //加载油价
        String oilStr = preferences.getString("oil_price", null);
        if (oilStr != null) {
            parseDataAndShow(oilStr);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadOilPrice(oilCity);
            }
        });
    }

    public void loadOilPrice(String cityName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Observable.create(emitter -> {
            City city = DataSupport.where("cityName=?", cityName).findFirst(City.class);
            if (city == null) {
                new InitUils().initProvince();
                city = DataSupport.where("cityName=?", cityName).findFirst(City.class);
            }
            String city_oil = httpUtils.doGet(city.getHref());
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("oil_price", city_oil);
            edit.apply();
            emitter.onNext(city_oil);

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidScheduler.mainThread())
                .subscribe(o -> {
                    swipeRefresh.setRefreshing(false);
                    parseDataAndShow((String) o);
                });
    }


    private void parseDataAndShow(String oilStr) {
        Document document = Jsoup.parse(oilStr);
        Elements oilS = document.select("#city-price-table tbody tr");

        String cityName = document.select("h3.text-center").get(0).text();
        oilCity = cityName.substring(0, cityName.length() - 2);
        titleCity.setText(oilCity);

        titleUpdateTime.setText(document.select("h5.text-center").get(1).text());
        llOilPrice.removeAllViews();
        for (int i = 1; i < oilS.size(); i++) {
            Element element = oilS.get(i);
            Elements tds = element.select("td");
            String oilType = tds.get(0).text();
            String hPrice = tds.get(1).text();
            String dPrice = tds.get(2).text();

            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_textview_item, llOilPrice, false);
            TextView tvOilType = view.findViewById(R.id.tv_oil_type);
            TextView tvHPrice = view.findViewById(R.id.tv_h_price);
            TextView tvDPrice = view.findViewById(R.id.tv_d_price);
            tvOilType.setText(oilType);
            tvHPrice.setText(hPrice);
            tvDPrice.setText(dPrice);
            llOilPrice.addView(view);
        }
    }

    /**
     * 加载必应每日一图
     */
    public void loadBackgroudnPic() {

        Observable.create(emitter -> {
            int start = new Random(System.currentTimeMillis()).nextInt(7088);
            String requestPic_url = "http://lab.mkblog.cn/wallpaper/api.php?cid=6&start=" + start + "&count=100";
            String pic_josn = httpUtils.doGet(requestPic_url);
            Gson gson = new Gson();
            try {
                ImageBean images = gson.fromJson(pic_josn, ImageBean.class);
                if (images != null && images.getData() != null) {
                    List<ImageBean.DataBean> imageList = images.getData();
                    ImageBean.DataBean dataBean = imageList.get(new Random(System.currentTimeMillis()).nextInt(imageList.size()));
                    emitter.onNext(dataBean.getImg_1280_1024());
                } else {
                    emitter.onNext("http://image.baidu.com/search/down?tn=download&word=download&ie=utf8&fr=detail&url=http://p5.qhimg.com/bdm/2560_1600_100/t01a7117bbc9683a7eb.jpg");
                }
            }catch (JsonSyntaxException e){
                emitter.onNext("http://image.baidu.com/search/down?tn=download&word=download&ie=utf8&fr=detail&url=http://p5.qhimg.com/bdm/2560_1600_100/t01a7117bbc9683a7eb.jpg");
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidScheduler.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        Glide.with(mContext).load((String) o).into(ivBackground);
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestPermission();
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }
        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @OnClick(R.id.nav_button)
    public void onViewClicked() {
        drawerLayout.openDrawer(GravityCompat.START);
    }
}
