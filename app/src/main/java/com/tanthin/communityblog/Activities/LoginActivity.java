package com.tanthin.communityblog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tanthin.communityblog.R;

public class LoginActivity extends AppCompatActivity {

    // key for FirebaseStorage
    public static final String KEY_BLOG_IMAGES = "blog_images";
    public static final String KEY_USERS_PHOTO = "users_photo";
    public static final String KEY_USERS_COVER = "users_cover";
    public static final String KEY_USER_COVER_DEFAULT = "users_cover/default/wallpaper.jpg";

    // link for Glide to load user's default cover
    private static String defaultCoverUri;

    private ImageView loginPhoto;
    private EditText userMail, userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init view
        loginPhoto = findViewById(R.id.login_photo);
        userMail = findViewById(R.id.login_mail);
        userPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.btn_login);
        loginProgress = findViewById(R.id.login_progress);

        loginProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        // defaul cover for all new user
        getDefaultCoverUriFromDbRef();

        // Login event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgress.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                String mail = userMail.getText().toString();
                String password = userPassword.getText().toString();

                if (mail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please Verify All Fields", Toast.LENGTH_SHORT).show();
                    loginProgress.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
                else {
                    signIn(mail, password);
                }
            }
        });

        // click image view to register
        loginPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerActivity);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // user is already connected, so we need to redirect user to home page
            updateUI();
        }
    }

    // defaul cover for all new user
    private void getDefaultCoverUriFromDbRef() {
//        Uri defaultUserCover = Uri.parse("android.resource://"+ R.class.getPackage().getName()+"/"+R.drawable.wallpaper);
//        Log.d("-NTT-", defaultUserCover.toString());
//        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("users_cover/default/demo");
//        ref.putFile(defaultUserCover)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Log.d("-NTT-", "upload default cover successful");
//                        ref.getDownloadUrl()
//                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                    @Override
//                                    public void onSuccess(Uri uri) {
//                                        defaultCoverUri = uri.toString();
//                                        Log.d("-NTT-", defaultCoverUri);
//                                    }
//                                });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("-NTT-", e.toString());
//                    }
//                });
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(KEY_USER_COVER_DEFAULT);
        storageRef.getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            defaultCoverUri = task.getResult().toString();
                            Log.d("-NTT-", "get default cover url successfully " + defaultCoverUri);
                        }
                    }
                });
    }

    public static String getDefaultCoverUri() {
        return defaultCoverUri;
    }

    private void signIn(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // login successfully
                            loginProgress.setVisibility(View.VISIBLE);
                            btnLogin.setVisibility(View.VISIBLE);

                            Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                        else {
                            // login fail
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loginProgress.setVisibility(View.INVISIBLE);
                            btnLogin.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    // update UI when login successfully
    private void updateUI() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }
}
