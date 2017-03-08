package com.mz.schudlerserver.model;

import com.mz.schudlerserver.common.MsgCodeParams;


public class TimeRespModel extends RespModel {
    private long systemTime;

    public TimeRespModel() {
    }

    public TimeRespModel(long systemTime) {
        super.setStatus(MsgCodeParams.SUCCESS_STATUS_CODE);
        this.setMsg(MsgCodeParams.SUCCESS_MSG);
        this.systemTime = systemTime;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }
}
