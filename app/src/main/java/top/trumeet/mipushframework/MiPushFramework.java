package top.trumeet.mipushframework;

import android.app.Application;
import rx_activity_result2.RxActivityResult;

/**
 * Created by Trumeet on 2017/12/23.
 */

public class MiPushFramework extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void init() {
        RxActivityResult.register(this);

    }

}
