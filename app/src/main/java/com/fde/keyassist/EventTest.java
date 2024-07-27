package com.fde.keyassist;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

public class EventTest {
    //向系统注入两点触摸事件
    private String TAG = "__Test__";

    PointerProperties[] properties = new PointerProperties[2];
    PointerProperties pp1 = new PointerProperties();
    PointerProperties pp2 = new PointerProperties();
    PointerCoords[] pointerCoords = new PointerCoords[2];

    EventTest() {
    }

    public void handleTouch(String args[]) throws Exception {
        String[] mArgs = args;
        try {
            String opt = mArgs[0];
            if (opt.equals("onetouch")) {
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis();
                float x = Float.valueOf(mArgs[1]);
                float y = Float.valueOf(mArgs[2]);
                int type = Integer.valueOf(mArgs[mArgs.length - 1]);
                PointerProperties pp1 = new PointerProperties();
                pp1.id = 0;
                pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
                properties[0] = pp1;

                PointerCoords pc1 = new PointerCoords();
                pc1.x = x;
                pc1.y = y;
                pc1.pressure = 1;
                pc1.size = 1;
                pointerCoords[0] = pc1;

                Log.i(TAG, "__one PointTouch__" + x + "___" + y + "___");
                MotionEvent event = null;
                if(type == 0){
                    event = MotionEvent.obtain(downTime, eventTime,
                            MotionEvent.ACTION_DOWN, 1, properties, pointerCoords,
                            0, 0, 1, 1, 0, 0, 0, 0);
                }else{
                    event = MotionEvent.obtain(downTime, eventTime,
                            MotionEvent.ACTION_UP, 1, properties, pointerCoords,
                            0, 0, 1, 1, 0, 0, 0, 0);
                }
                sendPointerTest(event);
            } else if (opt.equals("twotouch")) {
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis();
                float x = Float.valueOf(mArgs[1]);
                float y = Float.valueOf(mArgs[2]);
                int type = Integer.valueOf(mArgs[mArgs.length - 1]);
                PointerProperties pp2 = new PointerProperties();
                pp2.id = 1;
                pp2.toolType = MotionEvent.TOOL_TYPE_FINGER;
                properties[1] = pp2;

                PointerCoords pc2 = new PointerCoords();
                pc2.x = x;
                pc2.y = y;
                pc2.pressure = 1;
                pc2.size = 1;
                pointerCoords[1] = pc2;

                Log.i(TAG, "__two PointTouch__" + x + "___" + y + "___");
                MotionEvent event = null;
                if (type == 0) {
                    event = MotionEvent.obtain(downTime, eventTime,
                            MotionEvent.ACTION_DOWN, 2, properties,
                            pointerCoords, 0, 0, 1, 1, 3, 0, 0x2002, 0);
                    Log.d(TAG, "handleTouch: event" + event);
                } else {
                    event = MotionEvent.obtain(downTime, eventTime,
                            MotionEvent.ACTION_UP, 2, properties,
                            pointerCoords, 0, 0, 1, 1, 3, 0, 0x2002, 0);
                    Log.d(TAG, "handleTouch: event" + event);
                }
                sendPointerTest(event);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void sendPointerTest(MotionEvent event) {
        Log.d("TAG", "sendPointerTest: event:" + event + "");
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendPointerSync(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}