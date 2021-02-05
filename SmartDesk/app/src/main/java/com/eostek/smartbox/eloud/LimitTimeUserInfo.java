package com.eostek.smartbox.eloud;

public class LimitTimeUserInfo {

    private int Id;

    private int state;

    private int UserId;

    private String beginTime;

    private String EndTime;

    private static long limitStartTime;

    private static long limitEndTime;

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int id) {
        this.UserId = id;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String time) {
        this.beginTime = time;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String time) {
        this.EndTime = time;
    }

    public String toString(){
        return "UserId: " + UserId + ", beginTime: " + beginTime + ", EndTime: " + EndTime;
    }

    public void setState(int work_station_busy) {
        state = work_station_busy;
    }

    public int getState() {
        return state;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }

    public long getLimitStartTime() {
        return limitStartTime;
    }

    public void setLimitStartTime(long time) {
        this.limitStartTime = time;
    }

    public long getLimitEndTime() {
        return limitEndTime;
    }

    public void setLimitEndTime(long time) {
        this.limitEndTime = time;
    }
}
