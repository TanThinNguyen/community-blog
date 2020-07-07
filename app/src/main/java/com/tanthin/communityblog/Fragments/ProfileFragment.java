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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tanthin.communityblog.Activities.HomeActivity;
import com.tanthin.communityblog.Activities.LoginActivity;
import com.tanthin.communityblog.Models.User;
import com.tanthin.communityblog.R;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CURRENT_USER = "current_user";

    // TODO: Rename and change types of parameters
   private User mCurrentUserInfo;

    //----------------------------------------------------
    private static final String TAG = "ProfileFragment";
    public static final int PICK_PERMISSION_REQUEST_CODE = 0;
    public static final int PICK_USER_PHOTO_CODE = 1;
    public static final int PICK_USER_COVER_CODE = 2;

    private int requestCode;

    private Context mContext;

    // Views
    private ImageView userPhoto, userCover;
    private TextView userName, userMail, userTotalPosts, userTotalLikes, userBio;
    private FloatingActionButton fabEditProfile;

    private Dialog popupEditProfile;
    private ImageView popupUserPhoto, popupUserCover, popupUpdate;
    private TextView popupUserName, popupUserBio;
    private ProgressBar popupEditProgress;
    private ProgressBar loadProfileProgress;

//    private User currentUserInfo;

    private Uri pickedPhotoUri;
    private Uri pickedCoverUri;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(User currentUserInfo) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(CURRENT_USER, currentUserInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentUserInfo = (User) getArguments().getSerializable(CURRENT_USER);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = container.getContext();

        // init views
        userPhoto = view.findViewById(R.id.profile_user_photo);
        userCover = view.findViewById(R.id.profile_user_cover);
        userName = view.findViewById(R.id.profile_user_name);
        userMail = view.findViewById(R.id.profile_user_mail);
        userTotalPosts = view.findViewById(R.id.profile_total_post);
        userTotalLikes = view.findViewById(R.id.profile_total_like);
        userBio = view.findViewById(R.id.profile_user_bio);
        fabEditProfile = view.findViewById(R.id.fab_edit_profile);
        loadProfileProgress = view.findViewById(R.id.load_profile_progressbar);

        // bind user's data  to views
        bindUserInfo();

        // fab click event to show popup for editing profile
        fabEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Edit profile", Toast.LENGTH_SHORT).show();
                initEditPopup();
            }
        });


        return view;
    }

    // get current user object from database and bind to user profile to views
