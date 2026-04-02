package com.example.devicebookingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister;

    // 模拟器访问宿主电脑本地服务器
    private final String BASE_URL = "http://10.0.2.2:8080";
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        // 登录按钮事件
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            sendNetworkRequest(username, password, "/api/login", "登录");
        });

        // 注册按钮事件
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "请输入要注册的账号和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            // 假设你的 Spring Boot 注册接口是 /api/register
            sendNetworkRequest(username, password, "/api/register", "注册");
        });
    }

    // 把登录和注册的网络请求合并成一个通用的方法
    private void sendNetworkRequest(String username, String password, String endpoint, String actionName) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MainActivity", actionName + "请求失败", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "请求失败，请检查后端服务", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                Log.d("MainActivity", "后端返回: " + responseData);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, actionName + "成功: " + responseData, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, actionName + "失败，状态码: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}