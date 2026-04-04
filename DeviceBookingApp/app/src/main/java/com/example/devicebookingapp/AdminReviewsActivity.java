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
import com.google.android.material.button.MaterialButton;
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

public class AdminReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private List<Review> reviewList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reviews);

        recyclerView = findViewById(R.id.rv_reviews);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ReviewAdapter2());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchReviews);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        fetchReviews();
    }

    private void fetchReviews() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/review/allList").get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> swipeRefresh.setRefreshing(false));
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

    private void deleteReview(Integer id) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            Request req = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/review/delete")
                    .post(RequestBody.create(json.toString(),
                            MediaType.get("application/json; charset=utf-8"))).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(AdminReviewsActivity.this,
                            "删除失败", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminReviewsActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                        fetchReviews();
                    });
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    class ReviewAdapter2 extends RecyclerView.Adapter<ReviewAdapter2.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_review, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Review r = reviewList.get(pos);
            h.tvUsername.setText(r.getUsername());
            h.tvDevice.setText(r.getDeviceName() != null ? r.getDeviceName() : "设备");
            h.tvContent.setText(r.getContent());
            int stars = r.getRating() != null ? r.getRating() : 5;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < stars; i++) sb.append("★");
            for (int i = stars; i < 5; i++) sb.append("☆");
            h.tvStars.setText(sb.toString());
            h.btnDelete.setOnClickListener(v ->
                    new AlertDialog.Builder(AdminReviewsActivity.this)
                            .setTitle("删除评价")
                            .setMessage("确定删除该评价？")
                            .setPositiveButton("删除", (d, w) -> deleteReview(r.getId()))
                            .setNegativeButton("取消", null).show());
        }
        @Override public int getItemCount() { return reviewList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvUsername, tvDevice, tvContent, tvStars;
            MaterialButton btnDelete;
            VH(@NonNull View v) {
                super(v);
                tvUsername = v.findViewById(R.id.tv_username);
                tvDevice = v.findViewById(R.id.tv_device);
                tvContent = v.findViewById(R.id.tv_content);
                tvStars = v.findViewById(R.id.tv_stars);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}