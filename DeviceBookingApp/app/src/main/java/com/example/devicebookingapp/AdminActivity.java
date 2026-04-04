package com.example.devicebookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity {

    private TextView tvStatDevices, tvStatPending, tvStatRepairs;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tvStatDevices = findViewById(R.id.tv_stat_devices);
        tvStatPending = findViewById(R.id.tv_stat_pending);
        tvStatRepairs = findViewById(R.id.tv_stat_repairs);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

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