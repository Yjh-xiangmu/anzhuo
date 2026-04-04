package com.example.devicebookingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RepairActivity extends AppCompatActivity {

    private TextInputEditText etDeviceName, etDesc;
    private int selectedDeviceId = -1;
    private String username;
    private List<Device> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        etDeviceName = findViewById(R.id.et_device_name_repair);
        etDesc = findViewById(R.id.et_repair_desc);

        // 点击设备输入框弹出选择器
        etDeviceName.setOnClickListener(v -> fetchAndShowDevicePicker());

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_submit_repair).setOnClickListener(v -> {
            String desc = etDesc.getText() != null
                    ? etDesc.getText().toString().trim() : "";
            if (selectedDeviceId == -1) {
                Toast.makeText(this, "请选择报修设备", Toast.LENGTH_SHORT).show();
                return;
            }
            if (desc.isEmpty()) {
                Toast.makeText(this, "请填写故障描述", Toast.LENGTH_SHORT).show();
                return;
            }
            submitRepair(desc);
        });
    }

    private void fetchAndShowDevicePicker() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/device/list")
                .get().build();
        new OkHttpClient().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(RepairActivity.this,
                        "获取设备列表失败", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Gson gson = new Gson();
                Type type = new TypeToken<List<Device>>(){}.getType();
                deviceList = gson.fromJson(response.body().string(), type);
                if (deviceList == null || deviceList.isEmpty()) return;

                String[] names = new String[deviceList.size()];
                for (int i = 0; i < deviceList.size(); i++) {
                    names[i] = deviceList.get(i).getDeviceName();
                }

                runOnUiThread(() ->
                        new androidx.appcompat.app.AlertDialog.Builder(RepairActivity.this)
                                .setTitle("选择报修设备")
                                .setItems(names, (dialog, which) -> {
                                    Device d = deviceList.get(which);
                                    selectedDeviceId = d.getId();
                                    etDeviceName.setText(d.getDeviceName());
                                })
                                .show()
                );
            }
        });
    }

    private void submitRepair(String desc) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("deviceId", selectedDeviceId);
            json.put("description", desc);

            RequestBody body = RequestBody.create(
                    json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/repair/submit")
                    .post(body).build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(RepairActivity.this,
                            "网络错误", Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    String res = response.body().string();
                    runOnUiThread(() -> {
                        if ("success".equals(res)) {
                            Toast.makeText(RepairActivity.this,
                                    "报修提交成功！", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RepairActivity.this,
                                    "提交失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}