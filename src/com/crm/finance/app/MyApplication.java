package com.crm.finance.app;

import android.app.Application;

/**
 * Created by Administrator on 2018/9/12 0012.
 */

public class MyApplication  extends Application {
    private static MyApplication app;

    public static MyApplication getAPP() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
