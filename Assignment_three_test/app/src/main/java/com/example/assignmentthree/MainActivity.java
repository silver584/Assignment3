package com.example.assignmentthree;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

// 只保留 ServiceSettings 的引用，去掉 MapsInitializer
import com.amap.api.services.core.ServiceSettings;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ============================================================
        // ★★★ 高德隐私合规修正版 ★★★
        // ============================================================

        // 我们只调用 Search SDK (ServiceSettings) 的合规接口
        // 这通常能解决 "Search Error" 问题
        try {
            ServiceSettings.updatePrivacyShow(this, true, true);
            ServiceSettings.updatePrivacyAgree(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ============================================================

        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.start_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapActivity.class));
            }
        });
    }
}