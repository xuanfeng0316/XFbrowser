package com.xuanfeng.browser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class SettingsActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        
        TextView title = new TextView(this);
        title.setText("设置");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);
        
        TextView version = new TextView(this);
        version.setText("XF浏览器 v1.0");
        version.setTextSize(16);
        version.setPadding(0, 20, 0, 20);
        layout.addView(version);
        
        setContentView(layout);
    }
}