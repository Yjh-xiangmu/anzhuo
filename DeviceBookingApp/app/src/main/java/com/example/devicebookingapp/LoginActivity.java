package com.example.devicebookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://192.168.10.105:8080/api/user/login";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText etUsername = findViewById(R.id.et_username);
        TextInputEditText etPassword = findViewById(R.id.et_password);
        MaterialButton btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText() != null
                    ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null
                    ? etPassword.getText().toString().trim() : "";
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            performLogin(username, password);
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void performLogin(String username, String password) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(SERVER_URL).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this,
                            "网络请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    String resStr = response.body().string();
                    android.util.Log.d("LOGIN", "收到响应: [" + resStr + "]");
                    runOnUiThread(() -> {
                        if (response.isSuccessful() && resStr.startsWith("success")) {
                            // 解析角色：格式为 "success:student" 或 "success:admin"
                            String role = "student";
                            if (resStr.contains(":")) {
                                role = resStr.split(":")[1].trim();
                            }
                            final String finalRole = role;
                            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("isLoggedIn", true)
                                    .putString("username", username)
                                    .putString("role", finalRole)
                                    .apply();

                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "登录失败，用户名或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}