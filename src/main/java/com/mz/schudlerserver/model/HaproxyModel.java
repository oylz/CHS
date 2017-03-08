package com.mz.schudlerserver.model;

import java.io.Serializable;

public class HaproxyModel implements Serializable{
	private static final long serialVersionUID = 9150823650803053521L;
	
	private String ip;
	private String port;
    private String hostName;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
	public String toString() {
		return "HaproxyModel [ip=" + ip + ", port=" + port + ", hostName=" + hostName + "]";
	}
	
	

}
