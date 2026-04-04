package com.example.devicebookingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
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

public class AdminDevicesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private List<Device> deviceList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_devices);

        recyclerView = findViewById(R.id.rv_devices);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DeviceAdapter2());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchDevices);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_device).setOnClickListener(v -> showDeviceDialog(null));
        fetchDevices();
    }

    private void fetchDevices() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/device/list").get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<Device>>(){}.getType();
                List<Device> list = new Gson().fromJson(response.body().string(), type);
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    deviceList.clear();
                    if (list != null) deviceList.addAll(list);
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
            }
        });
    }

    private void showDeviceDialog(Device device) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_device_form, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_d_name);
        TextInputEditText etType = dialogView.findViewById(R.id.et_d_type);
        TextInputEditText etLocation = dialogView.findViewById(R.id.et_d_location);

        if (device != null) {
            etName.setText(device.getDeviceName());
            etType.setText(device.getDeviceType());
            etLocation.setText(device.getLocation());
        }

        new AlertDialog.Builder(this)
                .setTitle(device == null ? "添加设备" : "编辑设备")
                .setView(dialogView)
                .setPositiveButton("保存", (d, w) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String type = etType.getText() != null ? etType.getText().toString().trim() : "";
                    String loc = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
                    if (name.isEmpty() || type.isEmpty() || loc.isEmpty()) {
                        Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (device == null) addDevice(name, type, loc);
                    else editDevice(device.getId(), name, type, loc, device.getStatus());
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void addDevice(String name, String type, String loc) {
        try {
            JSONObject json = new JSONObject();
            json.put("deviceName", name);
            json.put("deviceType", type);
            json.put("location", loc);
            post("http://192.168.10.105:8080/api/device/add", json.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void editDevice(int id, String name, String type, String loc, String status) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("deviceName", name);
            json.put("deviceType", type);
            json.put("location", loc);
            json.put("status", status != null ? status : "空闲");
            post("http://192.168.10.105:8080/api/device/update", json.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteDevice(int id) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            post("http://192.168.10.105:8080/api/device/delete", json.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void post(String url, String body) {
        Request req = new Request.Builder().url(url)
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminDevicesActivity.this,
                        "操作失败", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                runOnUiThread(() -> {
                    Toast.makeText(AdminDevicesActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                    fetchDevices();
                });
            }
        });
    }

    class DeviceAdapter2 extends RecyclerView.Adapter<DeviceAdapter2.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_device, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Device d = deviceList.get(pos);
            h.tvName.setText(d.getDeviceName());
            h.tvType.setText(d.getDeviceType() + " · " + d.getLocation());
            h.tvStatus.setText(d.getStatus());
            boolean free = "空闲".equals(d.getStatus());
            h.tvStatus.setTextColor(getColor(free ? R.color.status_green : R.color.status_red));
            h.cvStatus.setCardBackgroundColor(getColor(free ? R.color.status_green_bg : R.color.status_red_bg));
            h.btnEdit.setOnClickListener(v -> showDeviceDialog(d));
            h.btnDelete.setOnClickListener(v ->
                    new AlertDialog.Builder(AdminDevicesActivity.this)
                            .setTitle("删除设备")
                            .setMessage("确定删除「" + d.getDeviceName() + "」？此操作不可撤销。")
                            .setPositiveButton("删除", (dlg, w) -> deleteDevice(d.getId()))
                            .setNegativeButton("取消", null).show());
        }
        @Override public int getItemCount() { return deviceList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvType, tvStatus;
            MaterialCardView cvStatus;
            com.google.android.material.button.MaterialButton btnEdit, btnDelete;
            VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_device_name);
                tvType = v.findViewById(R.id.tv_device_type);
                tvStatus = v.findViewById(R.id.tv_status);
                cvStatus = v.findViewById(R.id.cv_status);
                btnEdit = v.findViewById(R.id.btn_edit);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}