package com.mz.schudlerserver.netty.connhandler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.schudlerserver.task.AnswerTask;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;


public class HolderConnHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HolderConnHandler.class);
    /**
     * 读取http请求中的数据，并对将数据发送到RocketMQ
     * @param channelHandlerContext
     * @param fullHttpRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if(!fullHttpRequest.decoderResult().isSuccess()){
            logger.error(fullHttpRequest.uri()+"  body:"+fullHttpRequest.content().toString(CharsetUtil.UTF_8)+"请求解码失败，判定请求是否正确");
            AnswerTask.sendBadUrlError(channelHandlerContext, HttpResponseStatus.OK);
            return;
        }
        //String id = channelHandlerContext.hashCode() + StringDictionary.EMPTY_STRING;
        //logger.info("id:" + id);
        //ByteBuf byteBuf = fullHttpRequest.content();
        //获取请求参数
        //String requestBody = byteBuf.toString(CharsetUtil.UTF_8);
        //logger.info("id:" + id + "请求内容" + requestBody);
        String uri = fullHttpRequest.uri();
        String host = fullHttpRequest.headers().get("Host");
        String clientAddr = channelHandlerContext.channel().remoteAddress().toString();
        channelHandlerContext.executor().execute(
        		new AnswerTask(uri, host, clientAddr, channelHandlerContext));
    }

  

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
