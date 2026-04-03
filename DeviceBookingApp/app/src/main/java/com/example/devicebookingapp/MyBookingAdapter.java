package com.example.devicebookingapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyBookingAdapter extends RecyclerView.Adapter<MyBookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private Context context;
    private Runnable onStatusChanged; // 状态改变后通知页面刷新的回调

    public MyBookingAdapter(List<Booking> bookingList, Context context, Runnable onStatusChanged) {
        this.bookingList = bookingList;
        this.context = context;
        this.onStatusChanged = onStatusChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.tvDeviceName.setText(booking.getDeviceName());
        holder.tvTime.setText("使用时间：\n" + booking.getStartTime() + " \n至 " + booking.getEndTime());
        holder.tvStatus.setText(booking.getStatus());

        // 根据状态动态调整按钮显示
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnAction.setVisibility(View.GONE);

        if ("未开始".equals(booking.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("开始使用");

            holder.btnCancel.setOnClickListener(v -> updateBookingStatus(booking.getId(), "cancel"));
            holder.btnAction.setOnClickListener(v -> updateBookingStatus(booking.getId(), "start"));

        } else if ("使用中".equals(booking.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("完成使用");

            holder.btnAction.setOnClickListener(v -> updateBookingStatus(booking.getId(), "finish"));

        } else {
            // 已完成、已取消等状态，隐藏所有操作按钮
            holder.tvStatus.setTextColor(Color.parseColor("#757575"));
        }
    }

    @Override
    public int getItemCount() { return bookingList.size(); }

    private void updateBookingStatus(Integer bookingId, String action) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("bookingId", bookingId);
            jsonBody.put("action", action);

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url("http://10.0.2.2:8080/api/booking/updateStatus").post(body).build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show();
                            if (onStatusChanged != null) onStatusChanged.run(); // 触发页面重新拉取数据
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName, tvStatus, tvTime;
        MaterialButton btnCancel, btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_booking_device);
            tvStatus = itemView.findViewById(R.id.tv_booking_status);
            tvTime = itemView.findViewById(R.id.tv_booking_time);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}