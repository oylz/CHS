package com.mz.schudlerserver.netty.common;

import com.mz.schudlerserver.common.ConfigParams;
import com.mz.schudlerserver.util.BlowfishECB;
import com.sun.istack.internal.NotNull;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyResonse {
    private static final Logger logger = LoggerFactory.getLogger(NettyResonse.class);

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");

    /**
     * 获取返回状态为200的FullHttpResopnse
     * @param content
     * @return
     */
    public static FullHttpResponse getOKFullHttpResponse(@NotNull String content){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    /**
     * 设定随意状态的FullHttpResource
     * @param content
     * @param status
     * @return
     */
    public static FullHttpResponse getMadeFullHttpResponse(@NotNull String content, @NotNull HttpResponseStatus status){
        String result = BlowfishECB.getInstance(ConfigParams.blowfishECBKey).encrypt(content);
        logger.info("response result----clear:[" + content + "],--cipher:[" + result + "]");
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }
}
