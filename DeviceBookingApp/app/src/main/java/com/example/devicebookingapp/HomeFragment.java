package com.example.devicebookingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvStatAvailable, tvStatMyBookings;
    private LinearLayout llNoticeList;
    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvStatAvailable = view.findViewById(R.id.tv_stat_available);
        tvStatMyBookings = view.findViewById(R.id.tv_stat_mybookings);
        llNoticeList = view.findViewById(R.id.ll_notice_list);

        // Greeting
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean("isLoggedIn", false);
        String username = prefs.getString("username", "");
        if (loggedIn && !username.isEmpty()) {
            tvGreeting.setText("你好，" + username + " 👋");
        } else {
            tvGreeting.setText("你好，同学 👋");
        }

        // Quick book card
        view.findViewById(R.id.cv_quick_book).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToDeviceTab();
            }
        });

        // More notices
        view.findViewById(R.id.tv_more_notice).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToNoticeTab();
            }
        });

        fetchStats(loggedIn, username);
        fetchLatestNotices();

        return view;
    }

    private void fetchStats(boolean loggedIn, String username) {
        // Fetch available device count
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/device/availableCount")
                .get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) return;
                String count = response.body().string();
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> tvStatAvailable.setText(count));
            }
        });

        // Fetch my active bookings count
        if (loggedIn && !username.isEmpty()) {
            Request req2 = new Request.Builder()
                    .url("http://192.168.10.105:8080/api/booking/activeCount?username=" + username)
                    .get().build();
            client.newCall(req2).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.body() == null) return;
                    String count = response.body().string();
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> tvStatMyBookings.setText(count));
                }
            });
        } else {
            tvStatMyBookings.setText("0");
        }
    }

    private void fetchLatestNotices() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/notice/list?limit=3")
                .get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                String json = response.body().string();
                Gson gson = new Gson();
                Type type = new TypeToken<List<Notice>>(){}.getType();
                List<Notice> list = gson.fromJson(json, type);
                if (getActivity() == null || list == null) return;
                getActivity().runOnUiThread(() -> {
                    llNoticeList.removeAllViews();
                    for (Notice n : list) {
                        View item = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_notice_home, llNoticeList, false);
                        ((TextView) item.findViewById(R.id.tv_notice_title)).setText(n.getTitle());
                        ((TextView) item.findViewById(R.id.tv_notice_date)).setText(n.getCreatedAt());
                        llNoticeList.addView(item);
                    }
                });
            }
        });
    }
}