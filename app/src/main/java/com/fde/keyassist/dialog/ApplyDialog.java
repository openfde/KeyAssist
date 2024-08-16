package com.fde.keyassist.dialog;

import static android.content.Context.WINDOW_SERVICE;

import static org.litepal.LitePalApplication.getContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fde.keyassist.R;
import com.fde.keyassist.entity.AmplifyMappingEntity;
import com.fde.keyassist.entity.CursorEntity;
import com.fde.keyassist.entity.DirectMappingEntity;
import com.fde.keyassist.entity.DoubleClickMappingEntity;
import com.fde.keyassist.entity.KeyMappingEntity;
import com.fde.keyassist.entity.Plan;
import com.fde.keyassist.entity.ScaleMappingEntity;
import com.fde.keyassist.util.Constant;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;


public class ApplyDialog {

    private String planName;
    private WindowManager windowManager;
    private android.view.WindowManager.LayoutParams params;
    private Context context;
    private List<View> allView = new ArrayList<>();

    public ApplyDialog(String planName,Context context){
        this.planName = planName;
        this.context = context;
        init();
    }

    public void init(){
        windowManager = createWindow();
        params = createLayoutParams();
    }

    // 创建窗口
    public android.view.WindowManager createWindow(){
        android.view.WindowManager windowManager = (android.view.WindowManager) context.getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return windowManager;
    }

    // 创建窗口参数
    public WindowManager.LayoutParams createLayoutParams(){
        android.view.WindowManager.LayoutParams params = new android.view.WindowManager.LayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | android.view.WindowManager.LayoutParams.FLAG_SCALED
                | android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (Build.VERSION.SDK_INT >= 26) {
            params.type = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.format = PixelFormat.TRANSLUCENT;
        params.type = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.gravity = Gravity.TOP | Gravity.START;
        params.width = 70;
        params.height = 70;
        return params;
    }

    @SuppressLint("MissingInflatedId")
    public List<DirectMappingEntity> applyDirect(){
        params.width = 80;
        params.height = 100;
        List<DirectMappingEntity> directMappingEntities = new ArrayList<>();
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            directMappingEntities = LitePal.where("planId = ?", plan.getId().toString()).find(DirectMappingEntity.class);
        }

        for (DirectMappingEntity entity : directMappingEntities){
            View view = LayoutInflater.from(context).inflate(R.layout.modify_dialog_direct_click, null, false);
            TextView up = view.findViewById(R.id.modify_dialog_direct_click_up);
            up.setText(entity.getUpKeyValue());
            TextView down = view.findViewById(R.id.modify_dialog_direct_click_down);
            down.setText(entity.getDownKeyValue());
            TextView left = view.findViewById(R.id.modify_dialog_direct_click_left);
            left.setText(entity.getLeftKeyValue());
            TextView right = view.findViewById(R.id.modify_dialog_direct_click_right);
            right.setText(entity.getRightKeyValue());
            ImageView delete = view.findViewById(R.id.modify_dialog_tap_click_delete);
            delete.setVisibility(View.GONE);
            TextView hint = view.findViewById(R.id.modify_dialog_direct_click_hint);
            hint.setVisibility(View.GONE);
            params.x = entity.getX() - params.width/2;
            params.y = entity.getY() - params.height/2;
            windowManager.addView(view,params);
            allView.add(view);
        }
        return directMappingEntities;
    }


    public List<KeyMappingEntity> applyTapClick(){
        cancal();
        List<KeyMappingEntity> curKeyMappingEntity = new ArrayList<>();
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            curKeyMappingEntity = LitePal.where("planId = ?", plan.getId().toString()).find(KeyMappingEntity.class);
        }
        for (KeyMappingEntity entity : curKeyMappingEntity){
            if(entity.getEventType() == Constant.TAP_CLICK_EVENT) {
                View view = LayoutInflater.from(context).inflate(R.layout.apply_dialog_tap_click, null, false);
                params.x = entity.getX() - params.width/2;
                params.y = entity.getY() - params.height/2;
                if(entity.getKeyValue() != null && !entity.getKeyValue().isEmpty()){
                    TextView apply_dialog_tap_click_edit = view.findViewById(R.id.apply_dialog_tap_click_edit);
                    apply_dialog_tap_click_edit.setText(entity.getKeyValue());
                }
                windowManager.addView(view,params);
                allView.add(view);
            }

        }
        return curKeyMappingEntity;
    }

