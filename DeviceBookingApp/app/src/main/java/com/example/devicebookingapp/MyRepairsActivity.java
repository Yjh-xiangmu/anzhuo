package com.example.devicebookingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.card.MaterialCardView;
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

public class MyRepairsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private List<Repair> repairList = new ArrayList<>();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_repairs);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        recyclerView = findViewById(R.id.rv_my_repairs);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RepairListAdapter());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchRepairs);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_repair).setOnClickListener(v ->
                startActivity(new Intent(this, RepairActivity.class)));

        fetchRepairs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRepairs();
    }

    private void fetchRepairs() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/repair/myList?username=" + username)
                .get().build();
        new OkHttpClient().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MyRepairsActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<Repair>>(){}.getType();
                List<Repair> list = new Gson().fromJson(response.body().string(), type);
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    repairList.clear();
                    if (list != null) repairList.addAll(list);
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
            }
        });
    }

    class RepairListAdapter extends RecyclerView.Adapter<RepairListAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_repair, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Repair r = repairList.get(pos);
            h.tvDevice.setText(r.getDeviceName() != null ? r.getDeviceName() : "设备");
            h.tvDate.setText(r.getCreatedAt());
            h.tvDesc.setText(r.getDescription());
            h.tvStatus.setText(r.getStatus());

            switch (r.getStatus() != null ? r.getStatus() : "") {
                case "待处理":
                    h.tvStatus.setTextColor(getColor(R.color.status_orange));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_orange_bg));
                    break;
                case "处理中":
                    h.tvStatus.setTextColor(getColor(R.color.status_blue));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_blue_bg));
                    break;
                case "已完成":
                    h.tvStatus.setTextColor(getColor(R.color.status_green));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_green_bg));
                    break;
                default:
                    h.tvStatus.setTextColor(getColor(R.color.status_gray));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_gray_bg));
            }
        }

        @Override public int getItemCount() { return repairList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvDevice, tvDate, tvDesc, tvStatus;
            MaterialCardView cvStatus;
            VH(@NonNull View v) {
                super(v);
                tvDevice = v.findViewById(R.id.tv_repair_device);
                tvDate = v.findViewById(R.id.tv_repair_date);
                tvDesc = v.findViewById(R.id.tv_repair_desc);
                tvStatus = v.findViewById(R.id.tv_repair_status);
                cvStatus = v.findViewById(R.id.cv_repair_status);
            }
        }
    }
}