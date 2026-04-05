package com.example.devicebookingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Calendar;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<Device> deviceList;
    private Context context;

    public DeviceAdapter(List<Device> deviceList, Context context) {
        this.deviceList = deviceList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.tvName.setText(device.getDeviceName());
        holder.tvTypeBadge.setText(device.getDeviceType());
        holder.tvLocation.setText(device.getLocation());

        String status = device.getStatus() != null ? device.getStatus() : "";
        holder.tvStatus.setText(status);

        boolean isFree = "空闲".equals(status);
        boolean isLocked = "故障锁定".equals(status);

        if (isFree) {
            holder.tvStatus.setTextColor(context.getColor(R.color.status_green));
            holder.cvStatusBadge.setCardBackgroundColor(context.getColor(R.color.status_green_bg));
            holder.btnBook.setEnabled(true);
            holder.btnBook.setAlpha(1.0f);
            holder.btnBook.setText("立即预约");
        } else if (isLocked) {
            holder.tvStatus.setTextColor(context.getColor(R.color.status_red));
            holder.cvStatusBadge.setCardBackgroundColor(context.getColor(R.color.status_red_bg));
            holder.btnBook.setEnabled(false);
            holder.btnBook.setAlpha(0.4f);
            holder.btnBook.setText("维修中");
        } else {
            holder.tvStatus.setTextColor(context.getColor(R.color.status_red));
            holder.cvStatusBadge.setCardBackgroundColor(context.getColor(R.color.status_red_bg));
            holder.btnBook.setEnabled(false);
            holder.btnBook.setAlpha(0.5f);
            holder.btnBook.setText("不可预约");
        }

        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, DeviceDetailActivity.class);
            intent.putExtra("deviceId", device.getId());
            intent.putExtra("deviceName", device.getDeviceName());
            intent.putExtra("deviceType", device.getDeviceType());
            intent.putExtra("location", device.getLocation());
            intent.putExtra("status", status);
            context.startActivity(intent);
        });

        holder.btnBook.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            if (!prefs.getBoolean("isLoggedIn", false)) {
                Toast.makeText(context, "请登录后使用预约功能", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, LoginActivity.class));
                return;
            }
            if (isLocked) {
                Toast.makeText(context, "该设备正在维修中，暂无法预约", Toast.LENGTH_SHORT).show();
                return;
            }
            showBookingPicker(device, prefs.getString("username", ""));
        });
    }

    private void showBookingPicker(Device device, String username) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(context,
                (view, year, month, day) -> {
                    boolean isToday = (year == now.get(Calendar.YEAR)
                            && month == now.get(Calendar.MONTH)
                            && day == now.get(Calendar.DAY_OF_MONTH));
                    int minHour = isToday ? now.get(Calendar.HOUR_OF_DAY) : 0;
                    int minMinute = isToday ? now.get(Calendar.MINUTE) : 0;

                    new TimePickerDialog(context, (view2, hour, minute) -> {
                        if (isToday) {
                            Calendar chosen = Calendar.getInstance();
                            chosen.set(year, month, day, hour, minute, 0);
                            if (chosen.before(now)) {
                                Toast.makeText(context, "所选时间已过去，请选择未来的时间",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                        String fullTime = date + " " + String.format("%02d:%02d:00", hour, minute);
                        new AlertDialog.Builder(context)
                                .setTitle("选择使用时长")
                                .setItems(new String[]{"10 分钟", "20 分钟", "30 分钟"}, (dialog, which) -> {
                                    int duration = (which + 1) * 10;
                                    new AlertDialog.Builder(context)
                                            .setTitle("确认预约")
                                            .setMessage("设备：" + device.getDeviceName()
                                                    + "\n开始时间：" + fullTime
                                                    + "\n使用时长：" + duration + " 分钟")
                                            .setPositiveButton("确认预约", (d, w) ->
                                                    submitBooking(device.getId(), username,
                                                            duration, fullTime, device.getDeviceName()))
                                            .setNegativeButton("取消", null).show();
                                }).show();
                    }, minHour, minMinute, true).show();
                },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
        datePicker.show();
    }

    private void submitBooking(Integer deviceId, String username, int duration,
                               String startTime, String deviceName) {
        try {
            JSONObject json = new JSONObject();
            json.put("deviceId", deviceId);
            json.put("username", username);
            json.put("duration", duration);
            json.put("startTime", startTime);
            RequestBody body = RequestBody.create(
                    json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/booking/submit").post(body).build();
            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "网络错误，预约失败", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    String res = response.body().string();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        switch (res) {
                            case "success":
                                Toast.makeText(context, deviceName + " 预约成功！",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case "limit_error":
                                Toast.makeText(context, "您已有同类型设备的预约，无法重复预约",
                                        Toast.LENGTH_LONG).show();
                                break;
                            case "conflict":
                                Toast.makeText(context, "该时间段已被预约，请选择其他时间",
                                        Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(context, "预约失败，请稍后重试",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override public int getItemCount() { return deviceList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTypeBadge, tvLocation, tvStatus;
        MaterialButton btnBook, btnDetail;
        MaterialCardView cvStatusBadge;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvTypeBadge = itemView.findViewById(R.id.tv_item_type_badge);
            tvLocation = itemView.findViewById(R.id.tv_item_location);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            btnBook = itemView.findViewById(R.id.btn_item_book);
            btnDetail = itemView.findViewById(R.id.btn_item_detail);
            cvStatusBadge = itemView.findViewById(R.id.cv_status_badge);
        }
    }
}