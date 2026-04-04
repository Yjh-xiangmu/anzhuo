package com.example.devicebookingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MineFragment extends Fragment {

    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "未知用户");
        String role = prefs.getString("role", "student");

        // Header
        TextView tvUserName = view.findViewById(R.id.tv_user_name);
        TextView tvUserRole = view.findViewById(R.id.tv_user_role);
        TextView tvAvatarLetter = view.findViewById(R.id.tv_avatar_letter);

        tvUserName.setText(username);
        tvUserRole.setText("admin".equals(role) ? "管理员" : "普通用户");
        if (username.length() > 0) {
            tvAvatarLetter.setText(username.substring(0, 1).toUpperCase());
        }

        // Admin panel
        MaterialCardView cvAdmin = view.findViewById(R.id.cv_admin_panel);
        if ("admin".equals(role)) {
            cvAdmin.setVisibility(View.VISIBLE);
            cvAdmin.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), AdminActivity.class)));
        }

        fetchStats(username, view);

        // Menu items
        view.findViewById(R.id.cv_my_booking).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), MyBookingActivity.class)));

        view.findViewById(R.id.ll_my_reviews).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), MyReviewsActivity.class)));

        view.findViewById(R.id.ll_my_repairs).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), MyRepairsActivity.class)));

        view.findViewById(R.id.ll_change_password).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("退出登录")
                        .setMessage("确定要退出登录吗？")
                        .setPositiveButton("退出", (d, w) -> {
                            prefs.edit().clear().apply();
                            Toast.makeText(getActivity(), "已退出登录", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", null)
                        .show());

        return view;
    }

    private void fetchStats(String username, View view) {
        TextView tvTotal = view.findViewById(R.id.tv_stat_total);
        TextView tvOngoing = view.findViewById(R.id.tv_stat_ongoing);
        TextView tvDone = view.findViewById(R.id.tv_stat_done);

        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/booking/stats?username=" + username)
                .get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                String json = response.body().string();
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(json);
                    int total = obj.optInt("total", 0);
                    int ongoing = obj.optInt("ongoing", 0);
                    int done = obj.optInt("done", 0);
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        tvTotal.setText(String.valueOf(total));
                        tvOngoing.setText(String.valueOf(ongoing));
                        tvDone.setText(String.valueOf(done));
                    });
                } catch (Exception ignored) {}
            }
        });
    }
}