//    private void getCurrentUserInfo() {
//        Log.d("-NTT-", "getCurrentUserInfo");
//        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference(User.KEY_USER_MODEL);
//        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        databaseRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.d("-NTT-", "onDataChange");
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Log.d("-NTT-", snapshot.getKey());
//                    if (snapshot.getKey().equals(currentUser.getUid())) {
//                        Log.d("-NTT-", "get current user object");
//                        currentUserInfo = snapshot.getValue(User.class);
//                        bindUserInfo();
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    // bind current user data (name, mail, photo, bio, ...) to views
    private void bindUserInfo() {
        Log.d(TAG, "bindUserInfo: ");
        mCurrentUserInfo = HomeActivity.getCurrentUser();

        if (mCurrentUserInfo == null) {
            Log.d("-NTT-", "currentUserInfo == null");
        }
        Log.d("-NTT-", "currentUserInfo != null, bindUserInfo");

        loadProfileProgress.setVisibility(View.VISIBLE);
        fabEditProfile.hide();
        // set name, mail, photo, cover, bio
        Glide.with(getContext()).load(mCurrentUserInfo.getUserPhoto()).into(userPhoto);
        userName.setText(mCurrentUserInfo.getName());
        userMail.setText(mCurrentUserInfo.getMail());
        Glide.with(getContext()).load(mCurrentUserInfo.getUserCover()).into(userCover);
        userBio.setText(mCurrentUserInfo.getUserBio());
        Log.d(TAG, "bindUserInfo: " + mCurrentUserInfo.getUserBio());

        loadProfileProgress.setVisibility(View.INVISIBLE);
        fabEditProfile.show();

//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(User.KEY_USER_MODEL);
//        userRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    if (snapshot.getKey().equals(currentUser.getUid())) {
//                        User user = snapshot.getValue(User.class);
//                        currentUserInfo = user;
//                        Log.d("-NTT-", "currentUserInfo != null");
//                        Glide.with(mContext).load(currentUserInfo.getUserPhoto()).into(userPhoto);
//                        userName.setText(currentUserInfo.getName());
//                        userMail.setText(currentUserInfo.getMail());
//                        Glide.with(mContext).load(currentUserInfo.getUserCover()).into(userCover);
//                        userBio.setText(currentUserInfo.getUserBio());
////                        userBio.setText(user.getUserBio());
////                        Glide.with(getContext()).load(user.getUserCover()).into(userCover);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

    // create popup for user to edit profile
    private void initEditPopup() {
        Log.d("-NTT-", "initEditPopup");

        // setup popup
        popupEditProfile = new Dialog(getContext());
        popupEditProfile.setContentView(R.layout.popup_edit_profile);
        popupEditProfile.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupEditProfile.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popupEditProfile.getWindow().getAttributes().gravity = Gravity.TOP;

        // init views of popup
        popupUserName = popupEditProfile.findViewById(R.id.popup_profile_name);
        popupUserBio = popupEditProfile.findViewById(R.id.popup_profile_bio);
        popupUserPhoto = popupEditProfile.findViewById(R.id.popup_profile_user_photo);
        popupUserCover = popupEditProfile.findViewById(R.id.popup_profile_user_cover);
        popupUpdate = popupEditProfile.findViewById(R.id.popup_profile_update);
        popupEditProgress = popupEditProfile.findViewById(R.id.popup_edit_profile_progressbar);

        // set current user's data to popup dialog (name, bio photo cover)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference userCoverRef = FirebaseStorage.getInstance().getReference()
                .child(User.KEY_USER_MODEL)
                .child(currentUser.getUid())
                .child(User.KEY_COVER);
        popupUserName.setText(mCurrentUserInfo.getName());
        popupUserBio.setText(mCurrentUserInfo.getUserBio());
        Glide.with(mContext).load(mCurrentUserInfo.getUserPhoto()).into(popupUserPhoto);
        Glide.with(mContext).load(mCurrentUserInfo.getUserCover()).into(popupUserCover);

        // user pick other user photo
        popupUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCode = PICK_USER_PHOTO_CODE;
                pickUserImage(requestCode);

            }
        });

        // user pick other cover photo
        popupUserCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCode = PICK_USER_COVER_CODE;
                pickUserImage(requestCode);

            }
        });

        // update profile click event
        popupUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }
        });

        popupEditProfile.show();
    }

    // update new user profile
    private void updateUserProfile() {
        if (popupUserName.getText().toString().isEmpty() || popupUserBio.getText().toString().isEmpty()) {
            Toast.makeText(mContext, "Please verify all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        popupUpdate.setVisibility(View.INVISIBLE);
        popupEditProgress.setVisibility(View.VISIBLE);

        updateNewUserPhoto();

        // TODO: upload new photo, cover to FirebaseStorage
        // TODO: then update to FirebaseUser
        // TODO: update to FirebaseDatabase
    }

    private void updateNewUserPhoto() {
        Log.d(TAG, "updateNewUserPhoto: ");
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // upload new photo (if user choose a new one)
        if (pickedPhotoUri != null) {
            final StorageReference photoRef = FirebaseStorage.getInstance()
                    .getReference(LoginActivity.KEY_USERS_PHOTO)
                    .child(currentUser.getUid());

            photoRef.putFile(pickedPhotoUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("-NTT-", "upload new user photo successfully");
                            // get photo url
                            photoRef.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            mCurrentUserInfo.setUserPhoto(uri.toString());
                                            updateNewUserCover();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("-NTT-", "upload new user photo fail");
                        }
                    });
        }
        else {
            updateNewUserCover();
        }
    }

    private void updateNewUserCover() {
        Log.d(TAG, "updateNewUserCover: ");
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // upload new cover (if user choose a new one)
        if (pickedCoverUri != null) {
            final StorageReference coverRef = FirebaseStorage.getInstance()
                    .getReference(LoginActivity.KEY_USERS_COVER)
                    .child(currentUser.getUid());

            coverRef.putFile(pickedCoverUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("-NTT-", "upload new user cover successfully");
                            coverRef.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            mCurrentUserInfo.setUserCover(uri.toString());
                                            updateFirebaseUser();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("-NTT-", "upload new user cover fail");
                        }
                    });
        }
        else {
            updateFirebaseUser();
        }
    }

    private void updateFirebaseUser() {
        Log.d(TAG, "updateFirebaseUser: ");
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // set new user name and bio
        if (!popupUserName.getText().toString().trim().isEmpty()) {
            mCurrentUserInfo.setName(popupUserName.getText().toString().trim());
        }
        if (!popupUserBio.getText().toString().trim().isEmpty()) {
            mCurrentUserInfo.setUserBio(popupUserBio.getText().toString().trim());
        }
        // update name, user photo to FirebaseUser
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mCurrentUserInfo.getName())
                .setPhotoUri(Uri.parse(mCurrentUserInfo.getUserPhoto()))
                .build();
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("-NTT-", "User auth profile updated successfully");
                            Log.d("-NTT-", currentUser.getPhotoUrl().toString());
                            updateFirebaseDatabase();
                        }
                    }
                });
    }

    private void updateFirebaseDatabase() {
        Log.d(TAG, "updateFirebaseDatabase: ");
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // update name, bio, user photo, user cover to FirebaseDatabase
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference(User.KEY_USER_MODEL)
                .child(currentUser.getUid());
        databaseRef.child(User.KEY_NAME).setValue(mCurrentUserInfo.getName());
        databaseRef.child(User.KEY_BIO).setValue(mCurrentUserInfo.getUserBio());
        databaseRef.child(User.KEY_PHOTO).setValue(mCurrentUserInfo.getUserPhoto());
        databaseRef.child(User.KEY_COVER).setValue(mCurrentUserInfo.getUserCover());

        Log.d("-NTT-", "Update database successfully");
        // reset dialog
        popupEditProgress.setVisibility(View.INVISIBLE);
        popupUpdate.setVisibility(View.VISIBLE);
        popupUserName.setText("");
        popupUserBio.setText("");
        popupEditProfile.dismiss();
        Toast.makeText(mContext, "Edit profile successfully", Toast.LENGTH_SHORT).show();

        // update profile
//        Log.d(TAG, "Update profile UI after editting successfully");
//        bindUserInfo();
    }

    // check runtime permission and open gallery for user to pick image (for user photo or cover)
    private void pickUserImage(int requestCode) {
        // first we check runtime permission
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PICK_PERMISSION_REQUEST_CODE);
        }
        else {
            openGallery(requestCode);
        }
    }

    // open gallery for user to pick image
    private void openGallery(int requestCode) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/");
        startActivityForResult(galleryIntent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("-NTT-", "onRequestPermissionsResult");
            openGallery(requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_USER_PHOTO_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    // TODO: update new user photo
                    pickedPhotoUri = data.getData();
                    popupUserPhoto.setImageURI(pickedPhotoUri);
                    Log.d("-NTT-", "pickedPhotoUri != null");
                }
                break;
            case PICK_USER_COVER_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    // TODO: update new user cover
                    pickedCoverUri = data.getData();
                    popupUserCover.setImageURI(pickedCoverUri);
                    Log.d("-NTT-", "pickedCoverUri != null");
                }
                break;
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
