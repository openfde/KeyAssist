package com.fde.keyassist.dialog;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fde.keyassist.R;
import com.fde.keyassist.entity.DirectMappingEntity;
import com.fde.keyassist.entity.KeyMappingEntity;
import com.fde.keyassist.entity.Plan;
import com.fde.keyassist.util.Constant;


import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class ModifyDialog extends BaseServiceDialog implements View.OnClickListener {

    // 按键
    private List<KeyMappingEntity> keyMappingEntities = new ArrayList<>();


    private Integer eventType = Constant.TAP_CLICK_EVENT;

    private TextView curText;
//    private TextView curHintText;

    private View curView;

    private String planName;


    private List<View> tapView = new ArrayList<>();
    private List<View> allView = new ArrayList<>();
    private List<View> directView = new ArrayList<>();

    private WindowManager windowManager;

    private WindowManager.LayoutParams layoutParams;


    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public ModifyDialog(@NonNull Context context,String planName) {
        super(context);
        this.planName = planName;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_modify;
    }

    @Override
    protected int getWidth() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected int getHeight() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void onInited() {
        windowManager = createWindow();
        layoutParams = createLayoutParams();
    }
    
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility","ResourceType"})
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if(eventType == Constant.TAP_CLICK_EVENT){
                createTapClick(event);
            }else if(eventType == Constant.DIRECTION_KEY){
                createDirectClick(event);
            }

        }
        return super.onTouchEvent(event);
    }


    public void createTapClick(MotionEvent event){
        // 点击事件
        View view = LayoutInflater.from(getContext()).inflate(R.layout.modify_dialog_tap_click,null,false);
//                curText = view.findViewById(R.id.modify_dialog_tap_click_edit);
//                curHintText = view.findViewById(R.id.modify_dialog_tap_click_hint);
        layoutParams.width = 70;
        layoutParams.height = 70;
        layoutParams.x = (int)event.getRawX() - layoutParams.width/2;
        layoutParams.y = (int)event.getRawY() - layoutParams.height/2;
        windowManager.addView(view,layoutParams);
        curText = view.findViewById(R.id.modify_dialog_tap_click_edit);
        curView = view;
        tapView.add(view);
        allView.add(view);
        dragView(view,"");
    }

    public void createDirectClick(MotionEvent event){
        // 方向键
        View view = LayoutInflater.from(getContext()).inflate(R.layout.modify_dialog_direct_click,null,false);
        layoutParams.width = 80;
        layoutParams.height = 100;
        layoutParams.x = (int)event.getRawX() - layoutParams.width/2;
        layoutParams.y = (int)event.getRawY() - layoutParams.height/2;
        windowManager.addView(view,layoutParams);
        curText = view.findViewById(R.id.modify_dialog_direct_click_up);
        curView = view;
        directView.add(view);
        allView.add(view);
        dragView(view,"");
    }


    @SuppressLint("SetTextI18n")
    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        int metaState = event.getMetaState();
        boolean ctrlPressed = (metaState & KeyEvent.META_CTRL_ON) != 0;
        boolean altPressed = (metaState & KeyEvent.META_ALT_ON) != 0;
