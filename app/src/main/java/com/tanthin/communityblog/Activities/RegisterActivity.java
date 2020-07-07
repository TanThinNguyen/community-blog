package com.tanthin.communityblog.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tanthin.communityblog.Models.User;
import com.tanthin.communityblog.R;

public class RegisterActivity extends AppCompatActivity {

    private static final int PHOTO_REQUEST_CODE = 1;
    private static final int INTENT_REQUEST_CODE = 2;

    private ImageView imgUserPhoto;
    private Uri pickedImgUri;

    private EditText userEmail, userPassword, userPassword2, userName;
    private ProgressBar loadingProgress;
    private Button registerBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // init view
        userEmail = findViewById(R.id.registerMail);
        userPassword = findViewById(R.id.registerPassword);
        userPassword2 = findViewById(R.id.registerPassword2);
        userName = findViewById(R.id.registerName);
        imgUserPhoto = findViewById(R.id.registerUserPhoto);
        loadingProgress = findViewById(R.id.loadingProgress);
        registerBtn = findViewById(R.id.registerBtn);

        loadingProgress.setVisibility(View.INVISIBLE);

        // Register button event
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);

                String mail = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString().trim();
                String password2 = userPassword2.getText().toString().trim();
                String name = userName.getText().toString().trim();

                if (mail.isEmpty() || password.isEmpty() || password2.isEmpty() || name.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please verify all fields!", Toast.LENGTH_SHORT).show();
                    registerBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                    return;
                }

                if (!password.equals(password2)) {
                    Toast.makeText(RegisterActivity.this, "Password don't match!", Toast.LENGTH_SHORT).show();
                    registerBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                    return;
                }

                if (pickedImgUri == null) {
                    Toast.makeText(RegisterActivity.this, "Please pick a photo!", Toast.LENGTH_SHORT).show();
                    registerBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                    return;
                }

                // everything is ok, we create user account
                createUserAccount(mail, name, password);
            }
        });

        // Pick user photo event
        imgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // runtime permission on SDK >= 22
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndroidRequestForPermission();
                }
                else {
                    openGallery();
                }
            }
        });

    }

    private void createUserAccount(String mail, final String name, String password) {
        mAuth = FirebaseAuth.getInstance();

        // create user account
        mAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // user account created successfully
                            Toast.makeText(RegisterActivity.this, "Account created!", Toast.LENGTH_SHORT).show();

                            // after that, we update his profile picture and name
                            updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());
                        }
                        else {
                            // user account created failed
                            Toast.makeText(RegisterActivity.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            registerBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {
        // first, upload user photo to firebase storage and get photo url
        final StorageReference imageFilePath = FirebaseStorage.getInstance().getReference()
                                                .child(LoginActivity.KEY_USERS_PHOTO)
                                                .child(mAuth.getCurrentUser().getUid());    // bucket/users_photo/uid

        imageFilePath.putFile(pickedImgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("-NTT-", "upload user photo successfully");
                        // image uploaded successfully, now we can get image url
                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // uri contain image url

                                // create update profile request
                                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .setPhotoUri(uri)
                                        .build();

                                // update user profile
                                currentUser.updateProfile(profileUpdate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("-NTT-", "update user profile successfully");
                                                    // user profile update successfully
                                                    Toast.makeText(RegisterActivity.this, "Register Complete", Toast.LENGTH_SHORT).show();
                                                    updateDatabase();
                                                }
                                            }
                                        });

                            }
                        });

                    }
                });

    }

    // update user info to database and move to HomeDemoActivity
    private void updateDatabase() {
        // add user info (name, mail, uid,  to Users on database
        FirebaseUser currentUser = mAuth.getCurrentUser();
        User user = new User(currentUser.getDisplayName(),
                                currentUser.getEmail(),
                                currentUser.getUid(),
                                currentUser.getPhotoUrl().toString());
//        Log.d("-NTT-", user.getUserCover());

        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference()
                .child(User.KEY_USER_MODEL).child(currentUser.getUid());
        dataRef.setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d("-NTT-", "add user to database successfully");
                    // move to HomeDemoActivity
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void checkAndroidRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                Toast.makeText(RegisterActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                ActivityCompat.requestPermissions(RegisterActivity.this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        PHOTO_REQUEST_CODE);
//            }
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PHOTO_REQUEST_CODE);
        }
        else {
            openGallery();
        }
    }

    private void openGallery() {
        // TODO: open gallery intent and wait for user to pick and image
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/");
        startActivityForResult(galleryIntent, INTENT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // user has been successfully picked an image
            // get the uri of photo
            pickedImgUri = data.getData();
            imgUserPhoto.setImageURI(pickedImgUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PHOTO_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }
}
