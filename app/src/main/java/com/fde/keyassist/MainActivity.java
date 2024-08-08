package com.fde.keyassist;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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
        Intent intent = new Intent(MainActivity.this,FloatingService.class);
        startService(intent);
        moveTaskToBack(true);


    }



}
