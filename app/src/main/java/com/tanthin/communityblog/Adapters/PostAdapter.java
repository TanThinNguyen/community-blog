package com.tanthin.communityblog.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tanthin.communityblog.Activities.PostDetailActivity;
import com.tanthin.communityblog.Models.Post;
import com.tanthin.communityblog.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    Context mContext;
    List<Post> mData;

    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mData.get(position);
        // set title, author, views
        holder.tvTitle.setText(post.getTitle());
        holder.tvByUser.setText("by " + post.getUserName());
        holder.tvViews.setText(mContext.getResources().getQuantityString(R.plurals.num_of_views, post.currentViews(), post.currentViews()));
        // set post image, user photo
        Glide.with(mContext).load(post.getPicture()).into(holder.imgPost);
        Glide.with(mContext).load(post.getUserPhoto()).into(holder.imgPostProfile);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvByUser, tvViews;
        ImageView imgPost;
        ImageView imgPostProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.row_post_title);
            tvByUser = itemView.findViewById(R.id.row_post_by_user);
            tvViews = itemView.findViewById(R.id.row_post_views);
            imgPost = itemView.findViewById(R.id.row_post_img);
            imgPostProfile = itemView.findViewById(R.id.row_post_profile_imd);

            // item click event for user to see all detail of a post
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send Post data to PostDetailActivity
                    Intent intent = new Intent(mContext, PostDetailActivity.class);
                    Post post = mData.get(getAdapterPosition());

                    // update num of post's views
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (!post.getUserId().equals(currentUser.getUid())) {
                        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Posts")
                                .child(post.getPostKey()).child("views").child(currentUser.getUid());
                        dataRef.setValue(1);
                    }

                    intent.putExtra("PostData", post);
                    mContext.startActivity(intent);
                }
            });
        }


    }
}
