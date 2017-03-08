package com.mz.schudlerserver.netty.connhandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;



public class NettyServerChildHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline channelPipeline = socketChannel.pipeline();
        channelPipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
        //// 聚合器，把多个消息转换为一个单一的FullHttpRequest或是FullHttpRespon
        channelPipeline.addLast("httpAggergator", new HttpObjectAggregator(65536));
        channelPipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
        channelPipeline.addLast("holderConnHandler", new HolderConnHandler());
    }
}
