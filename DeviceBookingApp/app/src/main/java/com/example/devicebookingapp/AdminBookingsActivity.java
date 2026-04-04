package com.example.devicebookingapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llTabs;
    private List<Booking> allBookings = new ArrayList<>();
    private List<Booking> bookingList = new ArrayList<>();
    private String currentTab = "全部";
    private static final String[] TABS = {"全部", "未开始", "使用中", "已完成", "待审核", "已取消"};
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        recyclerView = findViewById(R.id.rv_bookings);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        llTabs = findViewById(R.id.ll_tabs);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new BookingAdapter2());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchBookings);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupTabs();
        fetchBookings();
    }

    private void setupTabs() {
        llTabs.removeAllViews();
        for (String tab : TABS) {
            Chip chip = new Chip(this);
            chip.setText(tab);
            chip.setCheckable(true);
            chip.setChecked(tab.equals(currentTab));
            chip.setChipBackgroundColorResource(
                    tab.equals(currentTab) ? R.color.brand_primary : R.color.bg_input);
            chip.setTextColor(tab.equals(currentTab) ? Color.WHITE : getColor(R.color.text_secondary));
            chip.setOnClickListener(v -> { currentTab = tab; setupTabs(); applyFilter(); });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setMarginEnd(8);
            chip.setLayoutParams(p);
            llTabs.addView(chip);
        }
    }

    private void applyFilter() {
        bookingList.clear();
        for (Booking b : allBookings) {
            if ("全部".equals(currentTab) || currentTab.equals(b.getStatus()))
                bookingList.add(b);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void fetchBookings() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/booking/allList").get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<Booking>>(){}.getType();
                List<Booking> list = new Gson().fromJson(response.body().string(), type);
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    allBookings.clear();
                    if (list != null) allBookings.addAll(list);
                    applyFilter();
                });
            }
        });
    }

    private void approveBooking(Integer bookingId) {
        try {
            JSONObject json = new JSONObject();
            json.put("bookingId", bookingId);
            Request req = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/booking/approve")
                    .post(RequestBody.create(json.toString(),
                            MediaType.get("application/json; charset=utf-8"))).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(AdminBookingsActivity.this,
                            "操作失败", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminBookingsActivity.this,
                                "审核通过，设备已恢复空闲", Toast.LENGTH_SHORT).show();
                        fetchBookings();
                    });
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    class BookingAdapter2 extends RecyclerView.Adapter<BookingAdapter2.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_booking, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Booking b = bookingList.get(pos);
            h.tvDevice.setText(b.getDeviceName());
            h.tvUsername.setText("预约人：" + b.getUsername());
            h.tvTime.setText("时间：" + b.getStartTime() + " → " + b.getEndTime());
            h.tvStatus.setText(b.getStatus());
            h.btnApprove.setVisibility(View.GONE);

            switch (b.getStatus() != null ? b.getStatus() : "") {
                case "未开始":
                    h.tvStatus.setTextColor(getColor(R.color.status_orange));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_orange_bg)); break;
                case "使用中":
                    h.tvStatus.setTextColor(getColor(R.color.status_green));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_green_bg)); break;
                case "已完成":
                    h.tvStatus.setTextColor(getColor(R.color.status_blue));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_blue_bg));
                    // 已完成 → 显示审核按钮
                    h.btnApprove.setVisibility(View.VISIBLE);
                    h.btnApprove.setOnClickListener(v ->
                            new AlertDialog.Builder(AdminBookingsActivity.this)
                                    .setTitle("审核确认")
                                    .setMessage("确认设备已归还并完好？审核通过后设备恢复空闲状态。")
                                    .setPositiveButton("确认通过", (d, w) -> approveBooking(b.getId()))
                                    .setNegativeButton("取消", null).show());
                    break;
                default:
                    h.tvStatus.setTextColor(getColor(R.color.status_gray));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_gray_bg));
            }
        }
        @Override public int getItemCount() { return bookingList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvDevice, tvUsername, tvTime, tvStatus;
            MaterialCardView cvStatus;
            MaterialButton btnApprove;
            VH(@NonNull View v) {
                super(v);
                tvDevice = v.findViewById(R.id.tv_device_name);
                tvUsername = v.findViewById(R.id.tv_username);
                tvTime = v.findViewById(R.id.tv_time);
                tvStatus = v.findViewById(R.id.tv_status);
                cvStatus = v.findViewById(R.id.cv_status);
                btnApprove = v.findViewById(R.id.btn_approve);
            }
        }
    }
}