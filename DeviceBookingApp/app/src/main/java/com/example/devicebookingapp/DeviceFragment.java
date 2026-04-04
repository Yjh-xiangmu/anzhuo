package com.example.devicebookingapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
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

public class DeviceFragment extends Fragment {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llFilterChips;
    private List<Device> deviceList = new ArrayList<>();
    private List<Device> allDevices = new ArrayList<>();
    private String currentFilter = "全部";
    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);

        recyclerView = view.findViewById(R.id.rv_devices);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_device);
        llFilterChips = view.findViewById(R.id.ll_filter_chips);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeviceAdapter(deviceList, getContext());
        recyclerView.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchDeviceData);

        fetchDeviceData();
        return view;
    }

    private void setupFilterChips(List<Device> devices) {
        // Collect unique types
        List<String> types = new ArrayList<>();
        types.add("全部");
        for (Device d : devices) {
            if (d.getDeviceType() != null && !types.contains(d.getDeviceType())) {
                types.add(d.getDeviceType());
            }
        }
        // Also add status filters
        types.add("空闲");

        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            llFilterChips.removeAllViews();
            for (String type : types) {
                Chip chip = new Chip(getContext());
                chip.setText(type);
                chip.setCheckable(true);
                chip.setChecked(type.equals(currentFilter));
                chip.setChipBackgroundColorResource(
                        type.equals(currentFilter) ? R.color.brand_primary : R.color.bg_input);
                chip.setTextColor(type.equals(currentFilter)
                        ? Color.WHITE
                        : getResources().getColor(R.color.text_secondary, null));
                chip.setOnClickListener(v -> {
                    currentFilter = type;
                    setupFilterChips(allDevices);
                    applyFilter();
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(8);
                chip.setLayoutParams(params);
                llFilterChips.addView(chip);
            }
        });
    }

    private void applyFilter() {
        deviceList.clear();
        for (Device d : allDevices) {
            if ("全部".equals(currentFilter)) {
                deviceList.add(d);
            } else if ("空闲".equals(currentFilter)) {
                if ("空闲".equals(d.getStatus())) deviceList.add(d);
            } else {
                if (currentFilter.equals(d.getDeviceType())) deviceList.add(d);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchDeviceData() {
        Request request = new Request.Builder()
                .url("http://192.168.10.105:8080/api/device/list")
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "获取设备列表失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                String json = response.body().string();
                Gson gson = new Gson();
                Type type = new TypeToken<List<Device>>(){}.getType();
                List<Device> fetched = gson.fromJson(json, type);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    allDevices.clear();
                    if (fetched != null) allDevices.addAll(fetched);
                    setupFilterChips(allDevices);
                    applyFilter();
                });
            }
        });
    }
}