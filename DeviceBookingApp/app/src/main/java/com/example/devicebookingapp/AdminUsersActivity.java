package com.example.devicebookingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private List<UserInfo> userList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        recyclerView = findViewById(R.id.rv_users);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new UserAdapter());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchUsers);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        fetchUsers();
    }

    private void fetchUsers() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/user/allList").get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(AdminUsersActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<UserInfo>>(){}.getType();
                List<UserInfo> list = new Gson().fromJson(response.body().string(), type);
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    userList.clear();
                    if (list != null) userList.addAll(list);
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
            }
        });
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            UserInfo u = userList.get(pos);
            String initial = u.getUsername() != null && u.getUsername().length() > 0
                    ? u.getUsername().substring(0, 1) : "U";
            h.tvAvatar.setText(initial);
            h.tvUsername.setText(u.getUsername());
            String roleLabel = "admin".equals(u.getRole()) ? "管理员"
                    : "teacher".equals(u.getRole()) ? "教师" : "学生";
            h.tvRole.setText(roleLabel);
            boolean isAdmin = "admin".equals(u.getRole());
            h.tvRole.setTextColor(getColor(isAdmin ? R.color.brand_primary : R.color.text_secondary));
            h.tvAvatar.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(isAdmin ? R.color.brand_primary : R.color.status_gray)));
        }
        @Override public int getItemCount() { return userList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvUsername, tvRole;
            VH(@NonNull View v) {
                super(v);
                tvAvatar = v.findViewById(R.id.tv_avatar);
                tvUsername = v.findViewById(R.id.tv_username);
                tvRole = v.findViewById(R.id.tv_role);
            }
        }
    }
}