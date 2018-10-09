package com.crm.finance.base;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crm.finance.R;


/**
 * Created by Administrator on 2018/9/12 0012.
 */

public class BaseActivity extends Activity {

    public static void initActionBar(final Activity activity, String title) {
        TextView textView = (TextView) activity.findViewById(R.id.title_content_text);
        textView.setText(title);
        LinearLayout titleBackBtn = (LinearLayout) activity.findViewById(R.id.title_back_layout);
        titleBackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

}
