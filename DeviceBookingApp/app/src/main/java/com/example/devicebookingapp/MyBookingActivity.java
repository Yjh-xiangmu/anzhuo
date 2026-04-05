package com.example.devicebookingapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyBookingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyBookingAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llStatusTabs;
    private List<Booking> bookingList = new ArrayList<>();
    private List<Booking> allBookings = new ArrayList<>();
    private String currentStatus = "全部";
    private String currentUsername;

    // 加入"已审核"
    private static final String[] STATUS_FILTERS =
            {"全部", "未开始", "使用中", "已完成", "已审核", "已取消"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_booking);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "");

        recyclerView = findViewById(R.id.rv_my_bookings);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        llStatusTabs = findViewById(R.id.ll_status_tabs);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyBookingAdapter(bookingList, this, this::fetchMyBookings);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchMyBookings);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupStatusTabs();
        fetchMyBookings();
    }

    // 每次回到此页面自动刷新（审核通知效果）
    @Override
    protected void onResume() {
        super.onResume();
        fetchMyBookings();
    }

    private void setupStatusTabs() {
        llStatusTabs.removeAllViews();
        for (String status : STATUS_FILTERS) {
            Chip chip = new Chip(this);
            chip.setText(status);
            chip.setCheckable(true);
            chip.setChecked(status.equals(currentStatus));
            chip.setChipBackgroundColorResource(
                    status.equals(currentStatus) ? R.color.brand_primary : R.color.bg_input);
            chip.setTextColor(status.equals(currentStatus)
                    ? Color.WHITE : getColor(R.color.text_secondary));
            chip.setOnClickListener(v -> {
                currentStatus = status;
                setupStatusTabs();
                applyStatusFilter();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            chip.setLayoutParams(params);
            llStatusTabs.addView(chip);
        }
    }

    private void applyStatusFilter() {
        bookingList.clear();
        for (Booking b : allBookings) {
            if ("全部".equals(currentStatus) || currentStatus.equals(b.getStatus()))
                bookingList.add(b);
        }
        adapter.notifyDataSetChanged();
    }

    void fetchMyBookings() {
        Request request = new Request.Builder()
                .url("http://192.168.10.105:8080/api/booking/myList?username=" + currentUsername)
                .get().build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MyBookingActivity.this, "拉取记录失败", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Type type = new TypeToken<List<Booking>>(){}.getType();
                    List<Booking> fetched = new Gson().fromJson(json, type);
                    runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        allBookings.clear();
                        if (fetched != null) allBookings.addAll(fetched);
                        applyStatusFilter();
                    });
                }
            }
        });
    }
}