package com.fde.keyassist.entity;


import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class KeyMappingEntity extends LitePalSupport {
    private Integer id;

    private Integer x; //坐标x

    private Integer y; // 坐标y

    private Integer keycode; // 按键值

    private String keyValue; // 按键名字

    private Boolean combination; //是否是组合键

    private Integer eventType;

    private Integer planId;

    @Column(defaultValue = "-1")
    private Integer combinationKeyCode; // 组合键的keycode

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getKeycode() {
        return keycode;
    }

    public void setKeycode(Integer keycode) {
        this.keycode = keycode;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public Boolean getCombination() {
        return combination;
    }

    public void setCombination(Boolean combination) {
        this.combination = combination;
    }

    public Integer getCombinationKeyCode() {
        return combinationKeyCode;
    }

    public void setCombinationKeyCode(Integer combinationKeyCode) {
        this.combinationKeyCode = combinationKeyCode;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public KeyMappingEntity(Integer x, Integer y, Integer keycode, String keyValue, Boolean combination, Integer eventType, Integer planId, Integer combinationKeyCode) {
        this.x = x;
        this.y = y;
        this.keycode = keycode;
        this.keyValue = keyValue;
        this.combination = combination;
        this.eventType = eventType;
        this.planId = planId;
        this.combinationKeyCode = combinationKeyCode;
    }

    public KeyMappingEntity() {
    }
}
