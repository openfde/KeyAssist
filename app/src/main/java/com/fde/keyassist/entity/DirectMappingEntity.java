package com.fde.keyassist.entity;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class DirectMappingEntity extends LitePalSupport {
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    private Integer x; //坐标x

    private Integer y; // 坐标y

    private Integer upKeycode; // 按键值
    private Integer downKeycode; // 按键值
    private Integer leftKeycode; // 按键值
    private Integer rightKeycode; // 按键值

    private String upKeyValue; // 按键名字
    private String downKeyValue; // 按键名字
    private String leftKeyValue; // 按键名字
    private String rightKeyValue; // 按键名字

    private Boolean upCombination; //是否是组合键
    private Boolean downCombination; //是否是组合键
    private Boolean rightCombination; //是否是组合键
    private Boolean leftCombination; //是否是组合键

    private Integer eventType;

    private Integer planId;

    @Column(defaultValue = "-1")
    private Integer upCombinationKeyCode; // 组合键的keycode

    @Column(defaultValue = "-1")
    private Integer downCombinationKeyCode; // 组合键的keycode

    @Column(defaultValue = "-1")
    private Integer leftCombinationKeyCode; // 组合键的keycode

    @Column(defaultValue = "-1")
    private Integer rightCombinationKeyCode; // 组合键的keycode

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

    public Integer getUpKeycode() {
        return upKeycode;
    }

    public void setUpKeycode(Integer upKeycode) {
        this.upKeycode = upKeycode;
    }

    public Integer getDownKeycode() {
        return downKeycode;
    }

    public void setDownKeycode(Integer downKeycode) {
        this.downKeycode = downKeycode;
    }

    public Integer getLeftKeycode() {
        return leftKeycode;
    }

    public void setLeftKeycode(Integer leftKeycode) {
        this.leftKeycode = leftKeycode;
    }

    public Integer getRightKeycode() {
        return rightKeycode;
    }

    public void setRightKeycode(Integer rightKeycode) {
        this.rightKeycode = rightKeycode;
    }

    public String getUpKeyValue() {
        return upKeyValue;
    }

    public void setUpKeyValue(String upKeyValue) {
        this.upKeyValue = upKeyValue;
    }

    public String getDownKeyValue() {
        return downKeyValue;
    }

    public void setDownKeyValue(String downKeyValue) {
        this.downKeyValue = downKeyValue;
    }

    public String getLeftKeyValue() {
        return leftKeyValue;
    }

    public void setLeftKeyValue(String leftKeyValue) {
        this.leftKeyValue = leftKeyValue;
    }

    public String getRightKeyValue() {
        return rightKeyValue;
    }

    public void setRightKeyValue(String rightKeyValue) {
        this.rightKeyValue = rightKeyValue;
    }

    public Boolean getUpCombination() {
        return upCombination;
    }

    public void setUpCombination(Boolean upCombination) {
        this.upCombination = upCombination;
    }

    public Boolean getDownCombination() {
        return downCombination;
    }

    public void setDownCombination(Boolean downCombination) {
        this.downCombination = downCombination;
    }

    public Boolean getRightCombination() {
        return rightCombination;
    }

    public void setRightCombination(Boolean rightCombination) {
        this.rightCombination = rightCombination;
    }

    public Boolean getLeftCombination() {
        return leftCombination;
    }

    public void setLeftCombination(Boolean leftCombination) {
        this.leftCombination = leftCombination;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getUpCombinationKeyCode() {
        return upCombinationKeyCode;
    }

    public void setUpCombinationKeyCode(Integer upCombinationKeyCode) {
        this.upCombinationKeyCode = upCombinationKeyCode;
    }

    public Integer getDownCombinationKeyCode() {
        return downCombinationKeyCode;
    }

    public void setDownCombinationKeyCode(Integer downCombinationKeyCode) {
        this.downCombinationKeyCode = downCombinationKeyCode;
    }

    public Integer getLeftCombinationKeyCode() {
        return leftCombinationKeyCode;
    }

    public void setLeftCombinationKeyCode(Integer leftCombinationKeyCode) {
        this.leftCombinationKeyCode = leftCombinationKeyCode;
    }

    public Integer getRightCombinationKeyCode() {
        return rightCombinationKeyCode;
    }

    public void setRightCombinationKeyCode(Integer rightCombinationKeyCode) {
        this.rightCombinationKeyCode = rightCombinationKeyCode;
    }

    public DirectMappingEntity() {
    }
}
