package com.example.taller2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtil {

    public static void requestPermission(Activity context,String permission,String justification,int idCode){
            context.requestPermissions(new String[]{permission},idCode);
    }
}
