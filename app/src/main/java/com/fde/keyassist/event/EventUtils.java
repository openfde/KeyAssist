package com.fde.keyassist.event;

import static android.view.Display.DEFAULT_DISPLAY;
import static android.view.Display.INVALID_DISPLAY;
import static android.view.KeyEvent.ACTION_DOWN;


import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;




import com.fde.keyassist.R;
import com.fde.keyassist.util.Constant;
import com.genymobile.scrcpy.Device;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    public static void diretClick(View view, KeyEvent event, int x, int y, Integer eventType){
        view.post(()-> DirectionController.getInstance().process(event, x,  y, eventType));
    }

    public static String eventString(Integer eventType) {
        switch (eventType){
            case 1:
                return "TAP_CLICK_EVENT";
            case 2:
                return "DOUBLE_CLICK_EVENT";
            case 3:
                return "SWIPE";
            case 4:
                return "DIRECTION_KEY";
            case 5:
                return "DIRECTION_KEY_UP";
            case 6:
                return "DIRECTION_KEY_DOWN";
            case 7:
                return "DIRECTION_KEY_LEFT";
            case 8:
                return "DIRECTION_KEY_RIGHT";
            case 9:
                return "SCALE";
            case 10:
                return "AMPLIFY";
        }
        return "UNKNOWN EVENT";
    }


    public static class ZoomController {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        private Pointer center;
        long downTime, eventTime;
        private final long PARAM_TIME = 3000; // ms
        private final float DEFAULT_RATE = 2f; // speed
        private final float DEFAULT_ZOOMOUT_AREA_SIZE = 80; // finger distance
        private final float DEFAULT_ZOOMIN_AREA_SIZE = 200; // finger distance
        MotionEvent.PointerProperties[] properties;
        MotionEvent.PointerCoords[] coords;
        int source = 0xd002;
        int deviceId = 10;
        private volatile boolean stopped = true;
        private String TAG = "ZoomController";
        private boolean zoomIn;

        private static class SingletonHolder {
            private static final ZoomController INSTANCE = new ZoomController();
        }
        public static ZoomController getInstance(){
            return ZoomController.SingletonHolder.INSTANCE;
        }

        public ZoomController(){
        }

        public ZoomController setCenter(Pointer center) {
            this.center = center;
            return this;
        }

        public void startZoomInner(){
            Log.d(TAG, "startZoomInner():  ");
            if(!stopped){
                return;
            }
            stopped = false;
            float x1 = center.x - (zoomIn ? DEFAULT_ZOOMIN_AREA_SIZE : DEFAULT_ZOOMOUT_AREA_SIZE);
            float y1 = center.y ; //- DEFAULT_AREA_SIZE;
            float x2 = center.x + (zoomIn ? DEFAULT_ZOOMIN_AREA_SIZE : DEFAULT_ZOOMOUT_AREA_SIZE);
            float y2 = center.y ; // + DEFAULT_AREA_SIZE;
            float rate = zoomIn ? DEFAULT_RATE : - DEFAULT_RATE;
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis();
            properties = new MotionEvent.PointerProperties[10];
            coords = new MotionEvent.PointerCoords[10];
            properties[0] = new MotionEvent.PointerProperties();
            properties[0].id = 2;
            properties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
            coords[0] = new MotionEvent.PointerCoords();
            coords[0].x = x1;
            coords[0].y = y1;
            coords[0].pressure = 1;
            coords[0].size = 1;
            properties[1] = new MotionEvent.PointerProperties();
            properties[1].id = 3;
            properties[1].toolType = MotionEvent.TOOL_TYPE_FINGER;
            coords[1] = new MotionEvent.PointerCoords();
            coords[1].x = x2;
            coords[1].y = y2;
            coords[1].pressure = 1;
            coords[1].size = 1;
            Log.d(TAG, "startZoom():  ");
            MotionEvent event = MotionEvent.obtain(
                    downTime, eventTime, MotionEvent.ACTION_DOWN, 1, properties, coords, 0,
                    0, 1, 1,
                    deviceId, 0, source, 0);
            Device.injectEvent(event, 0, 2);
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(
                    downTime, eventTime,
                    MotionEvent.ACTION_POINTER_DOWN + (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                    2,
                    properties, coords, 0, 0, 1, 1,
                    deviceId, 0, source, 0);
            Device.injectEvent(event, 0, 2);
            while ( (coords[1].x > center.x || !zoomIn) && !stopped){
                Log.d(TAG, "startZoom():  " + stopped);
//                coords[0].y += rate; // 第一个触摸点向下移动
                coords[0].x += rate; // 第一个触摸点向下移动
//                coords[1].y -= rate; // 第二个触摸点向下移动
                coords[1].x -= rate; // 第一个触摸点向下移动
                eventTime = SystemClock.uptimeMillis();
                event = MotionEvent.obtain(
                        downTime, eventTime, MotionEvent.ACTION_MOVE, 2,
                        properties, coords, 0, 0, 1, 1,
                        deviceId, 0, source, 0);
                Device.injectEvent(event, 0, 2);
            }
        }

        public void startZoom(int repeatCount, boolean zoomIn) {
            this.zoomIn = zoomIn;
            if(repeatCount == 0){
                executor.execute(()->startZoomInner());
            }
        }

        public void stopZoom() {
            stopZoomInner();
        }

        public void stopZoomInner(){
            Log.d(TAG, "stopZoom():  ");
            stopped = true;
            eventTime = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(
                    downTime, eventTime,
                    MotionEvent.ACTION_POINTER_UP + (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                    2,
                    properties, coords, 0, 0, 1, 1,
                    deviceId, 0, source, 0);

            Device.injectEvent(event, 0, 2);
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(
                    downTime, eventTime, MotionEvent.ACTION_UP, 1,
                    properties, coords, 0, 0, 1, 1,
                    deviceId, 0, source, 0);
            Device.injectEvent(event, 0, 2);
        }

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



    public static class ClickController {
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final ExecutorService service = Executors.newSingleThreadExecutor();
        private volatile boolean stopped = true;
        private long downTime, eventTime;

        private final int source = 0xd002;
        private final int deviceId = 10;
        private int x, y; // 点击的坐标位置

        private static class SingletonHolder {
            private static final ClickController INSTANCE = new ClickController();
        }

        public static ClickController getInstance() {
            return SingletonHolder.INSTANCE;
        }

        public ClickController setClickPosition(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public void startClick() {
            if (!stopped) {
                return;
            }
            stopped = false;
            executor.execute(this::startClickInner);
        }

        public void stopClick() {
            stopped = true;
        }

        private void startClickInner() {
            while (!stopped) {
                performClick();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            stopped = true;
        }

        private void performClick() {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    long now = SystemClock.uptimeMillis();
                    injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_DOWN, now, now, x, y, 1.0f,
                            0);
                    injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_UP, now, now, x, y, 0.0f, 0);
                }
            });

        }
    }


}
