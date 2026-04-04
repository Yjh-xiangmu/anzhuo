package com.example.devicebookingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etOld, etNew, etConfirm;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        etOld = findViewById(R.id.et_old_password);
        etNew = findViewById(R.id.et_new_password);
        etConfirm = findViewById(R.id.et_confirm_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            String oldPwd = etOld.getText() != null ? etOld.getText().toString().trim() : "";
            String newPwd = etNew.getText() != null ? etNew.getText().toString().trim() : "";
            String confirm = etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";

            if (oldPwd.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPwd.length() < 6) {
                Toast.makeText(this, "新密码至少需要6位", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPwd.equals(confirm)) {
                Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            changePassword(oldPwd, newPwd);
        });
    }

    private void changePassword(String oldPwd, String newPwd) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("oldPassword", oldPwd);
            json.put("newPassword", newPwd);

            RequestBody body = RequestBody.create(
                    json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/user/changePassword")
                    .post(body).build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this,
                            "网络错误", Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    String res = response.body().string();
                    runOnUiThread(() -> {
                        switch (res) {
                            case "success":
                                Toast.makeText(ChangePasswordActivity.this,
                                        "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                                // 清除登录状态，跳回登录页
                                getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                        .edit().clear().apply();
                                android.content.Intent intent = new android.content.Intent(
                                        ChangePasswordActivity.this, LoginActivity.class);
                                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                        | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                break;
                            case "wrong_password":
                                Toast.makeText(ChangePasswordActivity.this,
                                        "当前密码错误", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(ChangePasswordActivity.this,
                                        "修改失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}