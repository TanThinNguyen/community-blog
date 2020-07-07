package com.tanthin.communityblog.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tanthin.communityblog.Models.Comment;
import com.tanthin.communityblog.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mData;

    public CommentAdapter(Context mContext, List<Comment> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = mData.get(position);

        holder.tvName.setText(comment.getuName());
        holder.tvContent.setText(comment.getContent());
        holder.tvDate.setText(timestampToString(Long.valueOf(comment.getTimestamp().toString())));
        Glide.with(mContext).load(comment.getuImg()).into(holder.imgUser);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String currentTime = DateFormat.format("HH:mm", calendar).toString();
        return currentTime;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgUser;
        TextView tvName, tvContent, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUser = itemView.findViewById(R.id.comment_user_img);
            tvName = itemView.findViewById(R.id.comment_username);
            tvContent = itemView.findViewById(R.id.comment_content);
            tvDate = itemView.findViewById(R.id.comment_date);
        }
    }
}
