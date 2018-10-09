package com.crm.finance.broadcast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.crm.finance.app.MyApplication;

public class BroadcastManager {
    //发送广播
    public static void sendShowTopRankData(String broType) {
        Intent mIntent = new Intent();
        mIntent.setAction(broType);
        LocalBroadcastManager.getInstance(MyApplication.getAPP())
                .sendBroadcast(mIntent);
    }

    public static void sendShowTopRankData(Intent mIntent,String broType) {
        mIntent.setAction(broType);
        LocalBroadcastManager.getInstance(MyApplication.getAPP())
                .sendBroadcast(mIntent);
    }


    //注销广播
    public static void unregisterReceiver(BroadcastReceiver mReceiver) {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(
                    MyApplication.getAPP()).unregisterReceiver(
                    mReceiver);
        }
    }

    //注册广播
    public static  void registerReceiver(BroadcastReceiver mReceiver, IntentFilter filter) {
        LocalBroadcastManager.getInstance(MyApplication.getAPP())
                .registerReceiver(mReceiver, filter);
    }
}
