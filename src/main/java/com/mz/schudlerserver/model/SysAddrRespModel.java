package com.mz.schudlerserver.model;

import com.mz.schudlerserver.common.MsgCodeParams;


public class SysAddrRespModel extends RespModel {
    private String hostName;

    public SysAddrRespModel() {
    }

    public SysAddrRespModel(String hostName) {
        super.setStatus(MsgCodeParams.SUCCESS_STATUS_CODE);
        this.setMsg(MsgCodeParams.SUCCESS_MSG);
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