//        curText = curView.findViewById(R.id.modify_dialog_tap_click_edit);
        if(curText != null){
            if(event.getDisplayLabel() != 0 && ctrlPressed){
                curText.setText("CTRL+" + event.getDisplayLabel() );
                curText.setTextSize(8);
            }
            else if(event.getDisplayLabel() != 0 && altPressed){
                curText.setText("ALT+"+event.getDisplayLabel());
                curText.setTextSize(10);
            }
            else if(event.getDisplayLabel() != 0){
                curText.setText(String.valueOf(event.getDisplayLabel()));
            }
        }
        if(eventType == Constant.TAP_CLICK_EVENT){
            if(curText !=null &&!curText.getText().toString().equals("")){
                if(curView != null){
                    TextView curHintText = curView.findViewById(R.id.modify_dialog_tap_click_hint);
                    curHintText.setVisibility(View.GONE);
                }

            }

        }


        return super.onKeyUp(keyCode, event);
    }


    public int[] getCenterPostion(View view){
        // 获取TextView的左上角坐标
        int[] location = new int[2];
        view.getLocationOnScreen(location); // 或者使用getLocationInWindow(location)
        int width = view.getWidth();
        int height = view.getHeight();
        int centerX = location[0] + width / 2;
        int centerY = location[1] + height / 2;
        return new int[]{centerX,centerY};
    }


    // 创建窗口参数
    public WindowManager.LayoutParams createLayoutParams(){
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
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.gravity = Gravity.TOP | Gravity.START;
        params.width = 70;
        params.height = 70;
        return params;
    }

    // 创建窗口
    public WindowManager createWindow(){
        WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return windowManager;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void dragView(View view,String methodName){
            createTextListener(view,eventType);

        // 单击事件


        view.setClickable(true);
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
                            layoutParams.x =x + moveX - layoutParams.width/2;
                            layoutParams.y =y + moveY - layoutParams.height/2;
                            //更新View的位置
                            windowManager.updateViewLayout(view, layoutParams);
                            x = nowX;
                            y = nowY;
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
//                            curText = view.findViewById(R.id.modify_dialog_tap_click_edit);
//                            curHintText = view.findViewById(R.id.modify_dialog_tap_click_hint);
                            curView = view;

                            return true;
                        }
                        break;
                }
                return false;
            }
        });


    }



    @Override
    public void onClick(View view) {

    }

    // 保存
    public void save(){
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            LitePal.deleteAll(KeyMappingEntity.class, "planId = ?" , plans.get(0).getId().toString());
            LitePal.deleteAll(DirectMappingEntity.class, "planId = ?" , plans.get(0).getId().toString());
        }
        // 保存单个按键
        saveTapEvent();
        // 保存方向键
        saveDirectEvent();
        if(allView != null && !allView.isEmpty()){
            for (View view : allView){
                windowManager.removeView(view);
            }
        }
        allView.clear();
        tapView.clear();
        directView.clear();
        dismiss();
    }

    public void saveTapEvent(){
        // 单机事件view
        for (View view : tapView){
            // 清空已有的保存
            // 保存KeyMappingEntity
            KeyMappingEntity keyMapping = new KeyMappingEntity();
            int[] centerPostion = getCenterPostion(view);
            keyMapping.setX(centerPostion[0]);
            keyMapping.setY(centerPostion[1]);
            TextView textView = view.findViewById(R.id.modify_dialog_tap_click_edit);
            String keyValue = textView.getText().toString();
            keyMapping.setKeyValue(keyValue);
            keyMapping.setEventType(Constant.TAP_CLICK_EVENT);

            List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
            if(plans != null && plans.size() >=1){
                keyMapping.setPlanId(plans.get(0).getId());
            }else{
                Plan plan = new Plan();
                plan.setPlanName(planName);
                plan.save();
                keyMapping.setPlanId(plan.getId());
            }

            // 单个按键
            if(keyValue.length() == 1){
                int keycode = KeyEvent.keyCodeFromString(keyValue);
                keyMapping.setKeycode(keycode);
                keyMapping.setCombination(false);
                keyMapping.setCombinationKeyCode(0);
            }
            keyMapping.save();
        }
    }
    // 保存方向键
    public void saveDirectEvent(){
         for(View view : directView){
             DirectMappingEntity directMappingEntity = new DirectMappingEntity();
             int[] centerPostion = getCenterPostion(view);
             directMappingEntity.setX(centerPostion[0]);
             directMappingEntity.setY(centerPostion[1]);
             // 上键
             TextView up = view.findViewById(R.id.modify_dialog_direct_click_up);
             String upKeyValue = up.getText().toString();
             directMappingEntity.setUpKeyValue(upKeyValue);
             if(upKeyValue.length() == 1){
                 int keycode = KeyEvent.keyCodeFromString(upKeyValue);
                 directMappingEntity.setUpKeycode(keycode);
                 directMappingEntity.setUpCombination(false);
                 directMappingEntity.setUpCombinationKeyCode(0);
             }
             // 下键
             TextView down = view.findViewById(R.id.modify_dialog_direct_click_down);
             String downKeyValue = down.getText().toString();
             directMappingEntity.setDownKeyValue(downKeyValue);
             if(downKeyValue.length() == 1){
                 int keycode = KeyEvent.keyCodeFromString(downKeyValue);
                 directMappingEntity.setDownKeycode(keycode);
                 directMappingEntity.setDownCombination(false);
                 directMappingEntity.setDownCombinationKeyCode(0);
             }
             // 左键
             TextView left = view.findViewById(R.id.modify_dialog_direct_click_left);
             String leftKeyValue = left.getText().toString();
             directMappingEntity.setLeftKeyValue(leftKeyValue);
             if(leftKeyValue.length() == 1){
                 int keycode = KeyEvent.keyCodeFromString(leftKeyValue);
                 directMappingEntity.setLeftKeycode(keycode);
                 directMappingEntity.setLeftCombination(false);
                 directMappingEntity.setLeftCombinationKeyCode(0);
             }
             // 右键
             TextView right = view.findViewById(R.id.modify_dialog_direct_click_right);
             String rightKeyValue = right.getText().toString();
             directMappingEntity.setRightKeyValue(rightKeyValue);
             if(rightKeyValue.length() == 1){
                 int keycode = KeyEvent.keyCodeFromString(rightKeyValue);
                 directMappingEntity.setRightKeycode(keycode);
                 directMappingEntity.setRightCombination(false);
                 directMappingEntity.setRightCombinationKeyCode(0);
             }

             directMappingEntity.setEventType(Constant.DIRECTION_KEY);


             List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
             if(plans != null && plans.size() >=1){
                 directMappingEntity.setPlanId(plans.get(0).getId());
             }else{
                 Plan plan = new Plan();
                 plan.setPlanName(planName);
                 plan.save();
                 directMappingEntity.setPlanId(plan.getId());
             }


             directMappingEntity.save();
         }
    }




    public void showTapEvent(){
        List<KeyMappingEntity> curKeyMappingEntity = new ArrayList<>();
        // 取出planId
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            curKeyMappingEntity = LitePal.where("planId = ?", plan.getId().toString()).find(KeyMappingEntity.class);
        }
        for (KeyMappingEntity entity : curKeyMappingEntity){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.modify_dialog_tap_click, null, false);
            TextView modify_dialog_tap_click_edit = view.findViewById(R.id.modify_dialog_tap_click_edit);
            modify_dialog_tap_click_edit.setText(entity.getKeyValue());
            tapView.add(view);
            allView.add(view);
            dragView(view,"");
            layoutParams.x = entity.getX() - layoutParams.width/2;
            layoutParams.y = entity.getY() - layoutParams.height/2;
            windowManager.addView(view,layoutParams);
            createTextListener(view,entity.getEventType());
        }

    }

    public void showDirectEvent(){
        layoutParams.width = 80;
        layoutParams.height = 100;

        List<DirectMappingEntity> directMappingEntities = new ArrayList<>();
        // 取出planId
        List<Plan> plans = LitePal.where("planName = ?",planName).find(Plan.class);
        if(plans != null && plans.size() >=1){
            Plan plan = plans.get(0);
            directMappingEntities = LitePal.where("planId = ?", plan.getId().toString()).find(DirectMappingEntity.class);
        }
        for (DirectMappingEntity entity : directMappingEntities){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.modify_dialog_direct_click, null, false);
            TextView up = view.findViewById(R.id.modify_dialog_direct_click_up);
            up.setText(entity.getUpKeyValue());
            TextView down = view.findViewById(R.id.modify_dialog_direct_click_down);
            down.setText(entity.getDownKeyValue());
            TextView left = view.findViewById(R.id.modify_dialog_direct_click_left);
            left.setText(entity.getLeftKeyValue());
            TextView right = view.findViewById(R.id.modify_dialog_direct_click_right);
            right.setText(entity.getRightKeyValue());
            directView.add(view);
            allView.add(view);
            dragView(view,"");
            layoutParams.x = entity.getX() - layoutParams.width/2;
            layoutParams.y = entity.getY() - layoutParams.height/2;
            windowManager.addView(view,layoutParams);
            createTextListener(view,entity.getEventType());
        }

    }
    // 从数据库取出所有事件
    public void showView(){
        showTapEvent();
        showDirectEvent();
    }





    public void cancel(){
        if(windowManager !=null && allView != null && !allView.isEmpty()){
            for(View view: allView){
                windowManager.removeView(view);
            }
            allView.clear();
            tapView.clear();
            dismiss();
        }


    }


    @SuppressLint("ClickableViewAccessibility")
    public void createTextListener(View view,Integer evn){
        if(view == null) return;
        if(evn == Constant.TAP_CLICK_EVENT){
                TextView textView = view.findViewById(R.id.modify_dialog_tap_click_edit);
                if(textView != null){
                    textView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            curText = textView;
                            eventType = evn;
                            return false;
                        }
                    });
                }
            ImageView imageView = view.findViewById(R.id.modify_dialog_tap_click_delete);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    windowManager.removeView(view);
                    tapView.remove(view);
                    allView.remove(view);
                }
            });


        }

        if(evn == Constant.DIRECTION_KEY){

            ImageView imageView = view.findViewById(R.id.modify_dialog_tap_click_delete);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    windowManager.removeView(view);
                    directView.remove(view);
                    allView.remove(view);
                }
            });

            TextView up = view.findViewById(R.id.modify_dialog_direct_click_up);
            if(up != null){
                up.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        curText = up;
                        eventType = evn;
                        return false;
                    }
                });
            }

            TextView down = view.findViewById(R.id.modify_dialog_direct_click_down);
            if(down != null){
                down.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        curText = down;
                        eventType = evn;
                        return false;
                    }
                });
            }


            TextView left = view.findViewById(R.id.modify_dialog_direct_click_left);
            if(left != null){
                left.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        curText = left;
                        eventType = evn;
                        return false;
                    }
                });
            }

            TextView right = view.findViewById(R.id.modify_dialog_direct_click_right);
            if(right != null){
                right.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        curText = right;
                        eventType = evn;
                        return false;
                    }
                });
            }

        }


    }

}
