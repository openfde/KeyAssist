package com.fde.keyassist;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;

import static com.fde.keyassist.util.Constant.DIRECTION_EVENT;
import static com.fde.keyassist.util.Constant.DIRECTION_EVENT_DOWN;
import static com.fde.keyassist.util.Constant.DIRECTION_EVENT_MOVE;
import static com.fde.keyassist.util.Constant.DIRECTION_EVENT_UP;
import static com.fde.keyassist.util.Constant.DIRECTION_KEY_UP;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.fde.keyassist.event.EventUtils;
import com.fde.keyassist.util.Constant;

import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DirectionController {

    private static final String TAG = "DirectionController";
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    //        ExecutorService executor = Executors.newSingleThreadExecutor();
    private static float directX1 = 280f, directY1 = 675f;
    private static float swipeLength = 100f;
    private static int swipeDuration = 500;
    private static int swipeSource = 0xd002;

    private int mDirection;  // 0x1111 ADWS
    private int mSpeed;
    private int mDuration;
    private EventUtils.Pointer center = new EventUtils.Pointer(), current= new EventUtils.Pointer();
    private long downTime, when;

    private boolean isMoving, isDowning;

    private static final int IDLE =             0;
    private static final int DIRECTION_DOWN =   0x1;           // S
    private static final int DIRECTION_UP =     0x2;             // W
    private static final int DIRECTION_RIGHT =  0x4;          // D
    private static final int DIRECTION_LEFT =   0x8;           // A

    private static final int DIRECTION_UP_LEFT =    DIRECTION_UP | DIRECTION_LEFT;        //WA
    private static final int DIRECTION_DOWN_LEFT =  DIRECTION_DOWN | DIRECTION_LEFT;      //SA
    private static final int DIRECTION_UP_RIGHT =   DIRECTION_RIGHT | DIRECTION_UP;       //WD
    private static final int DIRECTION_DOWN_RIGHT = DIRECTION_DOWN | DIRECTION_RIGHT;     //SD

    private int DIRECTION = IDLE;

    private static final int action_idle = -1;
    private static final int action_down = MotionEvent.ACTION_DOWN;
    private static final int action_move= MotionEvent.ACTION_MOVE;
    private static final int action_up = MotionEvent.ACTION_UP;
    private static final int action = action_idle;

    private static final int DELAY_TIME = 33;

    private DirectionEventWrapper[] mDirectionWrappers = new DirectionEventWrapper[4];

    private HandlerThread handlerThread = new HandlerThread("event_executor");
    private Handler workHandler;

    private volatile boolean batchDroped;
    private int lastIndex = 0;

    public static DirectionController getInstance(){
        return SingletonHolder.INSTANCE;
    }

    public void startWorkThread(){
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case DIRECTION_EVENT:{
                        DirectionEventWrapper eventWrapper = (DirectionEventWrapper) msg.obj;
                        updateEventWrapper(eventWrapper);
                        int direction = calculateDirection();
                        processDirection(eventWrapper, direction);
                    }
                    break;
                    case DIRECTION_EVENT_DOWN:{
                        DirectionEventWrapper eventWrapper = (DirectionEventWrapper) msg.obj;
                        int direction = msg.arg1;
                        EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_DOWN, eventWrapper.keyEvent.getDownTime(),
                                eventWrapper.keyEvent.getDownTime(),
                                center.x, center.y, 1.0f,
                                0);
                        if(downTime == 0){
                            downTime = eventWrapper.keyEvent.getDownTime();
                        }
                        isDowning = true;
                        DIRECTION = direction;
                    }
                    break;
                    case DIRECTION_EVENT_MOVE: {
                        int direction = msg.arg1;
                        EventUtils.Pointer pointer = computeOffset(direction);
                        current.x =  center.x + pointer.x;
                        current.y =  center.y + pointer.y;
                        long now = SystemClock.uptimeMillis();
                        EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_MOVE, downTime, now,
                                current.x, current.y, 0.5f,
                                0);
                        DIRECTION = direction;
                    }
                    break;
                    case DIRECTION_EVENT_UP:{
                        DirectionEventWrapper eventWrapper = (DirectionEventWrapper) msg.obj;
                        int direction = msg.arg1;
                        EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_UP,
                                downTime, eventWrapper.keyEvent.getDownTime(),
                                current.x, current.y, 1.0f,
                                0);
                        current.x = center.x;
                        current.y = center.y;
                        downTime = 0;
                        isDowning = false;
                        DIRECTION = direction;
                    }
                    break;
                    default:
                        break;
                }
            }
        };
    }

    private void processDirection(DirectionEventWrapper eventWrapper, int direction) {
        Log.d(TAG, "processDirection(): direction :" + printDIRECTION(direction) + " eventWrapper:" + eventWrapper);
        if(DIRECTION == IDLE && direction != IDLE ){
            int delay = workHandler.hasMessages(DIRECTION_EVENT_MOVE) ? DELAY_TIME : 0;
            Message messageDown = Message.obtain();
            messageDown.obj = eventWrapper;
            messageDown.what = DIRECTION_EVENT_DOWN;
            workHandler.sendMessage(messageDown);
            Message message = Message.obtain();
            message.obj = eventWrapper;
            message.arg1 = direction;
            message.what = DIRECTION_EVENT_MOVE;
            workHandler.sendMessage(message);
        } else if(direction == IDLE){
            int delay = workHandler.hasMessages(DIRECTION_EVENT_MOVE) ? DELAY_TIME : 0;
            Message messageDown = Message.obtain();
            messageDown.obj = eventWrapper;
            messageDown.arg1 = direction;
            messageDown.what = DIRECTION_EVENT_UP;
            workHandler.sendMessage(messageDown);
        } else {
            int delay = workHandler.hasMessages(DIRECTION_EVENT_MOVE) ? DELAY_TIME : 0;
            Message message = Message.obtain();
            message.obj = eventWrapper;
            message.arg1 = direction;
            message.what = DIRECTION_EVENT_MOVE;
            workHandler.sendMessage(message);
//            EventUtils.Pointer pointer = computeOffset(direction);
//            current.x =  center.x + pointer.x;
//            current.y =  center.y + pointer.y;
//            long now = SystemClock.uptimeMillis();
//            EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_MOVE, downTime, now,
//                    current.x, current.y, 0.5f,
//                    0);
        }
        Log.d(TAG, "processDirection: directon:" + printDIRECTION(DIRECTION) + " current:" + current);
    }

    private String printDIRECTION(int d){
        switch (d){
            case IDLE:
                return "IDLE";
            case DIRECTION_DOWN:
                return "DIRECTION_DOWN";
            case DIRECTION_UP:
                return "DIRECTION_UP";
            case DIRECTION_RIGHT:
                return "DIRECTION_RIGHT";
            case DIRECTION_LEFT:
                return "DIRECTION_LEFT";
            case DIRECTION_UP_LEFT:
                return "DIRECTION_UP_LEFT";
            case DIRECTION_DOWN_LEFT:
                return "DIRECTION_DOWN_LEFT";
            case DIRECTION_UP_RIGHT:
                return "DIRECTION_UP_RIGHT";
            case DIRECTION_DOWN_RIGHT:
                return "DIRECTION_DOWN_RIGHT";
        }
        return "IDLE";
    }

    private void updateEventWrapper(DirectionEventWrapper eventWrapper) {
        int eventType = eventWrapper.eventType;
        int index = eventType - 5;
        if(eventWrapper.keyEvent.getAction() == ACTION_UP){
            mDirectionWrappers[index] = null;
        } else if(eventWrapper.keyEvent.getAction() == ACTION_DOWN){
//             if(index == 0){
//                 mDirectionWrappers[1] = null;
//             }
//             if(index == 1){
//                 mDirectionWrappers[0] = null;
//             }
//             if(index == 2){
//                 mDirectionWrappers[3] = null;
//             }
//             if(index == 3){
//                 mDirectionWrappers[2] = null;
//             }
            lastIndex = index;
            mDirectionWrappers[index] = eventWrapper;
        } else {
            mDirectionWrappers[index] = eventWrapper;
        }
        int count = 0;
        String direct = "key pressed:";
        if (mDirectionWrappers[0] != null){
            direct += "+ up ";
            count++;
        }
        if (mDirectionWrappers[1] != null){
            direct += "+ down ";
            count++;
        }
        if (mDirectionWrappers[2] != null){
            direct += "+ left ";
            count++;
        }
        if (mDirectionWrappers[3] != null){
            direct += "+ right ";
            count++;
        }
        if(count == 1){
            lastIndex = -1;
        }
        Log.d(TAG, "updateEventWrapper: direct:" + direct);
    }

    private int calculateDirection() {

        int direction = IDLE;
        if(mDirectionWrappers[0] != null && lastIndex != 1){
            direction += DIRECTION_UP;
        }
        if(mDirectionWrappers[1] != null && lastIndex != 0){
            direction += DIRECTION_DOWN;
        }
        if(mDirectionWrappers[2] != null && lastIndex != 3){
            direction += DIRECTION_LEFT;
        }
        if(mDirectionWrappers[3] != null && lastIndex != 2 ){
            direction += DIRECTION_RIGHT;
        }
        if(direction == DIRECTION_UP|| direction == DIRECTION_DOWN ||
                direction == DIRECTION_LEFT|| direction == DIRECTION_RIGHT){
            return direction;
        }
        if(direction == DIRECTION_UP_LEFT|| direction == DIRECTION_DOWN_LEFT ||
                direction == DIRECTION_UP_RIGHT|| direction == DIRECTION_DOWN_RIGHT){
            return direction;
        }
        return direction;
    }

    public void handleEvent(DirectionEventWrapper eventWrapper){
        if(workHandler != null){
            Message message = Message.obtain();
            message.what = DIRECTION_EVENT;
            message.obj = eventWrapper;
            workHandler.sendMessage(message);
        }
    }

    public synchronized void process(KeyEvent event, int x, int y, Integer eventType) {
        Log.d(TAG, "process():  x :" + x + ", y :" + y + ", eventType :" + eventType + " event :" + event);
//            leftPressed = rightPressed = upPressed = downPressed = false;
        setCenter(new EventUtils.Pointer(x,y));
        int action = event.getAction();
        int directBit = -1;

        if(eventType == DIRECTION_KEY_UP){
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
                    downTime, event.getDownTime(),
                    current.x, current.y, 1.0f,
                    0);
            downTime = 0;
            isMoving = false;
            this.mDirection = direct;
        } else {
            EventUtils.Pointer pointer = computeOffset(direct);
            processInnerOnce(direct, pointer, event.getDownTime(), false);
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

    private EventUtils.Pointer computeOffset(int direct) {
        float horizental  =  (direct & DIRECTION_RIGHT) != 0 ? swipeLength :
                (direct & DIRECTION_LEFT) != 0 ? - swipeLength : 0;
        float vertical  = (direct & DIRECTION_DOWN) != 0 ? swipeLength :
                (direct & DIRECTION_UP) != 0 ? - swipeLength : 0;
        return new EventUtils.Pointer(horizental, vertical);
    }

    private void processInnerOnce(int direct, EventUtils.Pointer pointer, long down, boolean once) {
        String format = String.format(" direct:%x, pointer:%s", direct, pointer);
        long now = SystemClock.uptimeMillis();
        if(mDirection == 0 &&  direct != 0){
            batchDroped = false;
            EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_DOWN, down, down,
                    center.x, center.y, 1.0f,
                    0);
            if(downTime == 0){
                downTime = down;
            }
            current = center;
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
        EventUtils.injectMotionEvent(swipeSource, MotionEvent.ACTION_MOVE, downTime, now,
                center.x + horizental, center.y + vertical, 0.5f,
                0);
        current.x = center.x + horizental;
        current.y = center.y + vertical;
        isMoving = true;
    }

    private static class SingletonHolder {
        private static final DirectionController INSTANCE = new DirectionController();
    }

    private DirectionController(){
//            setCenter(new Pointer(x, y));
    }

    public void setCenter(EventUtils.Pointer center){
        this.center = center;
    }

}