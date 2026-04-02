package com.example.devicebookingapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加载带有四个底部导航按钮的最新框架
        setContentView(R.layout.activity_main);

        // 之前的登录网络请求代码已被移除
        // 后续我们将在这里编写底部四个页面的切换逻辑
    }
}