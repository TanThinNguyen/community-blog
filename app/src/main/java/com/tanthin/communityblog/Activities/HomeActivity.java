package com.tanthin.communityblog.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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
import com.tanthin.communityblog.Fragments.HomeFragment;
import com.tanthin.communityblog.Fragments.ProfileFragment;
import com.tanthin.communityblog.Fragments.SettingsFragment;
import com.tanthin.communityblog.Models.Post;
import com.tanthin.communityblog.Models.User;
import com.tanthin.communityblog.R;

import java.net.NoRouteToHostException;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static User currentUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        // get current user info from database
        getCurrentUserInfo();

        // setup toolbar (androidx.appcompat.widget)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup drawer toggle
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(HomeActivity.this,
                drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // set navigation view items click event
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set home fragment as default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
            getSupportActionBar().setTitle("Home");
        }

        // update navigation view header with current user's name, email, photo
        updateNavHeader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        // press Back to close navigation drawer when it is opening
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(HomeActivity.this, "Settings", Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId() == R.id.action_search){
            Toast.makeText(HomeActivity.this, "Search", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportActionBar().setTitle("Home");
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                        .replace(R.id.container, new HomeFragment())
                        .commit();
                break;
            case R.id.nav_profile:
                if (currentUserInfo == null) {
                    Log.d("-NTT-", "Home Activity currentUserIno == null");
                }
                getSupportActionBar().setTitle("Profile");
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                        .replace(R.id.container, ProfileFragment.newInstance(currentUserInfo))
                        .commit();
                break;
            case R.id.nav_favorite:
                getSupportActionBar().setTitle("Favorite");
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                        .replace(R.id.container, new ProfileFragment())
                        .commit();
            case R.id.nav_settings:
                getSupportActionBar().setTitle("Settings");
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                        .replace(R.id.container, new SettingsFragment())
                        .commit();
                break;
            case R.id.nav_signout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                break;
        }

        // close navigation view after click on its items
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        // if we return false, there's no item is selected
        return true;
    }

    // update user photo, name, mail on navigation view header
    public void updateNavHeader() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView navUserName = headerView.findViewById(R.id.nav_user_name);
        TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        navUserName.setText(currentUser.getDisplayName());
        navUserMail.setText(currentUser.getEmail());
        // use Glide to load user image
        Glide.with(HomeActivity.this).load(currentUser.getPhotoUrl()).into(navUserPhoto);
    }

    private static void getCurrentUserInfo() {
        Log.d("-NTT-", "HomeActivity getCurrentUserInfo");
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference(User.KEY_USER_MODEL);
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("-NTT-", "onDataChange");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Log.d("-NTT-", snapshot.getKey());
                    if (snapshot.getKey().equals(currentUser.getUid())) {
                        Log.d("-NTT-", "get current user object");
                        currentUserInfo = snapshot.getValue(User.class);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // get current user info from database that fragments can use
    public static User getCurrentUser() {
//        getCurrentUserInfo();
        return currentUserInfo;
    }
}
