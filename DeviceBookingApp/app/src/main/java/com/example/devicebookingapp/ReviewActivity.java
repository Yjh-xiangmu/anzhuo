package com.example.devicebookingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
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

public class ReviewActivity extends AppCompatActivity {

    private int selectedRating = 5;
    private TextView[] stars = new TextView[5];
    private TextView tvRatingLabel, tvDeviceName;
    private TextInputEditText etContent;
    private int deviceId, bookingId;
    private String deviceName, username;

    private static final String[] RATING_LABELS = {
            "1 星 — 非常不满意", "2 星 — 不满意",
            "3 星 — 一般", "4 星 — 满意", "5 星 — 非常满意"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        deviceId = getIntent().getIntExtra("deviceId", -1);
        bookingId = getIntent().getIntExtra("bookingId", -1);
        deviceName = getIntent().getStringExtra("deviceName");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        tvDeviceName = findViewById(R.id.tv_device_name);
        tvRatingLabel = findViewById(R.id.tv_rating_label);
        etContent = findViewById(R.id.et_review_content);

        tvDeviceName.setText(deviceName);

        // Init stars
        int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
        for (int i = 0; i < 5; i++) {
            stars[i] = findViewById(starIds[i]);
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }
        setRating(5);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_submit_review).setOnClickListener(v -> {
            String content = etContent.getText() != null
                    ? etContent.getText().toString().trim() : "";
            if (content.isEmpty()) {
                Toast.makeText(this, "请填写评价内容", Toast.LENGTH_SHORT).show();
                return;
            }
            submitReview(content);
        });
    }

    private void setRating(int rating) {
        selectedRating = rating;
        for (int i = 0; i < 5; i++) {
            stars[i].setTextColor(i < rating
                    ? getColor(R.color.status_orange)
                    : getColor(R.color.status_gray));
        }
        tvRatingLabel.setText(RATING_LABELS[rating - 1]);
    }

    private void submitReview(String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("deviceId", deviceId);
            json.put("bookingId", bookingId);
            json.put("rating", selectedRating);
            json.put("content", content);

            RequestBody body = RequestBody.create(
                    json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/review/submit")
                    .post(body).build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ReviewActivity.this,
                            "网络错误", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    runOnUiThread(() -> {
                        switch (res) {
                            case "success":
                                Toast.makeText(ReviewActivity.this,
                                        "评价提交成功！", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                            case "already_reviewed":
                                Toast.makeText(ReviewActivity.this,
                                        "该订单已评价过了", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(ReviewActivity.this,
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