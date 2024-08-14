package com.fde.keyassist;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fde.keyassist.fragment.ConfigManageFragment;
import com.fde.keyassist.fragment.UserProfileFragment;

import org.litepal.LitePal;

public class MainActivity extends Activity{

    private static final String TAG = "MainActivity";



    public void init(){
//        LitePal.initialize(this); // 初始化数据库
        SQLiteDatabase db = LitePal.getDatabase();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_main);
        init();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        Intent intent = new Intent(MainActivity.this,FloatingService.class);
        startService(intent);
        moveTaskToBack(true);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，继续操作
            } else {
                // 权限被拒绝，无法执行操作
            }
        }
    }



}
