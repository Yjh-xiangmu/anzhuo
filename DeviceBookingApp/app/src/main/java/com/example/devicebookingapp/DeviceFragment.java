package com.example.devicebookingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private List<Device> deviceList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);

        recyclerView = view.findViewById(R.id.rv_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeviceAdapter(deviceList, getContext());
        recyclerView.setAdapter(adapter);

        fetchDeviceData();

        return view;
    }

    private void fetchDeviceData() {
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/device/list")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "获取设备列表失败", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonStr = response.body().string();

                    // 使用 Gson 将 JSON 字符串转换为 List<Device>
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Device>>(){}.getType();
                    List<Device> fetchedList = gson.fromJson(jsonStr, type);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            deviceList.clear();
                            deviceList.addAll(fetchedList);
                            adapter.notifyDataSetChanged();
                        });
                    }
                }
            }
        });
    }
}