package com.example.devicebookingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity {

    private TextView tvStatDevices, tvStatPending, tvStatRepairs, tvAdminName;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tvStatDevices = findViewById(R.id.tv_stat_devices);
        tvStatPending = findViewById(R.id.tv_stat_pending);
        tvStatRepairs = findViewById(R.id.tv_stat_repairs);
        tvAdminName = findViewById(R.id.tv_admin_name);

        // 显示管理员账号
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "管理员");
        tvAdminName.setText(username);

        // 退出登录
        findViewById(R.id.btn_logout).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("退出登录")
                        .setMessage("确定要退出管理员账号吗？")
                        .setPositiveButton("退出", (d, w) -> {
                            prefs.edit().clear().apply();
                            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", null)
                        .show());

        // 菜单跳转
        findViewById(R.id.menu_devices).setOnClickListener(v ->
                startActivity(new Intent(this, AdminDevicesActivity.class)));
        findViewById(R.id.menu_bookings).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBookingsActivity.class)));
        findViewById(R.id.menu_repairs).setOnClickListener(v ->
                startActivity(new Intent(this, AdminRepairsActivity.class)));
        findViewById(R.id.menu_notices).setOnClickListener(v ->
                startActivity(new Intent(this, AdminNoticesActivity.class)));
        findViewById(R.id.menu_reviews).setOnClickListener(v ->
                startActivity(new Intent(this, AdminReviewsActivity.class)));
        findViewById(R.id.menu_users).setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsersActivity.class)));

        fetchStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStats();
    }

    private void fetchStats() {
        fetchCount("http://192.168.10.105:8080/api/device/count", tvStatDevices);
        fetchCount("http://192.168.10.105:8080/api/booking/pendingCount", tvStatPending);
        fetchCount("http://192.168.10.105:8080/api/repair/pendingCount", tvStatRepairs);
    }

    private void fetchCount(String url, TextView tv) {
        Request req = new Request.Builder().url(url).get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (response.body() == null) return;
                String count = response.body().string().trim();
                runOnUiThread(() -> tv.setText(count));
            }
        });
    }
}