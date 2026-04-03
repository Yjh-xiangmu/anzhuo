package com.example.devicebookingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
        holder.tvType.setText("分类：" + device.getDeviceType());
        holder.tvLocation.setText("位置：" + device.getLocation());
        holder.tvStatus.setText(device.getStatus());

        if ("空闲".equals(device.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.btnBook.setEnabled(true);
            holder.btnBook.setText("预约");
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
            holder.btnBook.setEnabled(false);
            holder.btnBook.setText("不可用");
        }

        // 核心：点击预约按钮触发选择器
        holder.btnBook.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            String username = prefs.getString("username", "");

            if (!isLoggedIn) {
                Toast.makeText(context, "请登录后使用预约功能", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, LoginActivity.class));
                return;
            }

            // 1. 选日期
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(context, (view1, year, month, dayOfMonth) -> {
                String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);

                // 2. 选时间
                new TimePickerDialog(context, (view2, hourOfDay, minute) -> {
                    String fullTime = date + " " + String.format("%02d:%02d:00", hourOfDay, minute);

                    // 3. 选时长
                    String[] times = {"10分钟", "20分钟", "30分钟"};
                    new AlertDialog.Builder(context)
                            .setTitle("请选择使用时长")
                            .setItems(times, (dialog, which) -> {
                                int duration = (which + 1) * 10;
                                // 4. 提交到后端
                                submitBookingToServer(device.getId(), username, duration, fullTime, device.getDeviceName());
                            })
                            .show();

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    @Override
    public int getItemCount() { return deviceList.size(); }

    private void submitBookingToServer(Integer deviceId, String username, int duration, String startTime, String deviceName) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("deviceId", deviceId);
            jsonBody.put("username", username);
            jsonBody.put("duration", duration);
            jsonBody.put("startTime", startTime);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(), MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/booking/submit")
                    .post(body)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "网络错误，预约失败", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String res = response.body().string();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if ("success".equals(res)) {
                            Toast.makeText(context, deviceName + " 预约成功！", Toast.LENGTH_SHORT).show();
                        } else if ("limit_error".equals(res)) {
                            Toast.makeText(context, "预约失败：您已有一个同类型的设备订单", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "预约失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvLocation, tvStatus;
        MaterialButton btnBook;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvType = itemView.findViewById(R.id.tv_item_type);
            tvLocation = itemView.findViewById(R.id.tv_item_location);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            btnBook = itemView.findViewById(R.id.btn_item_book);
        }
    }
}