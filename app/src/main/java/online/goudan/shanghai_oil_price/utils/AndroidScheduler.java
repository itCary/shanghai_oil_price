package online.goudan.shanghai_oil_price.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author 刘成龙
 * @date 2020/4/7
 */
public class AndroidScheduler implements Executor {
    private static AndroidScheduler instance;
    private final Scheduler mMainScheduler;
    private final Handler mHandler;

    private AndroidScheduler() {
        mHandler = new Handler(Looper.myLooper());
        mMainScheduler = Schedulers.from(this);
    }

    public static synchronized Scheduler mainThread() {
        if (instance == null) {
            instance = new AndroidScheduler();
        }
        return instance.mMainScheduler;
    }

    public void execute(@NonNull Runnable command) {
        mHandler.post(command);
    }
}
