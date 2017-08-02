package com.turlygazhy.entity;

/**
 * Created by user on 2/28/17.
 */
public class Goal {
    private int id;
    private String name;
    private int aim;
    private boolean timeLimit;
    private String startTime;
    private String endTime;

    //частота обновления цели
    private String period;
    //вид клавиатуры
    private String keyboard_type;
    //возможно ли выполнить цель
    private boolean fulfil;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAim(int aim) {
        this.aim = aim;
    }

    public int getAim() {
        return aim;
    }

    public void setTimeLimit(boolean timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isTimeLimit() {
        return timeLimit;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getKeyboard_type() {
        return keyboard_type;
    }

    public void setKeyboard_type(String keyboard_type) {
        this.keyboard_type = keyboard_type;
    }

    public boolean isFulfil() {
        return fulfil;
    }

    public void setFulfil(boolean fulfil) {
        this.fulfil = fulfil;
    }
}
