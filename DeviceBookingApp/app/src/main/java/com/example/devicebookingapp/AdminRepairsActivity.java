package com.example.devicebookingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminRepairsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private List<Repair> repairList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_repairs);

        recyclerView = findViewById(R.id.rv_repairs);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RepairAdapter2());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchRepairs);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        fetchRepairs();
    }

    private void fetchRepairs() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/repair/allList").get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> swipeRefresh.setRefreshing(false));
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

    // 异步加载图片（无需第三方库）
    private void loadImageAsync(String imageUrl, ImageView imageView) {
        executor.execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                mainHandler.post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                // 图片加载失败静默处理
            }
        });
    }

    private void updateRepairStatus(int id, String status) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("status", status);
            postJson("http://192.168.10.105:8080/api/repair/updateStatus",
                    json.toString(), "报修状态已更新");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setDeviceLock(int deviceId, boolean lock) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", deviceId);
            json.put("status", lock ? "故障锁定" : "空闲");
            postJson("http://192.168.10.105:8080/api/device/updateStatus",
                    json.toString(), lock ? "设备已锁定，暂停预约" : "设备已解锁，恢复正常");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void postJson(String url, String body, String successMsg) {
        Request req = new Request.Builder().url(url)
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminRepairsActivity.this,
                        "操作失败", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                runOnUiThread(() -> {
                    Toast.makeText(AdminRepairsActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    fetchRepairs();
                });
            }
        });
    }

    class RepairAdapter2 extends RecyclerView.Adapter<RepairAdapter2.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_repair, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Repair r = repairList.get(pos);
            h.tvDevice.setText(r.getDeviceName());
            h.tvUsername.setText("报修人：" + r.getUsername());
            h.tvDesc.setText(r.getDescription());
            h.tvStatus.setText(r.getStatus());

            // 显示故障图片
            String imgUrl = r.getImageUrl();
            if (imgUrl != null && !imgUrl.isEmpty()) {
                h.cvImage.setVisibility(View.VISIBLE);
                loadImageAsync(imgUrl, h.ivImage);
            } else {
                h.cvImage.setVisibility(View.GONE);
            }

            // 状态按钮逻辑
            switch (r.getStatus() != null ? r.getStatus() : "") {
                case "待处理":
                    h.tvStatus.setTextColor(getColor(R.color.status_orange));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_orange_bg));
                    h.btnProcessing.setVisibility(View.VISIBLE);
                    h.btnDone.setVisibility(View.VISIBLE);
                    h.btnLock.setVisibility(View.VISIBLE);
                    h.btnLock.setText("🔒 锁定设备");
                    h.btnLock.setOnClickListener(v ->
                            new AlertDialog.Builder(AdminRepairsActivity.this)
                                    .setTitle("锁定设备")
                                    .setMessage("锁定后该设备将无法被预约，确认锁定「" + r.getDeviceName() + "」？")
                                    .setPositiveButton("锁定", (d, w) -> setDeviceLock(r.getDeviceId(), true))
                                    .setNegativeButton("取消", null).show());
                    break;
                case "处理中":
                    h.tvStatus.setTextColor(getColor(R.color.status_blue));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_blue_bg));
                    h.btnProcessing.setVisibility(View.GONE);
                    h.btnDone.setVisibility(View.VISIBLE);
                    h.btnLock.setVisibility(View.VISIBLE);
                    h.btnLock.setText("🔓 解锁设备");
                    h.btnLock.setOnClickListener(v ->
                            new AlertDialog.Builder(AdminRepairsActivity.this)
                                    .setTitle("解锁设备")
                                    .setMessage("维修完毕？解锁后设备恢复正常预约。")
                                    .setPositiveButton("解锁", (d, w) -> setDeviceLock(r.getDeviceId(), false))
                                    .setNegativeButton("取消", null).show());
                    break;
                default:
                    h.tvStatus.setTextColor(getColor(R.color.status_green));
                    h.cvStatus.setCardBackgroundColor(getColor(R.color.status_green_bg));
                    h.btnProcessing.setVisibility(View.GONE);
                    h.btnDone.setVisibility(View.GONE);
                    h.btnLock.setVisibility(View.GONE);
            }
            h.btnProcessing.setOnClickListener(v -> updateRepairStatus(r.getId(), "处理中"));
            h.btnDone.setOnClickListener(v -> updateRepairStatus(r.getId(), "已完成"));
        }

        @Override public int getItemCount() { return repairList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvDevice, tvUsername, tvDesc, tvStatus;
            MaterialCardView cvStatus, cvImage;
            ImageView ivImage;
            MaterialButton btnProcessing, btnDone, btnLock;
            VH(@NonNull View v) {
                super(v);
                tvDevice = v.findViewById(R.id.tv_device_name);
                tvUsername = v.findViewById(R.id.tv_username);
                tvDesc = v.findViewById(R.id.tv_desc);
                tvStatus = v.findViewById(R.id.tv_status);
                cvStatus = v.findViewById(R.id.cv_status);
                cvImage = v.findViewById(R.id.cv_image);
                ivImage = v.findViewById(R.id.iv_repair_image);
                btnProcessing = v.findViewById(R.id.btn_processing);
                btnDone = v.findViewById(R.id.btn_done);
                btnLock = v.findViewById(R.id.btn_lock);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}