package com.example.devicebookingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminNoticesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private List<Notice> noticeList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notices);

        recyclerView = findViewById(R.id.rv_notices);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new NoticeAdapter2());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(this::fetchNotices);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_notice).setOnClickListener(v -> showNoticeDialog(null));
        fetchNotices();
    }

    private void fetchNotices() {
        Request req = new Request.Builder()
                .url("http://192.168.10.105:8080/api/notice/list").get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                Type type = new TypeToken<List<Notice>>(){}.getType();
                List<Notice> list = new Gson().fromJson(response.body().string(), type);
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    noticeList.clear();
                    if (list != null) noticeList.addAll(list);
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
            }
        });
    }

    private void showNoticeDialog(Notice notice) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        EditText etTitle = new EditText(this);
        etTitle.setHint("公告标题");
        etTitle.setPadding(0, 8, 0, 8);
        layout.addView(etTitle);

        EditText etContent = new EditText(this);
        etContent.setHint("公告内容");
        etContent.setMinLines(4);
        etContent.setGravity(android.view.Gravity.TOP);
        etContent.setPadding(0, 8, 0, 8);
        layout.addView(etContent);

        if (notice != null) {
            etTitle.setText(notice.getTitle());
            etContent.setText(notice.getContent());
        }

        new AlertDialog.Builder(this)
                .setTitle(notice == null ? "发布公告" : "编辑公告")
                .setView(layout)
                .setPositiveButton("保存", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (notice == null) addNotice(title, content);
                    else editNotice(notice.getId(), title, content);
                })
                .setNegativeButton("取消", null).show();
    }

    private void addNotice(String title, String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("title", title);
            json.put("content", content);
            post("http://192.168.10.105:8080/api/notice/add", json.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void editNotice(Integer id, String title, String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("title", title);
            json.put("content", content);
            post("http://192.168.10.105:8080/api/notice/update", json.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteNotice(Integer id) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            post("http://192.168.10.105:8080/api/notice/delete", json.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void post(String url, String body) {
        Request req = new Request.Builder().url(url)
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8"))).build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminNoticesActivity.this,
                        "操作失败", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                runOnUiThread(() -> {
                    Toast.makeText(AdminNoticesActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                    fetchNotices();
                });
            }
        });
    }

    class NoticeAdapter2 extends RecyclerView.Adapter<NoticeAdapter2.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_notice, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Notice n = noticeList.get(pos);
            h.tvTitle.setText(n.getTitle());
            h.tvContent.setText(n.getContent());
            h.tvDate.setText(n.getCreatedAt());
            h.btnEdit.setOnClickListener(v -> showNoticeDialog(n));
            h.btnDelete.setOnClickListener(v ->
                    new AlertDialog.Builder(AdminNoticesActivity.this)
                            .setTitle("删除公告")
                            .setMessage("确定删除「" + n.getTitle() + "」？")
                            .setPositiveButton("删除", (d, w) -> deleteNotice(n.getId()))
                            .setNegativeButton("取消", null).show());
        }
        @Override public int getItemCount() { return noticeList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvContent, tvDate;
            MaterialButton btnEdit, btnDelete;
            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_notice_title);
                tvContent = v.findViewById(R.id.tv_notice_content);
                tvDate = v.findViewById(R.id.tv_notice_date);
                btnEdit = v.findViewById(R.id.btn_edit);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}