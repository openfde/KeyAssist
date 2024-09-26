package com.fde.keyassist.entity;

import org.litepal.crud.LitePalSupport;

public class CursorEntity extends LitePalSupport {
    private Integer id;
    private Boolean cursorSwitch; // 鼠标开关
    private Integer planId;

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getCursorSwitch() {
        return cursorSwitch;
    }

    public void setCursorSwitch(Boolean cursorSwitch) {
        this.cursorSwitch = cursorSwitch;
    }
}
