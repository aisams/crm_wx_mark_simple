
package com.crm.finance;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crm.finance.app.MyApplication;
import com.crm.finance.base.BaseActivity;
import com.crm.finance.broadcast.BroadcastManager;
import com.crm.finance.broadcast.BroadcastUtils;
import com.crm.finance.ui.HintActivity;
import com.crm.finance.ui.SetActivity;
import com.crm.finance.util.GlobalCofig;
import com.crm.finance.util.LogInputUtil;
import com.crm.finance.util.MyLog;
import com.crm.finance.util.ShareData;
import com.crm.finance.util.Utils;
import com.crm.finance.util.rootcmd.RootCmd;
import com.tencent.bugly.Bugly;

import org.apache.cordova.CordovaActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    TextView txt_state, txt_upload_time, txt_network_state, txt_ROOT_state, txt_app_version, txt_service, txt_log_state,btn_hint;
    ImageView img_logo,img_state,img_network_state,img_ROOT_state,img_service;
    Button btn_log;
    LinearLayout layout_set;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initBugly();
        initView();
        initListener();
        startTimer();
        excuteInit();
    }

    private TimerTask mTimerTask;
    private Timer mTimer = new Timer(true);

    public void startTimer() {
        if (mTimerTask != null) return;
        mTimerTask = new TimerTask() {
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };
        mTimer.schedule(mTimerTask, 0, GlobalCofig.EXECUTE_STATE_UI);//多少秒执行一次
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            initData();
        }
    };


    public void initView() {

        txt_state = (TextView) findViewById(R.id.txt_state);
        txt_upload_time = (TextView) findViewById(R.id.txt_upload_time);
        txt_network_state = (TextView) findViewById(R.id.txt_network_state);
        txt_ROOT_state = (TextView) findViewById(R.id.txt_ROOT_state);
        txt_app_version = (TextView) findViewById(R.id.txt_app_version);
        txt_service = (TextView) findViewById(R.id.txt_service);
        txt_log_state = (TextView) findViewById(R.id.txt_log_state);
        img_logo = (ImageView) findViewById(R.id.img_logo);
        layout_set = (LinearLayout) findViewById(R.id.layout_set);
        btn_log = (Button) findViewById(R.id.btn_log);
        btn_hint = (TextView) findViewById(R.id.btn_hint);

        img_state = (ImageView) findViewById(R.id.img_state);
        img_network_state = (ImageView) findViewById(R.id.img_network_state);
        img_ROOT_state = (ImageView) findViewById(R.id.img_ROOT_state);
        img_service = (ImageView) findViewById(R.id.img_service);
    }

    public void initData() {
        String defaultStr = "--";
        String lastUploadTime = ShareData.getInstance().getStringValue(this, GlobalCofig.MESSAGE_LAST_UPLOAD_TIME_ONLY, defaultStr);
        txt_upload_time.setText(lastUploadTime);

        boolean isServiceExist = Utils.isServiceRunning(this, "com.crm.finance.GohnsonService");
        setTextAndImg(txt_service,img_service,isServiceExist);

        setTextAndImg(txt_network_state,img_network_state,Utils.isNetworkAvailable(this));

        setTextAndImg(txt_state,img_state,Utils.checkUploadState());

        txt_app_version.setText(Utils.getVersionNumber(this));

        boolean isRoot = Utils.isROOT();
        isNoRootShowTip(isRoot);
        setTextAndImg(txt_ROOT_state,img_ROOT_state,isRoot);
        showLogTip();

        if (!isServiceExist && isRoot) {
            excuteInit();
        }

    }
    public void setTextAndImg(TextView textView,ImageView imageView,boolean state){
        setTextStrAndColor(textView,state);
        setImgViewBG(imageView,state);
    }
    public void setTextStrAndColor(TextView textView,boolean state){
        if(textView == null)return;
        String stateStr = "异常";
        int color = this.getResources().getColor(R.color.red);
        if(state){
            stateStr = "正常";
            color = this.getResources().getColor(R.color.btn_blue_color);
        }
        textView.setText(stateStr);
        textView.setTextColor(color);
    }
    public void setImgViewBG(ImageView imageView,boolean state){
        if(imageView == null)return;
        int color = R.drawable.err_icon;
        if(state){
            color = R.drawable.ok_icon;
        }
        imageView.setBackgroundResource(color);
    }


    public void initListener() {
        img_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isRoot = RootCmd.haveRoot();
                LogInputUtil.e(TAG, isRoot ? "已ROOT" : "未ROOT");
                initData();
            }
        });
        layout_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent service = new Intent(MainActivity.this, SetActivity.class);
                MainActivity.this.startActivity(service);
            }
        });
        btn_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent service = new Intent(MainActivity.this, UploadDataLogActivity.class);
                MainActivity.this.startActivity(service);
            }
        });
        btn_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent service = new Intent(MainActivity.this, HintActivity.class);
                MainActivity.this.startActivity(service);
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    public void excuteInit() {
        if (Utils.isROOT()) {
            excuteMain();
        }else{
            isNoRootShowTip(false);
        }
    }
    public void isNoRootShowTip(boolean root){
        if(root)return;//已获取权限不输出日志
        String noRootStr = "未获取ROOT权限";
        // LogInputUtil.showSingleTosat(this,noRootStr);
        Toast.makeText(this,noRootStr,Toast.LENGTH_SHORT);
        MyLog.inputLogToFile(TAG,noRootStr);
    }


    public void initBugly() {
        Bugly.init(getApplicationContext(), GlobalCofig.BUGLY_ID, GlobalCofig.BUGLY_ISDEBUG);
    }


    public void showLogTip() {
        boolean isNoInputFileLog = ShareData.getInstance().getBooleanValue(GlobalCofig.IS_INPUT_FILE_LOG, GlobalCofig.LOG_NO_LOG);

        String logStr = "开启中";
        if (isNoInputFileLog) {
            logStr = "关闭";
        }
        txt_log_state.setText(logStr);
    }


    public void excuteMain() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }
        //waitFor操作需开线程，不然部分机型会阻塞进程，后续代码无法执行
        new Thread(new Runnable() {
            @Override
            public void run() {
                //当前应用的代码执行目录
                if (!upgradeRootPermission(getPackageCodePath())) {
                    MyLog.inputLogToFile(TAG, "未获取到Root权限！");
                }
                ;
            }
        }).start();

        GlobalCofig.excuteGohnsonService(this);

        boolean isInputFileLog = ShareData.getInstance().getBooleanValue(GlobalCofig.IS_INPUT_FILE_LOG, GlobalCofig.LOG_NO_LOG);
        LogInputUtil.e(TAG, "是否输出日志 主页重启=" + isInputFileLog);


        Intent alarmIntent = new Intent("com.crm.finance.ACTION_SEND");
        PendingIntent sendIntent = PendingIntent.getBroadcast(getBaseContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //am.cancel(sendIntent);
        //间隔多久去触发广播
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), GlobalCofig.EXECUTE_BROADCAST_INTERVAL, sendIntent);
    }


    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            String cmd = "chmod 777 " + pkgCodePath;

            //NewThread(process);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            String filePath = Environment.getRootDirectory().getPath() + File.separator + "app";
            os.writeBytes("chmod -R 777 " + filePath + "\n");

            os.writeBytes("chmod -R 777 " + GlobalCofig.OPERATION_DIR + "\n");
            os.writeBytes("chmod -R 777 " + GlobalCofig.OPERATION_DIR_1 + "\n");
            os.writeBytes("chmod -R 777 " + "/data/data/com.parallel.space.lite" + "\n");
            os.writeBytes("chmod -R 777 " + GlobalCofig.OPERATION_DIR_0 + "\n");
            os.writeBytes("chmod -R 777 " + GlobalCofig.OPERATION_DIR_11 + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            MyLog.inputLogToFile(TAG, "root权限异常 " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                MyLog.inputLogToFile(TAG, "root权限异常2 " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        System.exit(0);
        super.onDestroy();
    }
}
