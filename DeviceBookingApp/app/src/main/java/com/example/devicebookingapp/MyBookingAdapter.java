package com.example.devicebookingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
    private Runnable onStatusChanged;

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
        holder.tvStatus.setText(booking.getStatus());
        holder.tvDuration.setText("使用时长：" + booking.getDuration() + " 分钟");
        holder.tvStart.setText(booking.getStartTime() != null ? booking.getStartTime() : "--");
        holder.tvEnd.setText(booking.getEndTime() != null ? booking.getEndTime() : "--");

        holder.btnCancel.setVisibility(View.GONE);
        holder.btnAction.setVisibility(View.GONE);

        switch (booking.getStatus() != null ? booking.getStatus() : "") {
            case "未开始":
                applyStatus(holder, R.color.status_orange, R.color.status_orange_bg);
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText("开始使用");
                holder.btnCancel.setOnClickListener(v -> confirmAction(
                        "取消预约", "确定要取消这个预约吗？", booking.getId(), "cancel"));
                holder.btnAction.setOnClickListener(v -> confirmAction(
                        "开始使用", "确认开始使用「" + booking.getDeviceName() + "」？",
                        booking.getId(), "start"));
                break;

            case "使用中":
                applyStatus(holder, R.color.status_green, R.color.status_green_bg);
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText("完成使用");
                holder.btnAction.setOnClickListener(v -> confirmAction(
                        "完成使用", "确认已使用完毕，提交归还？", booking.getId(), "finish"));
                break;

            case "已完成":
                // 已完成 = 等待管理员审核，不能评价
                applyStatus(holder, R.color.status_orange, R.color.status_orange_bg);
                holder.tvStatus.setText("已完成（待审核）");
                break;

            case "已审核":
                // 管理员审核通过，才能评价
                applyStatus(holder, R.color.status_blue, R.color.status_blue_bg);
                if (booking.getReviewed() == null || booking.getReviewed() == 0) {
                    holder.btnAction.setVisibility(View.VISIBLE);
                    holder.btnAction.setText("写评价");
                    holder.btnAction.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ReviewActivity.class);
                        intent.putExtra("deviceId", booking.getDeviceId());
                        intent.putExtra("bookingId", booking.getId());
                        intent.putExtra("deviceName", booking.getDeviceName());
                        context.startActivity(intent);
                    });
                }
                break;

            case "已取消":
                applyStatus(holder, R.color.status_gray, R.color.status_gray_bg);
                break;

            default:
                applyStatus(holder, R.color.status_gray, R.color.status_gray_bg);
        }
    }

    private void applyStatus(ViewHolder holder, int textColorRes, int bgColorRes) {
        holder.tvStatus.setTextColor(context.getColor(textColorRes));
        holder.cvStatusBadge.setCardBackgroundColor(context.getColor(bgColorRes));
    }

    private void confirmAction(String title, String message, Integer bookingId, String action) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确认", (d, w) -> updateStatus(bookingId, action))
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateStatus(Integer bookingId, String action) {
        try {
            JSONObject json = new JSONObject();
            json.put("bookingId", bookingId);
            json.put("action", action);
            RequestBody body = RequestBody.create(
                    json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/booking/updateStatus")
                    .post(body).build();
            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "操作失败，请检查网络", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show();
                            if (onStatusChanged != null) onStatusChanged.run();
                        }
                    });
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override public int getItemCount() { return bookingList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName, tvStatus, tvDuration, tvStart, tvEnd;
        MaterialButton btnCancel, btnAction;
        MaterialCardView cvStatusBadge;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_booking_device);
            tvStatus = itemView.findViewById(R.id.tv_booking_status);
            tvDuration = itemView.findViewById(R.id.tv_booking_duration);
            tvStart = itemView.findViewById(R.id.tv_booking_start);
            tvEnd = itemView.findViewById(R.id.tv_booking_end);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnAction = itemView.findViewById(R.id.btn_action);
            cvStatusBadge = itemView.findViewById(R.id.cv_booking_status_badge);
        }
    }
}