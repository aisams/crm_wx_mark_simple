package com.crm.finance.util;

import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.crm.finance.app.MyApplication;
import com.crm.finance.broadcast.BroadcastUtils;
import com.crm.finance.util.fileutil.FileUtil;
import com.crm.finance.util.rootcmd.RootCmd;
import com.crm.finance.util.timeutil.TimeUtils;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/4/25 0025.
 */

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String getVersionNumber(Context context) {
        String version = "";
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    //判断服务是否存在
    public static boolean isServiceRunning(Context context, String serviceName) {
        if (!isEmpty(serviceName) && context != null) {
            ActivityManager activityManager
                    = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ArrayList<ActivityManager.RunningServiceInfo> runningServiceInfoList
                    = (ArrayList<ActivityManager.RunningServiceInfo>) activityManager.getRunningServices(100);
            for (Iterator<ActivityManager.RunningServiceInfo> iterator = runningServiceInfoList.iterator(); iterator.hasNext(); ) {
                ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) iterator.next();
                if (serviceName.equals(runningServiceInfo.service.getClassName().toString())) {
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }


    /**
     * 获取apk包的信息：版本号，名称，图标等
     *
     * @param absPath apk包的绝对路径
     * @param context
     */
    public static int getAPKVersionCode(Context context, String absPath) {

        int versionCode = 0;
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            versionCode = pkgInfo.versionCode; // 得到版本信息
            LogInputUtil.e(TAG, String.format("APK版本号: %s , APK路径;%s", versionCode, absPath));
        }
        return versionCode;
    }

    /**
     * 防止升级后不是系统应用，将应用包也放到system/app目录下
     *
     * @param context
     */
    public static void copyVserAPK(Context context) {
        LogInputUtil.e(TAG, "准备核对版本");
        String sysApkName = "crm.apk";
        String systemPath = Environment.getRootDirectory().getPath();
        int sysAppVersion = Utils.getAPKVersionCode(context, systemPath + File.separator + "app" + File.separator + sysApkName);
        File nowAppFile = null;
        try {
            nowAppFile = new File(context.getPackageManager().getApplicationInfo("com.crm.finance", 0).sourceDir);
        } catch (Exception e) {
            MyLog.inputLogToFile(TAG, "获取当前APK目录异常：" + e.getLocalizedMessage());
            return;
        }

        String nowAppParentDir = nowAppFile.getPath();
        LogInputUtil.e(TAG, "当前应用安装目录：" + nowAppParentDir);
        int nowAppVersion = Utils.getAPKVersionCode(context, nowAppParentDir);

        if (sysAppVersion == nowAppVersion) {
            LogInputUtil.e(TAG, "两个版本一样，无需覆盖");
            return;//两个版本一样，无需覆盖
        } else if (nowAppVersion == 0) {
            MyLog.inputLogToFile(TAG, "缺少data/app/base.apk版本，即当前安装版本源件");
            return;
        } else {
            MyLog.inputLogToFile(TAG, "检测到版本差异，准备覆盖");
        }

        if (!RootCmd.haveRoot()) {
            LogInputUtil.e(TAG, "没ROOT权限");
            return;
        }

        String paramString = "adb shell" + "\n" +
                "su" + "\n" +
                "mount -oremount /system" + "\n" +
                "cp " + nowAppParentDir + "  /system/app/" + sysApkName + "\n" +
                "chmod 777 /system/app/" + sysApkName + "\n" +
                "exit" + "\n" +
                "exit";
        int result = RootCmd.execRootCmdSilent(paramString);
        if (result == -1) {
            LogInputUtil.e(TAG, "adb执行异常(复制apk包)");
        } else {
            LogInputUtil.e(TAG, "adb执行完成(复制apk包)");
        }
    }

    /**
     * 获取手机设备号
     *
     * @return
     */
    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) MyApplication.getAPP().getApplicationContext().getSystemService(MyApplication.getAPP().TELEPHONY_SERVICE);
        String DeviceId = tm.getDeviceId();
        return DeviceId;
    }

    /**
     * 判断app是否获得root权限
     *
     * @return
     */
    public static boolean isROOT() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("ls /data/data/\n");
            os.writeBytes("exit\n");
            os.flush();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            if (result.contains("com.android.phone")) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static void copySOFile(Context context) {
        LogInputUtil.e(TAG, "准备复制SO文件");
        String systemPath = Environment.getRootDirectory().getPath();
        String sysLibSODir = systemPath + File.separator + "lib" + File.separator;//system/lib目录，需将so文件存放在这
        if (!new File(sysLibSODir).exists()) return;//没有需存放SO的文件夹

        File nowLibSODir = null;
        try {
            nowLibSODir = new File(context.getPackageManager().getApplicationInfo("com.crm.finance", 0).sourceDir);
        } catch (Exception e) {
            MyLog.inputLogToFile(TAG, "获取当前SO目录异常：" + e.getLocalizedMessage());
            return;
        }
        if (!nowLibSODir.exists()) return;//没有存放SO的文件,预装的可能没有

        String soParentDir = nowLibSODir.getParent();
        LogInputUtil.e(TAG, "so文件夹父目录 = " + soParentDir);
        soParentDir = soParentDir + File.separator + "lib" + File.separator + "arm" + File.separator;
        File soParentFile = new File(soParentDir);
        LogInputUtil.e(TAG, "SOLib目录 = " + soParentDir + ",是否存在 =" + soParentFile.exists());

        if (!soParentFile.exists()) return;

        if (!RootCmd.haveRoot()) {
            LogInputUtil.e(TAG, "没ROOT权限");
            return;
        }

        String paramString = "adb shell" + "\n" +
                "su" + "\n" +
                "mount -oremount /system" + "\n" +
                "cp " + soParentDir + "*" + "  " + sysLibSODir + "\n" +
                "chmod -R 777 " + sysLibSODir + "\n" +
                "exit" + "\n" +
                "exit";
        LogInputUtil.e(TAG, "paramString = " + paramString);
        int result = RootCmd.execRootCmdSilent(paramString);
        if (result == -1) {
            LogInputUtil.e(TAG, "adb执行异常(复制SO包)");
        } else {
            LogInputUtil.e(TAG, "adb执行完成(复制SO包)");
        }
    }

    /**
     * 时间戳转日期
     *
     * @param ms
     * @return
     */
    public static String transForDate(Long ms) {
        String times = TimeUtils.transForDate(ms);
        return times;
    }

    /**
     * 将内容中所有11位电话号码 4-7位****化，例137****0180
     *
     * @param tel
     * @return
     */
    public static String replacePhoneNumber(String tel) {
        if (isEmpty(tel)) return "";
        // 括号表示组，被替换的部分$n表示第n组的内容,
        try {
            tel = tel.replaceAll("(1[34578]\\d)\\d{4}(\\d{4})([^\\d])", "$1****$2$3");
        } catch (Exception e) {
            MyLog.inputLogToFile(TAG, "电话号和谐异常，msg = " + e.getMessage() + ",content = " + tel);
        }
        return tel;
    }

    public static String addFuffix(String fileName) {
        int fuffixIndex = fileName.indexOf(".");
        if (fuffixIndex < 0) {
            return fileName + ".jpg";
        }
        return fileName;
    }

    public static boolean isEmpty(String content) {
        if (content == null || content.equals("")) {
            return true;
        }
        return false;
    }


    /**
     * @param sSecret
     * @return md5 32位加密，16位小写加密只需getMd5Value("xxx").substring(8, 24);即可
     */
    public static String getMd5Value(String sSecret) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(sSecret.getBytes());
            int i;
            StringBuffer buf = new StringBuffer();
            byte[] b = bmd5.digest();
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //数据有上传时间，用于UI正常异常展示
    public static void setUploadTime() {
        long time = System.currentTimeMillis();
        LogInputUtil.e(TAG, "写入时间 =" + time);
        ShareData.getInstance().saveLongValue(MyApplication.getAPP(), GlobalCofig.SHARE_UPDATETIME, time);
    }

    //获取上传时间，用于UI正常异常展示
    public static long getUploadTime() {
        long time = ShareData.getInstance().getLongValue(MyApplication.getAPP(), GlobalCofig.SHARE_UPDATETIME, 0);
        return time;
    }

    public static boolean checkUploadState() {
        long scrTime = Utils.getUploadTime();
        long newTime = System.currentTimeMillis();
        long count = (newTime - scrTime);
        //LogInputUtil.e(TAG, "Time = " + scrTime + ",currentTime = " + newTime + ",count = " + count);
        boolean isNormal = false;
        if (count < GlobalCofig.UPLOAD_NORMAL_TIME) {
            isNormal = true;
        }
        return isNormal;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    //清除掉数据库最后更新时间，便于重新上传数据
    public static void clearDataTime(Context context, String Path, String Extension, boolean IsIterative)  //搜索目录，扩展名，是否进入子文件夹
    {
        File[] files = new File(Path).listFiles();
        if (files == null) return;
        try {

            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isFile()) {
                    if (f.getPath().endsWith(Extension)) {//查找指定扩展名的文件
                        String wxFolderPath = new File(f.getParent()).getName();
                        if (wxFolderPath.length() != 32) continue;
                        Long fileChangeTime1 = ShareData.getInstance().getLongValue(context, f.getPath(), 0);
                        ShareData.getInstance().saveLongValue(context, f.getPath(), 0);
                        Long fileChangeTime = ShareData.getInstance().getLongValue(context, f.getPath(), 0);
                        LogInputUtil.e(TAG, "清除的时间=" + fileChangeTime + ",原保存的时间 = " + fileChangeTime1 + ", 路径 = " + f.getPath());
                    }
                    if (!IsIterative)
                        break;
                } else if (f.isDirectory()) {
                    clearDataTime(context, f.getPath(), Extension, IsIterative);
                }
            }

        } catch (Exception e) {
            MyLog.inputLogToFile(TAG, "关闭数据库修改时间异常：" + e.getMessage());
        }
    }

    //清除聊天信息上传的时间标识，便于重新上传聊天数据
    public static void clearMessageDataTime(Context context, String Path, String Extension, boolean IsIterative)  //搜索目录，扩展名，是否进入子文件夹
    {
        File[] files = new File(Path).listFiles();
        if (files == null) return;
        try {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isFile()) {
                    if (f.getPath().endsWith(Extension)) {//查找指定扩展名的文件
                        String wxFolderPath = new File(f.getParent()).getName();
                        if (wxFolderPath.length() != 32) continue;
                        String lastUploadTimeStr = GlobalCofig.MESSAGE_LAST_UPLOAD_TIME + f.getPath();

                        Long fileChangeTime1 = ShareData.getInstance().getLongValue(context, lastUploadTimeStr, 0);
                        ShareData.getInstance().saveLongValue(context, lastUploadTimeStr, 0);
                        Long fileChangeTime = ShareData.getInstance().getLongValue(context, lastUploadTimeStr, 0);
                        LogInputUtil.e(TAG, "清除的聊天时间=" + fileChangeTime + ",原保存的时间 = " + fileChangeTime1 + ", 路径 = " + lastUploadTimeStr);
                    }
                    if (!IsIterative)
                        break;
                } else if (f.isDirectory()) {
                    clearMessageDataTime(context, f.getPath(), Extension, IsIterative);
                }


            }
        } catch (Exception e) {
            MyLog.inputLogToFile(TAG, "清除聊天信息上传的时间标识异常：" + e.getMessage());
        }

    }

}

