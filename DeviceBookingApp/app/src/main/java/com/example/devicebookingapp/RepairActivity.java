package com.example.devicebookingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RepairActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;
    private TextInputEditText etDeviceName, etDesc;
    private ImageView ivPreview;
    private MaterialButton btnSelectImage;
    private int selectedDeviceId = -1;
    private String username;
    private String uploadedImageUrl = null;
    private List<Device> deviceList;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        etDeviceName = findViewById(R.id.et_device_name_repair);
        etDesc = findViewById(R.id.et_repair_desc);
        ivPreview = findViewById(R.id.iv_image_preview);
        btnSelectImage = findViewById(R.id.btn_select_image);

        etDeviceName.setOnClickListener(v -> fetchAndShowDevicePicker());
        btnSelectImage.setOnClickListener(v -> pickImage());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_submit_repair).setOnClickListener(v -> {
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
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

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
            ivPreview.setVisibility(View.VISIBLE);
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return;
            byte[] bytes = is.readAllBytes();
            is.close();

            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody fileBody = RequestBody.create(bytes, MediaType.parse(mimeType));
            RequestBody multipart = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "repair_image.jpg", fileBody)
                    .build();

            Request req = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/repair/uploadImage")
                    .post(multipart).build();

            Toast.makeText(this, "图片上传中...", Toast.LENGTH_SHORT).show();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(RepairActivity.this,
                            "图片上传失败，可继续提交（无图片）", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    String url = response.body() != null ? response.body().string() : "";
                    if (!url.startsWith("error")) {
                        uploadedImageUrl = url;
                        runOnUiThread(() -> Toast.makeText(RepairActivity.this,
                                "图片上传成功", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchAndShowDevicePicker() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/device/list").get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(RepairActivity.this,
                        "获取设备列表失败", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<Device>>(){}.getType();
                deviceList = new Gson().fromJson(response.body().string(), type);
                if (deviceList == null || deviceList.isEmpty()) return;
                String[] names = new String[deviceList.size()];
                for (int i = 0; i < deviceList.size(); i++) names[i] = deviceList.get(i).getDeviceName();
                runOnUiThread(() ->
                        new androidx.appcompat.app.AlertDialog.Builder(RepairActivity.this)
                                .setTitle("选择报修设备")
                                .setItems(names, (dialog, which) -> {
                                    Device d = deviceList.get(which);
                                    selectedDeviceId = d.getId();
                                    etDeviceName.setText(d.getDeviceName());
                                }).show());
            }
        });
    }

    private void submitRepair(String desc) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("deviceId", selectedDeviceId);
            json.put("description", desc);
            json.put("imageUrl", uploadedImageUrl != null ? uploadedImageUrl : "");

            RequestBody body = RequestBody.create(
                    json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request req = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/repair/submit").post(body).build();

            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(RepairActivity.this,
                            "网络错误", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    String res = response.body() != null ? response.body().string() : "";
                    runOnUiThread(() -> {
                        if ("success".equals(res)) {
                            Toast.makeText(RepairActivity.this, "报修提交成功！", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RepairActivity.this, "提交失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }
}