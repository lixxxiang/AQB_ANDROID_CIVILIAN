package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/7.
 */

public class checkMessage {
    String meta;
    Long civilianId;
    String civilianTel;

    public checkMessage(String meta, Long civilianId, String civilianTel) {
        this.meta = meta;
        this.civilianId = civilianId;
        this.civilianTel = civilianTel;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public Long getCivilianId() {
        return civilianId;
    }

    public void setCivilianId(Long civilianId) {
        this.civilianId = civilianId;
    }

    public String getCivilianTel() {
        return civilianTel;
    }

    public void setCivilianTel(String civilianTel) {
        this.civilianTel = civilianTel;
    }
}