//    @SuppressLint("MissingInflatedId")
//    public List<KeyMappingEntity> apply(){
//        applyTapClick();
//        applyDirect();
//    }

    @SuppressLint("MissingInflatedId")
    public List<ScaleMappingEntity> applyScaleClick(){
        List<ScaleMappingEntity> curKeyMappingEntity = new ArrayList<>();
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            curKeyMappingEntity = LitePal.where("planId = ?", plan.getId().toString()).find(ScaleMappingEntity.class);
        }
        for (ScaleMappingEntity entity : curKeyMappingEntity){
            if(entity.getEventType() == Constant.SCALE) {
                View view = LayoutInflater.from(context).inflate(R.layout.modify_dialog_scale, null, false);
                ImageView imageView = view.findViewById(R.id.modify_dialog_scale_delete);
                imageView.setVisibility(View.GONE);
                TextView textView = view.findViewById(R.id.modify_dialog_scale_hint);
                textView.setVisibility(View.GONE);

                params.x = entity.getX() - params.width/2;
                params.y = entity.getY() - params.height/2;
                if(entity.getKeyValue() != null && !entity.getKeyValue().isEmpty()){
                    TextView apply_dialog_tap_click_edit = view.findViewById(R.id.modify_dialog_scale_edit);
                    apply_dialog_tap_click_edit.setText(entity.getKeyValue());
                }
                windowManager.addView(view,params);
                allView.add(view);
            }

        }
        return curKeyMappingEntity;
    }


    @SuppressLint("MissingInflatedId")
    public List<AmplifyMappingEntity> applyAmplifyClick(){
        List<AmplifyMappingEntity> curKeyMappingEntity = new ArrayList<>();
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            curKeyMappingEntity = LitePal.where("planId = ?", plan.getId().toString()).find(AmplifyMappingEntity.class);
        }
        for (AmplifyMappingEntity entity : curKeyMappingEntity){
            if(entity.getEventType() == Constant.AMPLIFY) {
                View view = LayoutInflater.from(context).inflate(R.layout.modify_dialog_scale, null, false);
                ImageView imageView = view.findViewById(R.id.modify_dialog_scale_delete);
                imageView.setVisibility(View.GONE);
                TextView textView = view.findViewById(R.id.modify_dialog_scale_hint);
                textView.setVisibility(View.GONE);

                params.x = entity.getX() - params.width/2;
                params.y = entity.getY() - params.height/2;
                if(entity.getKeyValue() != null && !entity.getKeyValue().isEmpty()){
                    TextView apply_dialog_tap_click_edit = view.findViewById(R.id.modify_dialog_scale_edit);
                    apply_dialog_tap_click_edit.setText(entity.getKeyValue());
                }
                windowManager.addView(view,params);
                allView.add(view);
            }

        }
        return curKeyMappingEntity;
    }

    public List<DoubleClickMappingEntity> applyDoubleClick(){
        List<DoubleClickMappingEntity> curKeyMappingEntity = new ArrayList<>();
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            curKeyMappingEntity = LitePal.where("planId = ?", plan.getId().toString()).find(DoubleClickMappingEntity.class);
        }
        for (DoubleClickMappingEntity entity : curKeyMappingEntity){
            if(entity.getEventType() == Constant.DOUBLE_CLICK_EVENT) {
                View view = LayoutInflater.from(context).inflate(R.layout.modify_dialog_double_click, null, false);
                ImageView modify_dialog_double_click_delete = view.findViewById(R.id.modify_dialog_double_click_delete);
                modify_dialog_double_click_delete.setVisibility(View.GONE);
                TextView modify_dialog_double_click_count = view.findViewById(R.id.modify_dialog_double_click_count);
                modify_dialog_double_click_count.setVisibility(View.GONE);
                Button modify_dialog_double_click_up = view.findViewById(R.id.modify_dialog_double_click_up);
                modify_dialog_double_click_up.setVisibility(View.GONE);
                Button modify_dialog_double_click_down = view.findViewById(R.id.modify_dialog_double_click_down);
                modify_dialog_double_click_down.setVisibility(View.GONE);
                TextView modify_dialog_tap_click_hint = view.findViewById(R.id.modify_dialog_tap_click_hint);
                modify_dialog_tap_click_hint.setVisibility(View.GONE);
                params.x = entity.getX() - params.width/2;
                params.y = entity.getY() - params.height/2;
                if(entity.getKeyValue() != null && !entity.getKeyValue().isEmpty()){
                    TextView apply_dialog_tap_click_edit = view.findViewById(R.id.modify_dialog_double_click_edit);
                    apply_dialog_tap_click_edit.setText(entity.getKeyValue());
                }
                windowManager.addView(view,params);
                allView.add(view);
            }

        }
        return curKeyMappingEntity;
    }

    public Boolean applyCursor(){
        List<CursorEntity> cursorEntities = new ArrayList<>();
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            cursorEntities = LitePal.where("planId = ?", plan.getId().toString()).find(CursorEntity.class);
        }
        if(cursorEntities != null && !cursorEntities.isEmpty()){
            return cursorEntities.get(0).getCursorSwitch();
        }
        return false;
    }


    public void cancal(){
        if(windowManager!=null && allView!=null && !allView.isEmpty()){
            for(View view: allView){
                windowManager.removeView(view);
            }
            allView.clear();
        }



    }

}
