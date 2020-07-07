package com.tanthin.communityblog.Utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class UtilFunction {

    public static final int PICK_PERMISSION_REQUEST_CODE = 1;

    public static void pickImage(Context mContext, FragmentActivity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PICK_PERMISSION_REQUEST_CODE);
        }
        else {
            openGallery(activity, requestCode);
        }
    }

    public static void openGallery(FragmentActivity activity, int requestCode) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/");
        activity.startActivityForResult(galleryIntent, requestCode);
    }
}
