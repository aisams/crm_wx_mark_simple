package com.crm.finance.ui;

import android.os.Bundle;

import com.crm.finance.R;
import com.crm.finance.base.BaseActivity;

public class HintActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint);
        initActionBar(this,"注意事项");
    }
}
