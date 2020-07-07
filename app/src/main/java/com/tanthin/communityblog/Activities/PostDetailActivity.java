package com.tanthin.communityblog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tanthin.communityblog.Adapters.CommentAdapter;
import com.tanthin.communityblog.Models.Comment;
import com.tanthin.communityblog.Models.Post;
import com.tanthin.communityblog.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView imgPost, imgUserPost, imgCurrentUser;
    private TextView tvPostTitle, tvPostDesc, tvPostDateName, tvByUser;
    private EditText etComment;
    private Button btnAddComment;

    private RecyclerView recyclerComment;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private String postKey;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // set the status bar to transparent, hide the action bar
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        // init view
        imgPost = findViewById(R.id.post_detail_img);
        imgUserPost = findViewById(R.id.post_detail_user_img);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img);

        tvPostTitle = findViewById(R.id.post_detail_title);
        tvPostDesc = findViewById(R.id.post_detail_description);
        tvPostDateName = findViewById(R.id.post_detail_date_name);
        tvByUser = findViewById(R.id.post_detail_by_user);

        etComment = findViewById(R.id.post_detail_comment);
        btnAddComment = findViewById(R.id.post_detail_add_comment_btn);
        recyclerComment = findViewById(R.id.recyclerview_comment);

        // get current user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // get database
        database = FirebaseDatabase.getInstance();

        // add comment btn click event
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etComment.getText().toString().isEmpty()) {
                    Toast.makeText(PostDetailActivity.this, "Please write comment", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnAddComment.setEnabled(false);
                String content = etComment.getText().toString();
                String uId = currentUser.getUid();
                String uName = currentUser.getDisplayName();
                String uImg = currentUser.getPhotoUrl().toString();
                Comment comment = new Comment(content, uId, uImg, uName);

                DatabaseReference commentRef = database.getReference("Comments").child(postKey).push();
                comment.setCommentKey(commentRef.getKey());
                commentRef.setValue(comment)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(PostDetailActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                                etComment.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostDetailActivity.this, "Fail to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                btnAddComment.setEnabled(true);
            }
        });

        // bind all data to views
        bindDataToViews();

        // init recycler view comment
        initRvComment();
    }

    private void bindDataToViews() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Post post = null;
        if (bundle != null) {
            post = (Post) bundle.getSerializable("PostData");
        }
        if (post != null) {
            // set title, description, picture, user post image
            Glide.with(PostDetailActivity.this).load(post.getPicture()).into(imgPost);
            Glide.with(PostDetailActivity.this).load(post.getUserPhoto()).into(imgUserPost);
            tvPostTitle.setText(post.getTitle());
            tvPostDesc.setText(post.getDescription());
            // set by user
            tvByUser.setText("by " + post.getUserName());

            // set date post
            String date = post.getTimeStamp().toString();
            tvPostDateName.setText(timeStampToString(Long.valueOf(date)));

            // get postID
            postKey = post.getPostKey();
        }
        //set comment user image
        Glide.with(PostDetailActivity.this).load(currentUser.getPhotoUrl()).into(imgCurrentUser);
    }

    // init comments recycler view
    private void initRvComment() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(PostDetailActivity.this, commentList);

        recyclerComment.hasFixedSize();
        recyclerComment.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerComment.setAdapter(commentAdapter);

        DatabaseReference commentRef = database.getReference("Comments").child(postKey);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    commentList.add(data.getValue(Comment.class));
                    commentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private String timeStampToString(long time) {
        // java.util
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        // android.text.format
        String currentDate = DateFormat.format("dd-MM-yyyy", calendar).toString();

        return currentDate;
    }
}
