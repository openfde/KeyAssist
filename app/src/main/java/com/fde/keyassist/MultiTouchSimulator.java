package com.fde.keyassist;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.genymobile.scrcpy.Device;

public class MultiTouchSimulator {

    private static final String TAG = "MultiTouchSimulator";

    public static void simulateTwoFingerTouch(float x1, float y1, float x2, float y2, float rate) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        long endtime = SystemClock.uptimeMillis() + 8000;
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[10];
        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[10];
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
        int source = 0xd002;
        int deviceId = 10;
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
        long now = SystemClock.uptimeMillis();
        while (now < endtime){
            coords[0].y += rate; // 第一个触摸点向下移动
            coords[0].x += rate; // 第一个触摸点向下移动
            coords[1].y -= rate; // 第二个触摸点向下移动
            coords[1].x -= rate; // 第一个触摸点向下移动
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(
                    downTime, eventTime, MotionEvent.ACTION_MOVE, 2,
                    properties, coords, 0, 0, 1, 1,
                    deviceId, 0, source, 0);
            Device.injectEvent(event, 0, 2);
            now = SystemClock.uptimeMillis();
        }
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(
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
