package com.crm.finance.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.crm.finance.R;
import com.crm.finance.UploadDataLogActivity;
import com.crm.finance.base.BaseActivity;
import com.crm.finance.util.GlobalCofig;
import com.crm.finance.util.LogInputUtil;
import com.crm.finance.util.ShareData;
import com.crm.finance.util.Utils;
import com.crm.finance.util.dbutil.WeChatDBOperator;
import com.crm.finance.util.manager.DataCleanManager;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

public class SetActivity extends BaseActivity {
    Button btn_update,btn_upload_rcontact,btn_log_on_off,btn_upload_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        initActionBar(this,"设置");

        initView();
        initData();
        initListenner();
    }
    public void initView(){
        btn_update =  (Button)findViewById(R.id.btn_update);
        btn_upload_rcontact =  (Button)findViewById(R.id.btn_upload_rcontact);
        btn_log_on_off =  (Button)findViewById(R.id.btn_log_on_off);
        btn_upload_data =  (Button)findViewById(R.id.btn_upload_data);
    }
    public void initListenner(){

        btn_upload_rcontact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllDataTime();
                WeChatDBOperator weChatDBOperator =new WeChatDBOperator(SetActivity.this);
                weChatDBOperator.dropAllTable();
                LogInputUtil.showSingleTosat(SetActivity.this,"重置成功，等待上传好友数据！");
            }
        });
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bugly.init(getApplicationContext(), GlobalCofig.BUGLY_ID, GlobalCofig.BUGLY_ISDEBUG);
                Beta.checkUpgrade();
            }
        });
        btn_log_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isNoInputFileLog = ShareData.getInstance().getBooleanValue(GlobalCofig.IS_INPUT_FILE_LOG, GlobalCofig.LOG_NO_LOG);
                ShareData.getInstance().saveBooleanValue(GlobalCofig.IS_INPUT_FILE_LOG, !isNoInputFileLog);
                isNoInputFileLog = ShareData.getInstance().getBooleanValue(GlobalCofig.IS_INPUT_FILE_LOG, GlobalCofig.LOG_NO_LOG);
                String logStr = "日志已开启";
                if (isNoInputFileLog) {
                    logStr = "日志已关闭";
                }
                LogInputUtil.showSingleTosat(SetActivity.this,logStr);
                setLogBtnStr();
            }
        });
        btn_upload_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllDataTime();
                clearMessageDataTime();
            }
        });
    }
    public void clearAllDataTime(){//删除数据库最后更新的时间，防止因时间限制没有重新上传
        Utils.clearDataTime(SetActivity.this,GlobalCofig.OPERATION_DIR, GlobalCofig.WX_DATA_DB, true);
        Utils.clearDataTime(SetActivity.this,GlobalCofig.OPERATION_DIR_1, GlobalCofig.WX_DATA_DB, true);
    }
    public void clearMessageDataTime(){
        Utils.clearMessageDataTime(SetActivity.this,GlobalCofig.OPERATION_DIR, GlobalCofig.WX_DATA_DB, true);
        Utils.clearMessageDataTime(SetActivity.this,GlobalCofig.OPERATION_DIR_1, GlobalCofig.WX_DATA_DB, true);
    }
    public void initData(){
        setLogBtnStr();
    }
    public void setLogBtnStr(){
        boolean isNoInputFileLog = ShareData.getInstance().getBooleanValue(GlobalCofig.IS_INPUT_FILE_LOG, GlobalCofig.LOG_NO_LOG);
        String logStr = "关闭日志";
        if (isNoInputFileLog) {
            logStr = "开启日志";
        }
        if(btn_log_on_off != null ){btn_log_on_off.setText(logStr);}

    }
}
