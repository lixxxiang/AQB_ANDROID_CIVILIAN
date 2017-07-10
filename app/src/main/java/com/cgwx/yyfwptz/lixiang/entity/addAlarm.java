package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/10.
 */

public class addAlarm {
    String meta;
    String alarmId;

    public addAlarm(String meta, String alarmId) {
        this.meta = meta;
        this.alarmId = alarmId;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }
}
