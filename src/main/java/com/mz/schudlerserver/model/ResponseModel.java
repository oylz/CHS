package com.mz.schudlerserver.model;

import java.io.Serializable;
/**
 * 
 * 
 * @author Yuanbin LIN
 * @date 2016年9月26日
 * @version $Revision$
 */
import java.util.Map;

import com.mz.schudlerserver.common.MsgCodeParams;



public class ResponseModel implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 8238652915515240403L;
	private int status;
	private String msg;
	private Map<String, Object> result;

	public ResponseModel() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public ResponseModel(Map<String, Object> result) {
		this.status = MsgCodeParams.SUCCESS_STATUS_CODE;
		this.msg = MsgCodeParams.SUCCESS_MSG;
		this.result = result;
	}
	
	




	public ResponseModel(int status, String msg, Map<String, Object> result) {
		super();
		this.status = status;
		this.msg = msg;
		this.result = result;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map<String, Object> getResult() {
		return result;
	}

	public void setResult(Map<String, Object> result) {
		this.result = result;
	}

}
