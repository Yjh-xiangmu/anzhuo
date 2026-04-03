package com.example.devicebookingapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private List<Booking> bookingList = new ArrayList<>();
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_booking);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "");

        recyclerView = findViewById(R.id.rv_my_bookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 传入一个刷新回调，当点击按钮操作成功后自动刷新列表
        adapter = new MyBookingAdapter(bookingList, this, this::fetchMyBookings);
        recyclerView.setAdapter(adapter);

        fetchMyBookings();
    }

    private void fetchMyBookings() {
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/booking/myList?username=" + currentUsername)
                .get()
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MyBookingActivity.this, "拉取记录失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonStr = response.body().string();
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Booking>>(){}.getType();
                    List<Booking> fetchedList = gson.fromJson(jsonStr, type);

                    runOnUiThread(() -> {
                        bookingList.clear();
                        if (fetchedList != null) {
                            bookingList.addAll(fetchedList);
                        }
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}