package com.fde.keyassist;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fde.keyassist.adapter.PlaySpinnerAdapter;
import com.fde.keyassist.dialog.ApplyDialog;
import com.fde.keyassist.dialog.ModifyDialog;
import com.fde.keyassist.entity.AmplifyMappingEntity;
import com.fde.keyassist.entity.DirectMappingEntity;
import com.fde.keyassist.entity.DoubleClickMappingEntity;
import com.fde.keyassist.entity.KeyMappingEntity;
import com.fde.keyassist.entity.Plan;
import com.fde.keyassist.entity.ScaleMappingEntity;
import com.fde.keyassist.event.EventUtils;
import com.fde.keyassist.util.Constant;
import com.fde.keyassist.util.FileUtil;

import org.litepal.LitePal;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class FloatingService extends Service implements View.OnClickListener,AdapterView.OnItemSelectedListener{

    private static final String TAG = "FlatingService";
    private boolean isMainWindow = false; // 是否显示了主界面
    private View mainView;
    private WindowManager.LayoutParams mainParams;
    private WindowManager mainWindow;
    private boolean isChange = false; // 是否正在修改
    private ModifyDialog modifyDialog;

    private Button key_mapping_save;
    private Button key_mapping_cancel;
    private ImageView key_mapping_tap_click;

    private ApplyDialog applyDialog;
    private Button key_mapping_apply;
    private Boolean isApply = false;

    private Boolean editAndCancal = true;

    private List<KeyMappingEntity> keyMappingEntities;

    private Integer eventType;

    private List<DirectMappingEntity> directMappingEntities;

    private List<DoubleClickMappingEntity> doubleClickMappingEntities;

    private List<ScaleMappingEntity> scaleMappingEntities;

    private List<AmplifyMappingEntity> amplifyMappingEntities;

    private View floatView;

    private WindowManager.LayoutParams floatParams;

    private WindowManager floatWindow;

    private Boolean saveAndExit = false; // 保存和退出

    private ImageView key_mapping_direct_click;


    private LinearLayout key_mapping_plan_linear;

    private TextView key_mapping_plan_text;

    public static PopupWindow popupWindow;

    private ImageView dropdown_menu_add;

    private ImageView key_mapping_spinner_down;

    private List<Plan> plans;

    private ImageView key_mapping_double_click;

    private Integer curCount = 1;

    private ImageView key_mapping_scale;

    private ImageView dropdown_menu_export;

    private ImageView key_mapping_open_cursor;

    private Boolean cursorMake = true;

    private ImageView dropdown_menu_import;

    private ImageView key_mapping_amplify;

    private Button key_mapping_exit;





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase db = LitePal.getDatabase();

        showFloatView();
        applyDialog = new ApplyDialog(Constant.planName,this);
        // 导入预设按键
        FileUtil.league(this);
//        textView.setText(holder.key_mapping_plan_text.getText());
//        Constant.planName = holder.key_mapping_plan_text.getText().toString();
//        FloatingService.closePopupWindow();

    }




    @SuppressLint("ClickableViewAccessibility")
    private void showFloatView() {
        floatView = LayoutInflater.from(this).inflate(R.layout.background_window,null,false);
        floatParams = createLayoutParams();
        floatWindow = createWindow(50, 50, floatView, floatParams);
        dragView(floatView,floatWindow,floatParams,"showKeyMapping");
        onkey();

    }

    public void startListenerKey(){
        floatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SCALED
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        floatWindow.updateViewLayout(floatView,floatParams);
    }
    public void endListenerKey(){
        floatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SCALED
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        floatWindow.updateViewLayout(floatView,floatParams);
    }



    public void onkey(){
        floatView.setFocusableInTouchMode(true);
        // 鼠标监听事件

        // 按键事件
        floatView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int[] pos = getPosition(i,String.valueOf(keyEvent.getDisplayLabel()));
                if(pos[0] != -1 && pos[1]!=-1) {
                    if (eventType == Constant.TAP_CLICK_EVENT) {
                        EventUtils.tapClick(pos[0], pos[1]);
                    } else if (eventType == Constant.DIRECTION_KEY_UP
                            || eventType == Constant.DIRECTION_KEY_LEFT
                            || eventType == Constant.DIRECTION_KEY_DOWN
                            || eventType == Constant.DIRECTION_KEY_RIGHT) {
                        EventUtils.diretClick(floatView,keyEvent, pos[0], pos[1], eventType);
                    }else if (eventType == Constant.DOUBLE_CLICK_EVENT){
//                        EventUtils.doubleClick(pos[0], pos[1],curCount);
                        if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                            EventUtils.ClickController.getInstance()
                                    .setClickPosition(pos[0], pos[1]) // 设置点击位置 (x, y)
                                    .startClick(); // 开始持续点击
                        }
                        if(keyEvent.getAction() == KeyEvent.ACTION_UP){
                            EventUtils.ClickController.getInstance().stopClick();
                        }

                    }else if(eventType == Constant.SCALE){
                        if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                            Log.d(TAG, "onKey: down:" + keyEvent);
                            EventUtils.ZoomController.getInstance().setCenter(new EventUtils.Pointer(pos[0], pos[1])).
                                    startZoom(keyEvent.getRepeatCount(), true);
                            return true;
                        }
                        if(keyEvent.getAction() == KeyEvent.ACTION_UP){
                            Log.d(TAG, "onKey: up:" + keyEvent);
                            EventUtils.ZoomController.getInstance().stopZoom();
                            return true;
                        }
                    }else if(eventType == Constant.AMPLIFY){
                        if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                            Log.d(TAG, "onKey: down:" + keyEvent);
                            EventUtils.ZoomController.getInstance().setCenter(new EventUtils.Pointer(pos[0], pos[1])).
                                    startZoom(keyEvent.getRepeatCount(), false);
                            return true;
                        }
                        if(keyEvent.getAction() == KeyEvent.ACTION_UP){
                            Log.d(TAG, "onKey: up:" + keyEvent);
                            EventUtils.ZoomController.getInstance().stopZoom();
                            return true;
                        }
                    }
                }
                return true;
            }
        });
    }


    // 显示按键映射
    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    public void showKeyMapping(){
        if(isMainWindow){
            return;
        }
        mainView = LayoutInflater.from(this).inflate(R.layout.key_mapping,null,false);
        mainParams = createLayoutParams();
        mainWindow = createWindow(300, 450, mainView, mainParams);
        dragView(mainView,mainWindow,mainParams,"");
//        Button key_mapping_click = mainView.findViewById(R.id.key_mapping_tap_click);
//        key_mapping_save = mainView.findViewById(R.id.key_mapping_save);
        key_mapping_save = mainView.findViewById(R.id.key_mapping_save);
        key_mapping_save.setOnClickListener(this);
        key_mapping_cancel = mainView.findViewById(R.id.key_mapping_cancel);
        key_mapping_cancel.setOnClickListener(this);
        key_mapping_tap_click = mainView.findViewById(R.id.key_mapping_tap_click);
        key_mapping_tap_click.setOnClickListener(this);
        key_mapping_apply = mainView.findViewById(R.id.key_mapping_apply);
        key_mapping_apply.setOnClickListener(this);
        key_mapping_direct_click = mainView.findViewById(R.id.key_mapping_direct_click);
        key_mapping_direct_click.setOnClickListener(this);
        key_mapping_plan_linear = mainView.findViewById(R.id.key_mapping_plan_linear);
        key_mapping_plan_linear.setOnClickListener(this);
        key_mapping_plan_text = mainView.findViewById(R.id.key_mapping_plan_text);
        key_mapping_plan_text.setText(Constant.planName);
        key_mapping_plan_text.setText("王者荣耀");
        Constant.planName = key_mapping_plan_text.getText().toString();
        key_mapping_spinner_down = mainView.findViewById(R.id.key_mapping_spinner_down);

        key_mapping_double_click = mainView.findViewById(R.id.key_mapping_double_click);
        key_mapping_double_click.setOnClickListener(this);

        key_mapping_open_cursor = mainView.findViewById(R.id.key_mapping_open_cursor);
        key_mapping_open_cursor.setOnClickListener(this);

        key_mapping_scale = mainView.findViewById(R.id.key_mapping_scale);
        key_mapping_scale.setOnClickListener(this);

        key_mapping_amplify = mainView.findViewById(R.id.key_mapping_amplify);
        key_mapping_amplify.setOnClickListener(this);

        key_mapping_exit = mainView.findViewById(R.id.key_mapping_exit);
        key_mapping_exit.setOnClickListener(this);

        isMainWindow = true;



        if(isApply){
            key_mapping_apply.setText("取消");
        }
    }





    // 创建窗口参数
    public WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SCALED
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;


        if (Build.VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.format = PixelFormat.TRANSLUCENT;
        return params;
    }

    // 创建窗口
    public WindowManager createWindow(int width, int height, View view, WindowManager.LayoutParams params){
//        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        params.width = width;
        params.height = height;
        params.x = displayMetrics.widthPixels;
        params.y = displayMetrics.heightPixels;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(view,params);
        return windowManager;
    }

    public void dragView(View view,WindowManager windowManager,WindowManager.LayoutParams params,String methodName){
        view.setClickable(true);
        // 拖拽事件
        view.setOnTouchListener(new View.OnTouchListener() {
            private int x;
            private int y;
            //是否在移动
            private boolean isMoving;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        isMoving = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int moveX = nowX - x;
                        int moveY = nowY - y;
                        if (Math.abs(moveX) > 0 || Math.abs(moveY) > 0) {
                            isMoving = true;
                            params.x += moveX;
                            params.y += moveY;
                            //更新View的位置
                            windowManager.updateViewLayout(view, params);
                            x = nowX;
                            y = nowY;
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            if(methodName.equals("showKeyMapping")){
                                showKeyMapping();
                            }
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

    }

    public int[] getPosition(int keycode,String keyValue){
        int x = -1;
        int y = -1;
        if(keyMappingEntities != null && !keyMappingEntities.isEmpty()){
            Iterator iterator = keyMappingEntities.iterator();
            while(iterator.hasNext()){
                KeyMappingEntity keyMapping = (KeyMappingEntity) iterator.next();
                if(keyMapping.getKeycode() != null && keyMapping.getKeyValue().equals(keyValue)){
                    x = keyMapping.getX();
                    y = keyMapping.getY();
                    eventType = Constant.TAP_CLICK_EVENT;
                    return new int[]{x,y};
                }
            }

        }

        if(doubleClickMappingEntities != null && !doubleClickMappingEntities.isEmpty()){
            Iterator iterator = doubleClickMappingEntities.iterator();
            while(iterator.hasNext()){
                DoubleClickMappingEntity keyMapping = (DoubleClickMappingEntity) iterator.next();
                if(keyMapping.getKeycode() != null && keyMapping.getKeyValue().equals(keyValue)){
                    x = keyMapping.getX();
                    y = keyMapping.getY();
                    eventType = Constant.DOUBLE_CLICK_EVENT;
                    curCount = keyMapping.getCount();
                    return new int[]{x,y};
                }
            }

        }

        if(directMappingEntities != null && !directMappingEntities.isEmpty()){
            Iterator iterator = directMappingEntities.iterator();
            while(iterator.hasNext()){
                DirectMappingEntity directMapping = (DirectMappingEntity) iterator.next();
                if(directMapping.getUpKeycode() != null && directMapping.getUpKeyValue().equals(keyValue)){
                    x = directMapping.getX();
                    y = directMapping.getY();
                    eventType = Constant.DIRECTION_KEY_UP;
                    return new int[]{x,y};
                }else if(directMapping.getDownKeycode() != null &&directMapping.getDownKeyValue().equals(keyValue)){
                    x = directMapping.getX();
                    y = directMapping.getY();
                    eventType = Constant.DIRECTION_KEY_DOWN;
                    return new int[]{x,y};
                }else if(directMapping.getLeftKeycode() != null && directMapping.getLeftKeyValue().equals(keyValue)){
                    x = directMapping.getX();
                    y = directMapping.getY();
                    eventType = Constant.DIRECTION_KEY_LEFT;
                    return new int[]{x,y};
                }else if(directMapping.getRightKeycode() != null && directMapping.getRightKeyValue().equals(keyValue)){
                    x = directMapping.getX();
                    y = directMapping.getY();
                    eventType = Constant.DIRECTION_KEY_RIGHT;
                    return new int[]{x,y};
                }
            }

        }

        if(scaleMappingEntities != null && !scaleMappingEntities.isEmpty()){
            Iterator iterator = scaleMappingEntities.iterator();
            while(iterator.hasNext()){
                ScaleMappingEntity keyMapping = (ScaleMappingEntity) iterator.next();
                if(keyMapping.getKeycode() != null && keyMapping.getKeyValue().equals(keyValue)){
                    x = keyMapping.getX();
                    y = keyMapping.getY();
                    eventType = Constant.SCALE;
                    return new int[]{x,y};
                }
            }
        }

        if(amplifyMappingEntities != null && !amplifyMappingEntities.isEmpty()){
            Iterator iterator = amplifyMappingEntities.iterator();
            while(iterator.hasNext()){
                AmplifyMappingEntity keyMapping = (AmplifyMappingEntity) iterator.next();
                if(keyMapping.getKeycode() != null && keyMapping.getKeyValue().equals(keyValue)){
                    x = keyMapping.getX();
                    y = keyMapping.getY();
                    eventType = Constant.AMPLIFY;
                    return new int[]{x,y};
                }
            }
        }

        return new int[]{-1,-1};
    }


    public void startModify(String planName){
        if(!isChange){
            modifyDialog = new ModifyDialog(getApplication(),planName);
            modifyDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                }
            });
            modifyDialog.show();
            mainWindow.removeView(mainView);
            mainWindow.addView(mainView,mainParams);
            isChange = true;
        }
    }

    public static void closePopupWindow(){
        if(popupWindow !=null){
            popupWindow.dismiss();
        }
    }

    @SuppressLint({"NonConstantResourceId", "MissingInflatedId"})
    @Override
    public void onClick(View view) {
         switch (view.getId()){
             case R.id.key_mapping_exit:
                 endListenerKey();
                 applyDialog.cancal();
                 isApply = false;
                 key_mapping_apply.setText("应用");
                 closeCursor();
                 mainWindow.removeView(mainView);
                 floatWindow.removeView(floatView);

                 stopSelf();
                 break;

             case R.id.key_mapping_open_cursor:
//                 openCursor();
                 if(!editAndCancal){
                     if(cursorMake){
                         modifyDialog.setCursorSwitch(true);
                         cursorMake = !cursorMake;
                         setCursorBack(true);
                     }else{
                         modifyDialog.setCursorSwitch(false);
                         cursorMake = !cursorMake;
                         setCursorBack(false);
                     }
                 }
                 break;



             case R.id.key_mapping_plan_linear:
                 View dropdownView = LayoutInflater.from(this).inflate(R.layout.dropdown_menu, null);
                 dropdown_menu_add = dropdownView.findViewById(R.id.dropdown_menu_add);
                 dropdown_menu_export = dropdownView.findViewById(R.id.dropdown_menu_export);
                 dropdown_menu_import = dropdownView.findViewById(R.id.dropdown_menu_import);
                 RecyclerView recyclerView = dropdownView.findViewById(R.id.key_mapping_recyclerview);

                 plans = LitePal.findAll(Plan.class);

                 PlaySpinnerAdapter adapter = new PlaySpinnerAdapter(plans,key_mapping_plan_text);
                 recyclerView.setAdapter(adapter);

                 dropdown_menu_export.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         List<String> list = new ArrayList<>();
                         List<Plan> planList = LitePal.findAll(Plan.class);
                         for(Plan plan : planList){
                             list.add(plan.getPlanName());
                         }
                         FileUtil.exportData(list,getApplication());
                         Toast.makeText(getApplication(),"成功导出文件到根目录keyAssist目录下",Toast.LENGTH_SHORT).show();
                     }
                 });

                 dropdown_menu_import.setOnClickListener(new View.OnClickListener(){

                     @SuppressLint("NotifyDataSetChanged")
                     @Override
                     public void onClick(View view) {
                         FileUtil.importData();
                         plans = LitePal.findAll(Plan.class);
                         adapter.notifyDataSetChanged();
                         PlaySpinnerAdapter adapter = new PlaySpinnerAdapter(plans,key_mapping_plan_text);
                         recyclerView.setAdapter(adapter);
                     }
                 });

                 dropdown_menu_add.setOnClickListener(new View.OnClickListener() {
                     @Override
                     @SuppressLint("NotifyDataSetChanged")
                     public void onClick(View view) {
                         Plan plan = new Plan();
                         plan.setPlanName("方案");
                         plan.save();
                         plans = LitePal.findAll(Plan.class);
                         adapter.notifyDataSetChanged();
                         PlaySpinnerAdapter adapter = new PlaySpinnerAdapter(plans,key_mapping_plan_text);
                         recyclerView.setAdapter(adapter);
                     }
                 });

                 recyclerView.setLayoutManager(new LinearLayoutManager(this));
                 popupWindow = new PopupWindow(dropdownView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                 popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                 popupWindow.setOutsideTouchable(true); // Dismiss the popup when touching outside
                 popupWindow.showAsDropDown(key_mapping_plan_linear,0,0,Gravity.NO_GRAVITY);

                 break;
             case R.id.key_mapping_tap_click:
                 if(!editAndCancal){
//                     startModify("方案一");
                     modifyDialog.setEventType(Constant.TAP_CLICK_EVENT); //单击事件
//                     key_mapping_tap_click.setBackground(R.drawable.key_mapping_key_background_click);
//                     key_mapping_tap_click.setBackgroundResource(R.drawable.key_mapping_key_background_click);
                     setButtonBack(key_mapping_tap_click);
                 }
                 break;
             case R.id.key_mapping_direct_click:
                 if(!editAndCancal){
//                     startModify("方案一");
                     modifyDialog.setEventType(Constant.DIRECTION_KEY); //单击事件
                     setButtonBack(key_mapping_direct_click);
                 }
                 break;
             case R.id.key_mapping_double_click:
                 if(!editAndCancal){
                     modifyDialog.setEventType(Constant.DOUBLE_CLICK_EVENT); //单击事件
                     setButtonBack(key_mapping_double_click);
                 }
                 break;
             case R.id.key_mapping_amplify:
                 if(!editAndCancal){
                     modifyDialog.setEventType(Constant.AMPLIFY);
                     setButtonBack(key_mapping_amplify);
                 }
                 break;
             case R.id.key_mapping_scale:
                 if(!editAndCancal){
                     modifyDialog.setEventType(Constant.SCALE);
                     setButtonBack(key_mapping_scale);
                 }
                 break;
             case R.id.key_mapping_apply:
                 if(!isApply && editAndCancal){
                     startListenerKey();
                     if(applyDialog != null){
                         applyDialog.cancal();
                     }
                     applyDialog = new ApplyDialog(Constant.planName,this);
                     keyMappingEntities = applyDialog.applyTapClick();
                     directMappingEntities = applyDialog.applyDirect();
                     doubleClickMappingEntities = applyDialog.applyDoubleClick();
                     scaleMappingEntities = applyDialog.applyScaleClick();
                     amplifyMappingEntities = applyDialog.applyAmplifyClick();
                     Boolean b = applyDialog.applyCursor();
                     if(b){
                         openCursor();
                     }else{
                         closeCursor();
                     }
                     isApply = true;
                     isMainWindow = false;
                     key_mapping_apply.setText("取消");
                     mainWindow.removeView(mainView);
                 }else{
                     if(applyDialog!=null) {
                         endListenerKey();
                         applyDialog.cancal();
                         isApply = false;
                         key_mapping_apply.setText("应用");
                         closeCursor();
                     }
                 }
                 break;
             case R.id.key_mapping_save:
                 setButtonBack(null);
                 setCursorBack(false);
                 if(!editAndCancal) {
                     modifyDialog.save();
                     key_mapping_cancel.setText("编辑");
                     isChange = false;
                     editAndCancal = true;
                     key_mapping_save.setText("隐藏");
                 }else{
                     endListenerKey();
                     applyDialog.cancal();
                     isApply = false;
                     key_mapping_apply.setText("应用");
                     isMainWindow = false;
                     mainWindow.removeView(mainView);
                 }
                 break;
             case R.id.key_mapping_cancel:
                 setButtonBack(null);
                 setCursorBack(false);
                 // 编辑
                     if(editAndCancal){
                         key_mapping_save.setText("保存");
                         applyDialog.cancal();
                         isApply = false;
                         editAndCancal = false;
                         key_mapping_apply.setText("应用");
                         endListenerKey();
                         key_mapping_cancel.setText("取消");
                         startModify(Constant.planName);
                         modifyDialog.showView(); //单击事件
                         setCursorBack(modifyDialog.getCursorSwitch());
                     }else{
                         key_mapping_save.setText("隐藏");
                         editAndCancal = true;
                         key_mapping_cancel.setText("编辑");
                         modifyDialog.cancel();
                         isChange = false;
                     }
                     break;
                 }

         }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selectedItem = adapterView.getItemAtPosition(i).toString();
        Constant.planName = selectedItem;

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void setButtonBack(ImageView imageView){
        key_mapping_tap_click.setBackgroundResource(R.drawable.key_mapping_key_background);
        key_mapping_direct_click.setBackgroundResource(R.drawable.key_mapping_key_background);
        key_mapping_double_click.setBackgroundResource(R.drawable.key_mapping_key_background);
        key_mapping_scale.setBackgroundResource(R.drawable.key_mapping_key_background);
        key_mapping_amplify.setBackgroundResource(R.drawable.key_mapping_key_background);
        if(imageView != null){
            imageView.setBackgroundResource(R.drawable.key_mapping_key_background_click);
        }
    }



    public void setCursorBack(Boolean b){
        if(b){
            key_mapping_open_cursor.setBackgroundResource(R.drawable.key_mapping_key_background_click);
        }else{
            key_mapping_open_cursor.setBackgroundResource(R.drawable.key_mapping_key_background);
        }
    }




    //打开鼠标
    public void openCursor(){
        try {

            Process process = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            os.writeBytes("su\n");
            os.writeBytes("/system/bin/setprop fde.show_wayland_cursor true\n");
            os.writeBytes("/system/bin/setprop fde.click_as_touch true\n");
            os.writeBytes("/system/bin/setprop fde.inject_as_touch true\n");
            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
//            Process process = Runtime.getRuntime().exec("/system/bin/sh");
//            DataOutputStream os = new DataOutputStream(process.getOutputStream());
//            os.writeBytes("setprop fde.show_wayland_cursor true\n");
//            os.writeBytes("setprop fde.click_as_touch true\n");
//            os.writeBytes("setprop fde.inject_as_touch true\n");
//            os.writeBytes("exit\n");
//            os.flush();
//            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    // 关闭鼠标
    public void closeCursor(){
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("setprop fde.show_wayland_cursor true\n");
            os.writeBytes("setprop fde.click_as_touch false\n");
            os.writeBytes("setprop fde.inject_as_touch false\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }




}