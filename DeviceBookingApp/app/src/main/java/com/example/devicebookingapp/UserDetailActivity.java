package com.example.devicebookingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class UserDetailActivity extends AppCompatActivity {

    private TextView tvUsername, tvRole, tvTotal, tvDone, tvRepairs, tvNoBooking;
    private LinearLayout llRecentBookings;
    private final OkHttpClient client = new OkHttpClient();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        username = getIntent().getStringExtra("username");
        String role = getIntent().getStringExtra("role");

        tvUsername = findViewById(R.id.tv_detail_username);
        tvRole = findViewById(R.id.tv_detail_role);
        tvTotal = findViewById(R.id.tv_stat_total);
        tvDone = findViewById(R.id.tv_stat_done);
        tvRepairs = findViewById(R.id.tv_stat_repairs);
        llRecentBookings = findViewById(R.id.ll_recent_bookings);
        tvNoBooking = findViewById(R.id.tv_no_booking);

        tvUsername.setText(username);
        String roleLabel = "admin".equals(role) ? "管理员"
                : "teacher".equals(role) ? "教师" : "学生";
        tvRole.setText(roleLabel);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        fetchUserStats();
        fetchRecentBookings();
    }

    private void fetchUserStats() {
        // 预约统计
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/booking/stats?username=" + username)
                .get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                    int total = obj.optInt("total", 0);
                    int done = obj.optInt("done", 0);
                    runOnUiThread(() -> {
                        tvTotal.setText(String.valueOf(total));
                        tvDone.setText(String.valueOf(done));
                    });
                } catch (Exception ignored) {}
            }
        });

        // 报修次数
        Request req2 = new Request.Builder()
                .url("http://192.168.10.105:8080/api/repair/countByUser?username=" + username)
                .get().build();
        client.newCall(req2).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (response.body() == null) return;
                String count = response.body().string().trim();
                runOnUiThread(() -> tvRepairs.setText(count));
            }
        });
    }

    private void fetchRecentBookings() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/booking/myList?username=" + username)
                .get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<Booking>>(){}.getType();
                List<Booking> list = new Gson().fromJson(response.body().string(), type);
                runOnUiThread(() -> {
                    llRecentBookings.removeAllViews();
                    if (list == null || list.isEmpty()) {
                        tvNoBooking.setVisibility(View.VISIBLE);
                        return;
                    }
                    tvNoBooking.setVisibility(View.GONE);
                    // 只显示最近5条
                    int count = Math.min(list.size(), 5);
                    for (int i = 0; i < count; i++) {
                        Booking b = list.get(i);
                        View item = LayoutInflater.from(UserDetailActivity.this)
                                .inflate(R.layout.item_admin_booking, llRecentBookings, false);

                        ((TextView) item.findViewById(R.id.tv_device_name))
                                .setText(b.getDeviceName());
                        ((TextView) item.findViewById(R.id.tv_username))
                                .setText("时长：" + b.getDuration() + " 分钟");
                        ((TextView) item.findViewById(R.id.tv_time))
                                .setText(b.getStartTime() + " → " + b.getEndTime());

                        TextView tvStatus = item.findViewById(R.id.tv_status);
                        MaterialCardView cvStatus = item.findViewById(R.id.cv_status);
                        tvStatus.setText(b.getStatus());

                        switch (b.getStatus() != null ? b.getStatus() : "") {
                            case "未开始":
                                tvStatus.setTextColor(getColor(R.color.status_orange));
                                cvStatus.setCardBackgroundColor(getColor(R.color.status_orange_bg));
                                break;
                            case "使用中":
                                tvStatus.setTextColor(getColor(R.color.status_green));
                                cvStatus.setCardBackgroundColor(getColor(R.color.status_green_bg));
                                break;
                            case "已完成": case "已审核":
                                tvStatus.setTextColor(getColor(R.color.status_blue));
                                cvStatus.setCardBackgroundColor(getColor(R.color.status_blue_bg));
                                break;
                            default:
                                tvStatus.setTextColor(getColor(R.color.status_gray));
                                cvStatus.setCardBackgroundColor(getColor(R.color.status_gray_bg));
                        }
                        // 隐藏审核按钮
                        item.findViewById(R.id.btn_approve).setVisibility(View.GONE);
                        llRecentBookings.addView(item);
                    }
                });
            }
        });
    }
}