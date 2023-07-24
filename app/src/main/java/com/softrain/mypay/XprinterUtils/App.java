package com.softrain.mypay.XprinterUtils;

import android.app.Application;

import net.posprinter.POSConnect;

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        POSConnect.init(this);
    }

    public static App get() {
        return app;
    }
}