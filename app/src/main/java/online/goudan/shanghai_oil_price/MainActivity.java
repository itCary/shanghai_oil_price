package online.goudan.shanghai_oil_price;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;
import online.goudan.shanghai_oil_price.bean.City;
import online.goudan.shanghai_oil_price.bean.Province;
import online.goudan.shanghai_oil_price.utils.AndroidScheduler;
import online.goudan.shanghai_oil_price.utils.Common;
import online.goudan.shanghai_oil_price.utils.HttpUtils;
import online.goudan.shanghai_oil_price.utils.InitUils;

public class MainActivity extends BaseActivity {
    List<String> mPermissionList = new ArrayList<>();
    @BindView(R.id.btn_request_http)
    Button btnRequestHttp;
    @BindView(R.id.tv_show_data)
    TextView tvShowData;

    HttpUtils httpUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestPermission();
        httpUtils = new HttpUtils();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                boolean success = false;
                if (grantResults.length > 0) {
                    for (int i : grantResults) {
                        if (i == PackageManager.PERMISSION_GRANTED) {
                            success = true;
                        } else {
                            success = false;
                            break;
                        }

                    }
                }
                break;


        }
    }

    @OnClick(R.id.btn_request_http)
    public void onViewClicked() {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("请求数据");
        dialog.setMessage("正在联网。。。");
        dialog.setCancelable(true);
        dialog.show();

        Observable.create(emitter -> {
            List<Province> provinceList = DataSupport.findAll(Province.class);
            List<City> cityList = DataSupport.findAll(City.class);
            emitter.onNext(String.valueOf(provinceList) + String.valueOf(cityList));
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidScheduler.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull Object o) {
                        dialog.hide();
                        tvShowData.setText(String.valueOf(o));
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
