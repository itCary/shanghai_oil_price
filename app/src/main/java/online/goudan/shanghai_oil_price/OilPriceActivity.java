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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
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
import online.goudan.shanghai_oil_price.bean.Province;
import online.goudan.shanghai_oil_price.utils.AndroidScheduler;
import online.goudan.shanghai_oil_price.utils.HttpUtils;
import online.goudan.shanghai_oil_price.utils.InitUils;

public class OilPriceActivity extends BaseActivity {
    List<String> mPermissionList = new ArrayList<>();
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
    @BindView(R.id.tv_city_discount)
    TextView tvCityDiscount;
    @BindView(R.id.btn_previous)
    Button btnPrevious;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.ll_city_station_list)
    LinearLayout llCityStationList;
    @BindView(R.id.tv_show_index_and_total)
    TextView tvShowIndexAndTotal;

    private String oilCity;

    private int index = 1;
    private int total;

    private City currentCity;

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
        String oilStr = preferences.getString("layout_show_oil_price", null);
        if (oilStr != null) {
            parseDataAndShow(oilStr);
            //软件启动时获取当前城市的省份
            Observable.create(emitter -> {
                City city = DataSupport.where("cityName=?", oilCity).findFirst(City.class);
                Province province = DataSupport.where("id=?", String.valueOf(city.getProvincedId())).findFirst(Province.class);
                emitter.onNext(province);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidScheduler.mainThread())
                    .subscribe(o -> {
                        List<Fragment> fragments = getSupportFragmentManager().getFragments();
                        ChooseAreaFragment fragment = (ChooseAreaFragment) fragments.get(0);
                        fragment.setCurrentLevel((Province) o);
                    });
        } else {

            drawerLayout.openDrawer(GravityCompat.START);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (oilCity != null) {
                    loadOilPrice(oilCity);
                } else {
                    Toast.makeText(mContext, "还没有选择城市", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    /**
     * 从网络加载城市的油价
     *
     * @param cityName
     */
    public void loadOilPrice(String cityName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Observable.create(emitter -> {
            City city = DataSupport.where("cityName=?", cityName).findFirst(City.class);
            if (city == null) {
                new InitUils().initProvince();
                city = DataSupport.where("cityName=?", cityName).findFirst(City.class);
            }
            String city_oil = httpUtils.doGet(city.getHref());
            if (city.getDisHref() == null) {
                Document cityDocument = Jsoup.parse(city_oil);
                String tempHref = cityDocument.select("div.col-xs-12 div.text-center a.btn").attr("href");
                String disHref = "http://www.xiaoxiongyouhao.com/" + tempHref;
                city.setDisHref(disHref);
                city.save();
            }
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("layout_show_oil_price", city_oil);
            edit.apply();
            emitter.onNext(city_oil);

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidScheduler.mainThread())
                .subscribe(o -> {
                    swipeRefresh.setRefreshing(false);
                    parseDataAndShow((String) o);
                });
    }

    public void loadOilDiscount(String cityName, int page) {
        Log.i(TAG, "loadOilDiscount: cityName:" + cityName + ",page:" + page);
        if (page <= 1) {
            index = 1;
            btnPrevious.setEnabled(false);
        } else {
            btnPrevious.setEnabled(true);
        }
        if (total > 0 && page >= total) {
            index = total;
            btnNext.setEnabled(false);
        } else {
            btnNext.setEnabled(true);
        }
        Observable.create(emitter -> {
            City city = DataSupport.where("cityName=?", cityName).findFirst(City.class);
            if (city != null) {
                String cityOilDiscountHtml = httpUtils.doGet(city.getDisHref() + "&page=" + page);
                emitter.onNext(cityOilDiscountHtml);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidScheduler.mainThread())
                .subscribe(o -> {
                    parseCityOilDiscount(String.valueOf(o));
                });


    }

    private void parseCityOilDiscount(String html) {
        Document document = Jsoup.parse(html);
        Elements trs = document.select("table.table tbody tr");
        tvCityDiscount.setText(document.select("h4").text());
        String text = document.select("div.container div.col-xs-12 div.text-center span").text();
        String split = text.split("，")[1];
        String totalStr = split.substring(1, split.length() - 1);
        total = Integer.parseInt(totalStr);
        tvShowIndexAndTotal.setText("" + index + "/" + total);
        llCityStationList.removeAllViews();
        for (int i = 1; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            String rank = tds.get(0).text();
            String district = tds.get(1).text();
            String gasName = tds.get(2).text();
            String personTime = tds.get(3).text();
            String oilPrice = tds.get(4).text();

            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_discount_item, llCityStationList, false);

            TextView tvRank = view.findViewById(R.id.tv_rank);
            TextView tvDistrict = view.findViewById(R.id.tv_district);
            TextView tvGasName = view.findViewById(R.id.tv_gas_name);
            TextView tvPersonTime = view.findViewById(R.id.tv_person_time);
            TextView tvOilPrice = view.findViewById(R.id.tv_oil_price);
            tvRank.setText(rank);
            tvDistrict.setText(district);
            tvGasName.setText(gasName);
            tvPersonTime.setText(personTime);
            tvOilPrice.setText(oilPrice);
            llCityStationList.addView(view);
        }
    }


    /**
     * 解析城市油价
     *
     * @param oilStr
     */
    private void parseDataAndShow(String oilStr) {
        index = 1;
        Document document = Jsoup.parse(oilStr);
        Elements oilS = document.select("#city-price-table tbody tr");

        String cityName = document.select("h3.text-center").get(0).text();
        oilCity = cityName.substring(0, cityName.length() - 2);
        loadOilDiscount(oilCity, index);
        titleCity.setText(oilCity);
        String timeString = document.select("h5.text-center").get(1).text();

        titleUpdateTime.setText(timeString.substring(5));
        llOilPrice.removeAllViews();
        for (int i = 1; i < oilS.size(); i++) {
            Element element = oilS.get(i);
            Elements tds = element.select("td");
            String oilType = tds.get(0).text();
            String hPrice = tds.get(1).text();
            String dPrice = tds.get(2).text();

            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_show_oil_price_item, llOilPrice, false);
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
     * 加载背景图
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
            } catch (JsonSyntaxException e) {
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


    @OnClick({R.id.nav_button, R.id.btn_previous, R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.nav_button:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.btn_previous:
                loadOilDiscount(oilCity, --index);
                break;
            case R.id.btn_next:
                loadOilDiscount(oilCity, ++index);
                break;
        }
    }
}
