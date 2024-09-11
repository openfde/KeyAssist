package com.fde.keyassist.entity;

import org.litepal.crud.LitePalSupport;

public class DialogEntity extends LitePalSupport {
    private Integer id;
    private Boolean dialogSwitch; // 鼠标开关
    private Integer planId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getDialogSwitch() {
        return dialogSwitch;
    }

    public void setDialogSwitch(Boolean dialogSwitch) {
        this.dialogSwitch = dialogSwitch;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public DialogEntity() {
    }

    public DialogEntity(Integer id, Boolean dialogSwitch, Integer planId) {
        this.id = id;
        this.dialogSwitch = dialogSwitch;
        this.planId = planId;
    }
}
