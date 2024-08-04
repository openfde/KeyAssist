package com.fde.keyassist.event;

import static android.view.Display.DEFAULT_DISPLAY;
import static android.view.Display.INVALID_DISPLAY;
import static android.view.KeyEvent.ACTION_DOWN;


import android.app.Instrumentation;
import android.app.Service;
import android.app.UiAutomation;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;



import com.fde.keyassist.R;
import com.fde.keyassist.util.Constant;
import com.genymobile.scrcpy.Device;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventUtils {


    public static void injectMotionEvent(int inputSource, int action, long downTime, long when,
                                         float x, float y, float pressure, int displayId) {
        final float DEFAULT_SIZE = 1.0f;
        final int DEFAULT_META_STATE = 0;
        final float DEFAULT_PRECISION_X = 1.0f;
        final float DEFAULT_PRECISION_Y = 1.0f;
        final int DEFAULT_EDGE_FLAGS = 0;
        MotionEvent event = MotionEvent.obtain(downTime, when, action, x, y, pressure, DEFAULT_SIZE,
                DEFAULT_META_STATE, DEFAULT_PRECISION_X, DEFAULT_PRECISION_Y,
                4, DEFAULT_EDGE_FLAGS);
        event.setSource(0xd002);
        if (displayId == INVALID_DISPLAY && (inputSource & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            displayId = DEFAULT_DISPLAY;
        }
//        event.setDisplayId(displayId);
        Device.injectEvent(event, 0,
                2);
    }

    // 连击事件
    public static void doubleClick(int x,int y,int count){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<count;i++){
                    long now = SystemClock.uptimeMillis();
                    injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_DOWN, now, now, x, y, 1.0f,
                            0);
                    injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_UP, now, now, x, y, 0.0f, 0);
                }
            }
        }).start();
    }

    // 点击事件
    public static void tapClick(int x, int y) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long now = SystemClock.uptimeMillis();
                injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_DOWN, now, now, x, y, 1.0f,
                        0);
                injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_UP, now, now, x, y, 0.0f, 0);
            }
        }).start();


        // 发送按下事件
        // 获取屏幕的中心坐标
        // 创建并发送MotionEvent




    }

    public static void diretClick(View view, KeyEvent event, int x, int y, Integer eventType){
        view.post(()-> DirectionController.getInstance().process(event, x,  y, eventType));
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                DirectionController.getInstance().process(event, x,  y, eventType);
//            }
//        }).start();
    }


    public static class DirectionController {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        private static float directX1 = 280f, directY1 = 675f;
        private static float swipeLength = 75f;
        private static int swipeDuration = 500;
        private static int swipeSource = 0xd002;

        private int mDirection;  // 0x1111 ADWS
        private int mSpeed;
        private int mDuration;
        private Pointer center,current;

        private boolean isMoving;
        private static final int DIRECTION_DOWN = 0x1;           // S
        private static final int DIRECTION_UP = 0x2;             // W
        private static final int DIRECTION_RIGHT = 0x4;          // D
        private static final int DIRECTION_LEFT = 0x8;           // A

        private static final int DIRECTION_UP_LEFT = DIRECTION_UP | DIRECTION_LEFT;        //WA
        private static final int DIRECTION_DOWN_LEFT = DIRECTION_DOWN | DIRECTION_LEFT;      //SA
        private static final int DIRECTION_UP_RIGHT = DIRECTION_RIGHT | DIRECTION_UP;       //WD
        private static final int DIRECTION_DOWN_RIGHT = DIRECTION_DOWN | DIRECTION_RIGHT;     //SD

        private volatile boolean batchDroped;

        public static DirectionController getInstance(){
            return SingletonHolder.INSTANCE;
        }

        public synchronized void process(KeyEvent event,int x, int y,Integer eventType) {
//            leftPressed = rightPressed = upPressed = downPressed = false;
            setCenter(new Pointer(x,y));
            int action = event.getAction();
            int directBit = -1;

            if(eventType == Constant.DIRECTION_KEY_UP){
                directBit = 1;
            }else if(eventType == Constant.DIRECTION_KEY_DOWN){
                directBit = 0;
            }else if(eventType == Constant.DIRECTION_KEY_LEFT){
                directBit = 3;
            }else if(eventType == Constant.DIRECTION_KEY_RIGHT){
                directBit = 2;
            }
            int direct = updateDirection(action, directBit);
            if(direct == 0) {
                batchDroped = true;
                EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_UP,
                        event.getDownTime(), event.getDownTime(),
                        center.x, center.y, 1.0f,
                        0);
                isMoving = false;
                this.mDirection = direct;
            } else {
                Pointer pointer = computeOffset(direct);
                executor.execute(()->processInnerOnce(direct, pointer, event.getDownTime(), false));
            }
        }

        private int updateDirection(int action, int directBit) {
            int direction = mDirection;
            switch (directBit){
                case 0: //S
                    direction = action == ACTION_DOWN ? mDirection | (1 << 0) : mDirection & (~(1 << 0));
                    int mask = ~(1 << 1);
                    direction = action == ACTION_DOWN ? direction & mask : direction;
                    break;
                case 1: //W
                    direction = action == ACTION_DOWN ? mDirection | (1 << 1) : mDirection & (~(1 << 1));
                    direction = action == ACTION_DOWN ? direction & (~(1 << 0)): direction;
                    break;
                case 2: //D
                    direction = action == ACTION_DOWN ? mDirection | (1 << 2) : mDirection & (~(1 << 2));
                    direction = action == ACTION_DOWN ? direction & (~(1 << 3)) : direction;
                    break;
                case 3: //A
                    direction = action == ACTION_DOWN ? mDirection | (1 << 3) : mDirection & (~(1 << 3));
                    direction = action == ACTION_DOWN ? direction & (~(1 << 2)) : direction;
                    break;
                default:
                    break;
            }
            return direction;
        }

        public void setDirection(int direction) {
            if (direction == DIRECTION_UP || direction == DIRECTION_DOWN ||
                    direction == DIRECTION_LEFT || direction == DIRECTION_RIGHT) {
                mDirection = direction;
            } else {
                throw new IllegalArgumentException("Invalid direction");
            }
        }

        private Pointer computeOffset(int direct) {
            float horizental  =  (direct & DIRECTION_RIGHT) != 0 ? swipeLength :
                    (direct & DIRECTION_LEFT) != 0 ? - swipeLength : 0;
            float vertical  = (direct & DIRECTION_DOWN) != 0 ? swipeLength :
                    (direct & DIRECTION_UP) != 0 ? - swipeLength : 0;
            return new Pointer(horizental, vertical);
        }

        private void processInnerOnce(int direct, Pointer pointer, long down, boolean once) {
            String format = String.format(" direct:%x, pointer:%s", direct, pointer);
            long now = SystemClock.uptimeMillis();
            if(mDirection == 0 &&  direct != 0){
                batchDroped = false;
                EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_DOWN, down, down,
                        center.x, center.y, 1.0f,
                        0);
                isMoving = false;
                moveOnce(pointer.x, pointer.y);
            } else if ( !isMoving  || mDirection != direct || once ){
                moveOnce(pointer.x, pointer.y);
            }
            this.mDirection = direct;
            long duration = SystemClock.uptimeMillis() - now;

        }

        private void moveOnce(float horizental, float vertical) {
            if(batchDroped) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_MOVE, now, now,
                    center.x + horizental, center.y + vertical, 1.0f,
                    0);
            isMoving = true;
        }

        private static class SingletonHolder {
            private static final DirectionController INSTANCE = new DirectionController();
        }

        private DirectionController(){
//            setCenter(new Pointer(x, y));
        }

        private void setCenter(Pointer center){
            this.center = center;
        }

        public static class Pointer {
            public Pointer(float x, float y) {
                this.x = x;
                this.y = y;
            }
            public Pointer(){

            }
            public float x , y;

            @Override
            public String toString() {
                return "Pointer{" +
                        "x=" + x +
                        ", y=" + y +
                        '}';
            }
        }

    }

}
