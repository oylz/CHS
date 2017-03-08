package com.mz.schudlerserver.task;

import java.util.HashMap;
import java.util.Map;

import com.mz.schudlerserver.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.schudlerserver.common.ConfigParams;
import com.mz.schudlerserver.common.GlobalParams;
import com.mz.schudlerserver.common.MsgCodeParams;
import com.mz.schudlerserver.netty.common.NettyResonse;
import com.mz.schudlerserver.netty.connhandler.HolderConnHandler;
import com.mz.schudlerserver.util.ConsistentHashUtil;
import com.mz.schudlerserver.util.HaProxyAddrsUtil;
import com.mz.schudlerserver.util.JsonUtils;
import com.mz.schudlerserver.util.Md5Encrypt;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class AnswerTask implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(AnswerTask.class);
	private String uri_  = "";
	private String host_ = "";
	private String clientAddr_ = "";
	private ChannelHandlerContext chc_ = null;
	public AnswerTask(String uri, String host, String clientAddr, ChannelHandlerContext chc){
		uri_ = uri;
		host_ = host;
		clientAddr_ = clientAddr;
		chc_ = chc;
	}
	@Override
	public void run() {
        String pre = "clientIP:" + clientAddr_;
        logger.info("beg----" + pre + ",请求host:" + host_ + "  uri:" + uri_);
		try{
	        //鉴权和URL校验
	        if(!authentication(chc_, pre, host_, uri_)){
	            return;
	        }
	        
	        //对客户端进行响应回写
	        if(uri_.split("\\?")[0].endsWith(ConfigParams.timeRequestStr)){
	            sendRequestTimeOK(chc_);
	        } else {
	            String[] params = uri_.split(GlobalParams.URL_SPLIT);
	            String roomId = params[1];
	            String anchorId = params[2];
	            sendRequestAddrOK(chc_, roomId, anchorId);
	        }
		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("AnswerTask:run出错:" + e.getMessage() + e.getStackTrace());
		}
		
	}	
	
    static public void sendBadUrlError(ChannelHandlerContext ctx, HttpResponseStatus status) throws Exception {
    	Map<String, Object> result = new HashMap<String, Object>();
     	ResponseModel responseModel = new ResponseModel(MsgCodeParams.BAD_REQUEST_STATUS_CODE,MsgCodeParams.BAD_REQUEST_MSG,result);
        FullHttpResponse response = NettyResonse.getMadeFullHttpResponse(JsonUtils.toJsonString(responseModel), status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    private void sendServerError(ChannelHandlerContext ctx, HttpResponseStatus status) throws Exception {
    	Map<String, Object> result = new HashMap<String, Object>();
     	ResponseModel responseModel = new ResponseModel(MsgCodeParams.INTERNAL_SERVER_ERROR_STATUS_CODE,MsgCodeParams.INTERNAL_SERVER_ERROR_MSG,result);
        FullHttpResponse response = NettyResonse.getMadeFullHttpResponse(JsonUtils.toJsonString(responseModel), status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendAuthFailError(ChannelHandlerContext ctx, HttpResponseStatus status) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        ResponseModel responseModel = new ResponseModel(MsgCodeParams.UNAUTHORIZED_STATUS_CODE,MsgCodeParams.UNAUTHORIZED_MSG,result);
        FullHttpResponse response = NettyResonse.getMadeFullHttpResponse(JsonUtils.toJsonString(responseModel), status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendOK(ChannelHandlerContext ctx, RespModel responseModel,
                        HttpResponseStatus responseStatus) throws Exception {
        String contentJson = JsonUtils.toJsonString(responseModel);
        String pre = "clientIP:" + ctx.channel().remoteAddress().toString();
        logger.info("end----" + pre + ",response:" + contentJson);
        FullHttpResponse response = NettyResonse.getMadeFullHttpResponse(contentJson, responseStatus);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 返回当前系统秒级时间
     * @param ctx
     * @throws Exception
     */
    private void sendRequestTimeOK(ChannelHandlerContext ctx) throws Exception {
        TimeRespModel respModel = new TimeRespModel(getNowSecond());
        sendOK(ctx, respModel, HttpResponseStatus.OK);
    }

    /**
     * 1.以roomid和anchorid组装的hash key，去一致性hash环中取出haproxy节点域名
     * @param ctx
     * @param roomId
     * @param anchorId
     * @throws Exception
     */
    private void sendRequestAddrOK(ChannelHandlerContext ctx, String roomId, String anchorId) throws Exception {
        String key = roomId + GlobalParams.HASH_KEY_SPLIT + anchorId;
        //去hash环取出相应的节点
        String haproxyNode = ConsistentHashUtil.getInstance().get(key);
        if (StringUtils.isNotBlank(haproxyNode)) {
            //去map取出已经拆分好的hostName
            HaproxyModel model = HaProxyAddrsUtil.getInstance().haproxyMap.get(haproxyNode);
            if (model != null) {
                SysAddrRespModel respModel = new SysAddrRespModel(model.getHostName());
                sendOK(ctx, respModel, HttpResponseStatus.OK);
            } else {
                sendServerError(ctx, HttpResponseStatus.OK);
            }
        } else {
            sendServerError(ctx, HttpResponseStatus.OK);
        }
    }

    /**
     * 鉴权及URL校验
     * @param uri
     * @return
     */
    private boolean authentication(ChannelHandlerContext channelHandlerContext, String pre, String host, String uri) throws Exception {
        String[] uriAndParam = uri.split("\\?");
        if(uriAndParam.length < 2){
            logger.error("end----" + pre + ",未携带鉴权参数，拒绝请求：" + host + "  uri:" + uri);
            sendBadUrlError(channelHandlerContext, HttpResponseStatus.OK);
            return false;
        }
        String requestUri = uriAndParam[0];
        Map<String, String> params = getParamsFromUri(uriAndParam[1]);
        if(!requestUri.endsWith(ConfigParams.timeRequestStr) && !requestUri.endsWith(ConfigParams.systemaddrRequestStr)){
            logger.error("end----" + pre + ",后缀不符合规范，拒绝请求：" + host + "  uri:" + uri);
            sendBadUrlError(channelHandlerContext, HttpResponseStatus.OK);
            return false;
        } else {
            if(!params.containsKey("r") || !params.containsKey("k")){
                logger.error("end----" + pre + ",不完整的鉴权参数，拒绝请求：" + host + "  uri:" + uri);
                sendBadUrlError(channelHandlerContext, HttpResponseStatus.OK);
                return false;
            }

            String md5Key = Md5Encrypt.md5(params.get("r") + ConfigParams.authenticationKey);
            if(!md5Key.equalsIgnoreCase(params.get("k"))){
                logger.error("end----" + pre + ",k值错误，拒绝请求：" + host + "  uri:" + uri + " md5:" + md5Key);
                sendAuthFailError(channelHandlerContext, HttpResponseStatus.OK);
                return false;
            }

            if(requestUri.endsWith(ConfigParams.systemaddrRequestStr)){
                String[] urlParams = requestUri.split(GlobalParams.URL_SPLIT);
                if (urlParams != null && urlParams.length > 4) {
                    String roomId = urlParams[1];
                    String anchorId = urlParams[2];
                    String versionId = urlParams[3];
                    //roomid和anchorid、versionId校验
                    if (StringUtils.isBlank(roomId) || StringUtils.isBlank(anchorId) || StringUtils.isBlank(versionId)) {
                        logger.error("end----" + pre + ",roomId/anchorId/versionId不完整，拒绝请求：" + host + "  uri:" + uri);
                        sendBadUrlError(channelHandlerContext, HttpResponseStatus.OK);
                        return false;
                    }
                } else {
                    logger.error("end----" + pre + ",roomId/anchorId/versionId不完整，拒绝请求：" + host + "  uri:" + uri);
                    sendBadUrlError(channelHandlerContext, HttpResponseStatus.OK);
                    return false;
                }

                try {
                    long now = getNowSecond();
                    long requestTime = Long.valueOf(params.get("r"));
                    if ((requestTime - now > ConfigParams.requestTimestampLimit) ||
                            (now - requestTime > ConfigParams.requestTimestampLimit)) {
                        logger.error("end----" + pre + ",时间不在允许的范围内，拒绝请求：" + host + "  uri:" + uri + " systemTime:" + now);
                        sendAuthFailError(channelHandlerContext, HttpResponseStatus.OK);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    logger.error("end----" + pre + ",时间格式不正确，拒绝请求：" + host + "  uri:" + uri);
                    sendBadUrlError(channelHandlerContext, HttpResponseStatus.OK);
                    return false;
                }
            }
        }
        return true;
    }

    private Map<String, String> getParamsFromUri(String paramStr){
        Map<String, String> params = new HashMap<>();
        String[] fullParam = paramStr.split("&");
        for (int i = 0; i < fullParam.length; i++) {
            String[] kv = fullParam[i].split("=");
            if(kv.length > 1) {
                params.put(kv[0], kv[1]);
            }
        }
        return params;
    }

    /**
     * 获取秒级别时间
     * @return
     */
    private long getNowSecond(){
        return System.currentTimeMillis() / 1000;
    }
	
}