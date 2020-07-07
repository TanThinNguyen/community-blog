package com.tanthin.communityblog.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tanthin.communityblog.Adapters.PostAdapter;
import com.tanthin.communityblog.Models.Post;
import com.tanthin.communityblog.Models.User;
import com.tanthin.communityblog.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //----------------------------------------------------
    private static final int PICK_PERMISSION_REQUEST_CODE = 0;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;

    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference postRef;

    private Dialog popupAddPost;
    private ImageView popupUserImage, popupPostImage, popupAddBtn;
    private EditText popupTitle, popupDescription;
    private ProgressBar popupClickProgress;
    private FloatingActionButton fabAddPost;

    private Uri pickedImgUri;


    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private ProgressBar postLoading;

    private DatabaseReference dataRef;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        mContext = container.getContext();

        postLoading = fragmentView.findViewById(R.id.post_loading_progressbar);
        postRecyclerView = fragmentView.findViewById(R.id.post_recycler_view);
        fabAddPost = fragmentView.findViewById(R.id.fab_post_add);

        // get current user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        // init popup to add post
        initPopup();
        setUpPopupImageClick();

        // init newfeed recycler view
        initRecyclerViewPost();

        // fab click event to show popup for adding new post
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Add new post", Toast.LENGTH_SHORT).show();
                popupAddPost.show();
            }
        });

        return fragmentView;
    }

    // init all posts recycler view (news feed)
    private void initRecyclerViewPost() {
        // setup recycler view
        postRecyclerView.hasFixedSize();
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        postRecyclerView.setAdapter(postAdapter);

        // init reference to database
        dataRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        // get list of posts from database
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                postLoading.setVisibility(View.VISIBLE);
                for (DataSnapshot postSnap : dataSnapshot.getChildren()) {
                    postList.add(postSnap.getValue(Post.class));
                }
                postAdapter.notifyDataSetChanged();
                postLoading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // init popup dialog to add new post
    private void initPopup() {
        // init dialog
        popupAddPost = new Dialog(mContext);
        popupAddPost.setContentView(R.layout.popup_add_post);
        popupAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // popupAddPost.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        popupAddPost.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT); // popupAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popupAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        // init popup widget
        popupUserImage = popupAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popupAddPost.findViewById(R.id.popup_img);
        popupTitle = popupAddPost.findViewById(R.id.popup_title);
        popupDescription = popupAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popupAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popupAddPost.findViewById(R.id.popup_progressbar);

        // load user image
        Glide.with(mContext).load(currentUser.getPhotoUrl()).into(popupUserImage);

        // Add post click listener
        // when user has added title, description, picked image for new post, we start adding post
        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupAddBtn.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                // check empty Title, Description, Image
                if (popupTitle.getText().toString().isEmpty()
                        || popupDescription.getText().toString().isEmpty()
                        || pickedImgUri == null) {
                    Toast.makeText(mContext, "Please verify all fields and choose post image!", Toast.LENGTH_SHORT).show();

                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }
                else {
                    // everything's okay
                    // TODO: upload image with reference bucket/blog_images/postKey
                    postRef = FirebaseDatabase.getInstance().getReference().child("Posts").push();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                                                    .child("blog_images").child(postRef.getKey());
                    final StorageReference imageFilePathh = storageRef.child(pickedImgUri.getLastPathSegment());
                    imageFilePathh.putFile(pickedImgUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    imageFilePathh.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    // get download link
                                                    String imageDownloadLink = uri.toString();

                                                    // TODO: creat Post Object and add it to firebase database
                                                    Post post = new Post(postRef.getKey(),
                                                                        popupTitle.getText().toString(),
                                                                        popupDescription.getText().toString(),
                                                                        imageDownloadLink,
                                                                        currentUser.getDisplayName(),
                                                                        currentUser.getUid(),
                                                                        currentUser.getPhotoUrl().toString());

                                                    // reset edit text, image
                                                    popupDescription.setText("");
                                                    popupTitle.setText("");
                                                    popupPostImage.setImageDrawable(null);

                                                    addPost(post);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // something goes wrong when upload image
                                    Toast.makeText(mContext, "Fail: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    popupAddBtn.setVisibility(View.VISIBLE);
                                    popupClickProgress.setVisibility(View.INVISIBLE);
                                }
                            });

                }
            }
        });
    }

    // add post to the firebase database
    private void addPost(Post post) {
        // update allPost for current user on database
       /* DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(currentUser.getUid()).child("allPosts");
        dataRef.child(postRef.getKey()).setValue(post.getTitle());*/

        /*postRef.setValue(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "Post added successfully", Toast.LENGTH_SHORT).show();
                        popupAddBtn.setVisibility(View.VISIBLE);
                        popupClickProgress.setVisibility(View.INVISIBLE);
                        popupAddPost.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Failed to add post", Toast.LENGTH_SHORT).show();
                        popupAddBtn.setVisibility(View.VISIBLE);
                        popupClickProgress.setVisibility(View.INVISIBLE);
                    }
                });*/


        // add post to database to location "/Posts/postKey" and "UserPosts/uid/postKey"
        Map<String, Object> postValue = post.toMap();
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(Post.KEY_POSTS_MODEL + "/" + post.getPostKey(), postValue);
        updateValue.put(User.KEY_USER_POSTS_MODEL + "/" + currentUser.getUid() + "/" + post.getPostKey(), postValue);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.updateChildren(updateValue, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(mContext, "Post added successfully", Toast.LENGTH_SHORT).show();
                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                    popupAddPost.dismiss();
                } else {
                    Toast.makeText(mContext, "Failed to add post", Toast.LENGTH_SHORT).show();
                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    // click event to pick a image for new post
    private void setUpPopupImageClick() {
        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // first we check runtime permission, then open the gallery
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PICK_PERMISSION_REQUEST_CODE);
                }
                else {
                    openGallery();
                }
            }
        });
    }

    // open gallery for user to pick image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/");
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PICK_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }

    // when user has picked an image
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            pickedImgUri = data.getData();
            popupPostImage.setImageURI(pickedImgUri);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
