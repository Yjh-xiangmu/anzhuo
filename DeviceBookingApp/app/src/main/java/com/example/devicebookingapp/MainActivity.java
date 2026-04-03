package com.example.devicebookingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 默认显示首页，防止一进来是空白的
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_notice) {
                selectedFragment = new NoticeFragment();
            } else if (itemId == R.id.nav_device) {
                // 设备页面全员开放
                selectedFragment = new DeviceFragment();
            } else if (itemId == R.id.nav_mine) {
                // 只有“我的”页面需要强制拦截登录
                if (!isLoggedIn()) {
                    Toast.makeText(this, "请登录后查看个人信息", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                selectedFragment = new MineFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    // 封装一个切换页面的工具方法
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }
}