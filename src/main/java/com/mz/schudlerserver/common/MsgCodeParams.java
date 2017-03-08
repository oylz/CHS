package com.mz.schudlerserver.common;


public class MsgCodeParams {
	public static final int SUCCESS_STATUS_CODE = 0000;
	public static final String SUCCESS_MSG = "ok";
	
	//已创建的房间（重复发送创建房间请求）
	public static final int CREATE_STATUS_CODE = 1101;
	public static final String CREATE_MSG = "existing room";
	
	//已存在待连麦用户重复发送连麦请求
	public static final int IN_STAY_STATUS_CODE = 1102;
	public static final String IN_STAY_MSG = "existing in stay connected list";
	
	//已存在的已连麦用户列表中的用户重复发送连麦请求
	public static final int IN_CONNECTED_STATUS_CODE = 1103;
	public static final String IN_CONNECTED_MSG = "exiting in connected list";
	
	//主播重复答应连麦请求（用户已在连麦列表中）
	public static final int AGREE_ALEARY_STATUS_CODE = 1104;
	public static final String AGREE_ALEARY_MSG = "agree already in connected list";
	
	//观众重复发送取消连麦（已连麦用户列表和待连麦用户列表中都没有此用户）
	public static final int DISAGREE_ALEARY_STATUS_CODE = 1105;
	public static final String DISAGREE_ALEARY_MSG = "forbidden";
	
	//主播重复踢人（已连麦用户列表和待连麦用户列表中都没有此用户）
	public static final int REMOVE_ALEARY_STATUS_CODE = 1106;
	public static final String REMOVE_ALEARY_MSG = "remove already not in connected list";
	
	//房间不存在
	public static final int NOT_EXIST_ROOM_STATUS_CODE = 1107;
	public static final String NOT_EXIST_ROOM_MSG = "room is not existing";
	
	
	//不符合规范的请求url或者参数
	public static final int BAD_REQUEST_STATUS_CODE = 2301;
	public static final String BAD_REQUEST_MSG = "bad request";
	
	//未授权的请求或者请求鉴权失败
	public static final int UNAUTHORIZED_STATUS_CODE = 2302;
	public static final String UNAUTHORIZED_MSG = "unauthorized";
	
	//服务器异常
	public static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 3301;
	public static final String INTERNAL_SERVER_ERROR_MSG = "internal server error";
	
	
	
	

	

}
