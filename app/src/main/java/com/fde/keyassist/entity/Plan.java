package com.fde.keyassist.entity;

import org.litepal.crud.LitePalSupport;

//project
public class Plan extends LitePalSupport {
    private Integer id;
    private String planName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Plan() {
    }

    public Plan(Integer id, String planName) {
        this.id = id;
        this.planName = planName;
    }
}
