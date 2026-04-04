package com.example.devicebookingapp;

import android.content.Context;
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

public class MyReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private List<Review> reviewList = new ArrayList<>();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        recyclerView = findViewById(R.id.rv_my_reviews);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ReviewListAdapter());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchReviews);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        fetchReviews();
    }

    private void fetchReviews() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/review/myList?username=" + username)
                .get().build();
        new OkHttpClient().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> { swipeRefresh.setRefreshing(false);
                    Toast.makeText(MyReviewsActivity.this, "获取失败", Toast.LENGTH_SHORT).show(); });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<Review>>(){}.getType();
                List<Review> list = new Gson().fromJson(response.body().string(), type);
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    reviewList.clear();
                    if (list != null) reviewList.addAll(list);
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
            }
        });
    }

    class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_review, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Review r = reviewList.get(pos);
            h.tvDevice.setText(r.getDeviceName() != null ? r.getDeviceName() : "设备");
            h.tvDate.setText(r.getCreatedAt());
            h.tvContent.setText(r.getContent());
            StringBuilder sb = new StringBuilder();
            int stars = r.getRating() != null ? r.getRating() : 5;
            for (int i = 0; i < stars; i++) sb.append("★");
            for (int i = stars; i < 5; i++) sb.append("☆");
            h.tvStars.setText(sb.toString());
        }
        @Override public int getItemCount() { return reviewList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvDevice, tvDate, tvContent, tvStars;
            VH(@NonNull View v) {
                super(v);
                tvDevice = v.findViewById(R.id.tv_review_device);
                tvDate = v.findViewById(R.id.tv_review_date);
                tvContent = v.findViewById(R.id.tv_review_content);
                tvStars = v.findViewById(R.id.tv_review_stars);
            }
        }
    }
}