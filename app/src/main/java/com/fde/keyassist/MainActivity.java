package com.fde.keyassist;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fde.keyassist.fragment.ConfigManageFragment;
import com.fde.keyassist.fragment.UserProfileFragment;

import org.litepal.LitePal;

import java.lang.reflect.Method;
import java.util.List;


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
        getWindow().getDecorView().postDelayed(this::finish, 0);
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



    private void changeAllAppsWindowSize() {



        try {
            // 获取 ActivityManager
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            // 使用反射调用 getRunningTasks
            Method getRunningTasksMethod = ActivityManager.class.getDeclaredMethod("getRunningTasks", int.class);
            getRunningTasksMethod.setAccessible(true);
            List<ActivityManager.RunningTaskInfo> tasks = (List<ActivityManager.RunningTaskInfo>) getRunningTasksMethod.invoke(activityManager, Integer.MAX_VALUE);

            // 获取屏幕的宽高
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            for (ActivityManager.RunningTaskInfo task : tasks) {
                ComponentName topActivity = task.topActivity;

                if (topActivity != null) {
                    // 创建 Intent 启动顶层 Activity
                    Intent intent = new Intent();
                    intent.setComponent(topActivity);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 启动新任务

                    // 设置新的窗口大小和位置
                    Rect newBounds = new Rect();
                    newBounds.left = (screenWidth - 200) / 2;
                    newBounds.top = (screenHeight - 300) / 2;
                    newBounds.right = newBounds.left + 200;
                    newBounds.bottom = newBounds.top + 300;

                    // 使用 ActivityOptions 设置窗口边界
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLaunchBounds(newBounds);

                    // 重新启动 Activity 并应用新的窗口设置
                    startActivity(intent, options.toBundle());
                }
            }
        } catch (Exception e) {

        }
    }

}
