package com.example.devicebookingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceDetailActivity extends AppCompatActivity {

    private TextView tvName, tvType, tvTypeInfo, tvLocation, tvStatus, tvReviewCount, tvNoReview;
    private LinearLayout llReviews;
    private MaterialButton btnBook;
    private final OkHttpClient client = new OkHttpClient();
    private int deviceId;
    private String deviceName, deviceType, location, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        deviceId = getIntent().getIntExtra("deviceId", -1);
        deviceName = getIntent().getStringExtra("deviceName");
        deviceType = getIntent().getStringExtra("deviceType");
        location = getIntent().getStringExtra("location");
        status = getIntent().getStringExtra("status");

        tvName = findViewById(R.id.tv_detail_name);
        tvType = findViewById(R.id.tv_detail_type);
        tvTypeInfo = findViewById(R.id.tv_detail_type_info);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvReviewCount = findViewById(R.id.tv_review_count);
        tvNoReview = findViewById(R.id.tv_no_review);
        llReviews = findViewById(R.id.ll_reviews);
        btnBook = findViewById(R.id.btn_book_from_detail);

        tvName.setText(deviceName);
        tvType.setText(deviceType);
        tvTypeInfo.setText(deviceType);
        tvLocation.setText(location);
        tvStatus.setText(status);

        boolean isFree = "空闲".equals(status);
        btnBook.setEnabled(isFree);
        btnBook.setText(isFree ? "立即预约此设备" : "设备暂不可用");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnBook.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            if (!prefs.getBoolean("isLoggedIn", false)) {
                Toast.makeText(this, "请登录后预约", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            // Pass back to device fragment booking flow via intent
            // For simplicity, we reuse the same booking dialog used in DeviceAdapter
            Toast.makeText(this, "请在设备列表页点击「立即预约」", Toast.LENGTH_SHORT).show();
        });

        fetchReviews();
    }

    private void fetchReviews() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/review/deviceList?deviceId=" + deviceId)
                .get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                String json = response.body().string();
                Gson gson = new Gson();
                Type type = new TypeToken<List<Review>>(){}.getType();
                List<Review> reviews = gson.fromJson(json, type);
                runOnUiThread(() -> {
                    if (reviews == null || reviews.isEmpty()) {
                        tvNoReview.setVisibility(View.VISIBLE);
                        tvReviewCount.setText("0 条");
                        return;
                    }
                    tvNoReview.setVisibility(View.GONE);
                    tvReviewCount.setText(reviews.size() + " 条");
                    llReviews.removeAllViews();
                    for (Review r : reviews) {
                        View item = LayoutInflater.from(DeviceDetailActivity.this)
                                .inflate(R.layout.item_review, llReviews, false);
                        String initial = r.getUsername() != null && r.getUsername().length() > 0
                                ? r.getUsername().substring(0, 1) : "用";
                        ((TextView) item.findViewById(R.id.tv_review_avatar)).setText(initial);
                        ((TextView) item.findViewById(R.id.tv_review_username)).setText(r.getUsername());
                        ((TextView) item.findViewById(R.id.tv_review_date)).setText(r.getCreatedAt());
                        ((TextView) item.findViewById(R.id.tv_review_content)).setText(r.getContent());
                        // Stars
                        int stars = r.getRating() != null ? r.getRating() : 5;
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < stars; i++) sb.append("★");
                        for (int i = stars; i < 5; i++) sb.append("☆");
                        ((TextView) item.findViewById(R.id.tv_review_stars)).setText(sb.toString());
                        llReviews.addView(item);
                    }
                });
            }
        });
    }
}