package com.example.devicebookingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 管理员登录后直接跳管理员控制台
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String role = prefs.getString("role", "student");
        if (isLoggedIn && "admin".equals(role)) {
            startActivity(new Intent(this, AdminActivity.class));
            // 不 finish()，保留 MainActivity 作为返回栈底
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f = null;

            if (id == R.id.nav_home) {
                f = new HomeFragment();
            } else if (id == R.id.nav_notice) {
                f = new NoticeFragment();
            } else if (id == R.id.nav_device) {
                f = new DeviceFragment();
            } else if (id == R.id.nav_mine) {
                if (!isLoggedIn()) {
                    Toast.makeText(this, "请登录后查看个人信息", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                f = new MineFragment();
            }
            return loadFragment(f);
        });
    }

    // 每次回到主界面时刷新当前 fragment（切换页面自动刷新）
    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载当前选中的 tab 对应的 fragment
        int selectedId = bottomNav.getSelectedItemId();
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current instanceof DeviceFragment) {
            loadFragment(new DeviceFragment());
            bottomNav.setSelectedItemId(R.id.nav_device);
        } else if (current instanceof NoticeFragment) {
            loadFragment(new NoticeFragment());
            bottomNav.setSelectedItemId(R.id.nav_notice);
        }
        // HomeFragment 和 MineFragment 内部已有 onResume 刷新逻辑
    }

    public void switchToDeviceTab() {
        bottomNav.setSelectedItemId(R.id.nav_device);
    }

    public void switchToNoticeTab() {
        bottomNav.setSelectedItemId(R.id.nav_notice);
    }

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
        return getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getBoolean("isLoggedIn", false);
    }
}