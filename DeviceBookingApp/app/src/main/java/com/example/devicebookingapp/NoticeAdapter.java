package com.example.devicebookingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private List<Notice> list;
    private Context context;
    private OnNoticeClickListener listener;

    public interface OnNoticeClickListener {
        void onClick(Notice notice);
    }

    public NoticeAdapter(List<Notice> list, Context context, OnNoticeClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notice n = list.get(position);
        holder.tvTitle.setText(n.getTitle());
        holder.tvContent.setText(n.getContent());
        holder.tvDate.setText(n.getCreatedAt());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(n);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notice_title);
            tvContent = itemView.findViewById(R.id.tv_notice_content);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
        }
    }
}