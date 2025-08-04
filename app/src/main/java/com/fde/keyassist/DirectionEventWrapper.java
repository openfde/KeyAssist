package com.fde.keyassist;

import android.view.KeyEvent;

import com.fde.keyassist.event.EventUtils;

public class DirectionEventWrapper{
    KeyEvent keyEvent;
    EventUtils.Pointer pointer;
    int eventType;

    public DirectionEventWrapper(KeyEvent event, EventUtils.Pointer pointer, Integer eventType) {
        this.eventType = eventType;
        this.keyEvent = event;
        this.pointer = pointer;
    }

    @Override
    public String toString() {
        return "DirectionEventWrapper{" +
                "keyEvent=" + keyEvent +
                ", pointer=" + pointer +
                ", eventType=" + eventType +
                '}';
    }
}
