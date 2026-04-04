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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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

public class NoticeFragment extends Fragment {

    private RecyclerView recyclerView;
    private NoticeAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<Notice> noticeList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);

        recyclerView = view.findViewById(R.id.rv_notices);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_notice);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NoticeAdapter(noticeList, getContext(), notice ->
                Toast.makeText(getContext(), notice.getTitle(), Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchNotices);

        fetchNotices();
        return view;
    }

    private void fetchNotices() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/notice/list")
                .get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "获取公告失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                String json = response.body().string();
                Gson gson = new Gson();
                Type type = new TypeToken<List<Notice>>(){}.getType();
                List<Notice> fetched = gson.fromJson(json, type);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    noticeList.clear();
                    if (fetched != null) noticeList.addAll(fetched);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
}