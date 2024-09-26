package com.fde.keyassist.util;

public class Constant {
    public final static Integer TAP_CLICK_EVENT = 1; // 单击事件
    public final static Integer DOUBLE_CLICK_EVENT = 2; // 双击事件 not now,  for continus click
    public final static Integer SWIPE = 3; // 滑动
    public final static Integer DIRECTION_KEY = 4; // 方向键
    public final static Integer DIRECTION_KEY_UP = 5;
    public final static Integer DIRECTION_KEY_DOWN = 6;
    public final static Integer DIRECTION_KEY_LEFT = 7;
    public final static Integer DIRECTION_KEY_RIGHT = 8;
    public static final Integer SCALE = 9;
    public static final Integer AMPLIFY = 10;
    public static String planName = "王者荣耀";

    public static String toEventString(int eventType){
        switch(eventType) {
            case 1:
                return "单击事件";
            case 2:
                return "双击事件";
            case 3:
                return "滑动";
            case 4:
                return "方向键";
            case 5:
                return "方向键上";
            case 6:
                return "方向键下";
            case 7:
                return "方向键左";
            case 8:
                return "方向键右";
        }
        return null;
    }
}
