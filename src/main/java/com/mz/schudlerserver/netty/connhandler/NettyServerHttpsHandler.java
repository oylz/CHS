package com.mz.schudlerserver.netty.connhandler;

import java.io.File;
import java.io.FileInputStream;

import com.mz.schudlerserver.util.NettyConfig;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;



public class NettyServerHttpsHandler extends ChannelInitializer<SocketChannel> {
	
    private static String ROOT_CRT_PATH = NettyConfig.getPropValue("SSL_ROOT_CRT_PATH", "/etc/ssl/miclink.8686c.com/miclink.8686c.com.crt");
    private static String DOMAIN_CRT_PATH = NettyConfig.getPropValue("SSL_DOMAIN_CRT_PATH", "/etc/ssl/miclink.8686c.com/miclink.8686c.com.crt");
    private static String KEY_PATH = NettyConfig.getPropValue("SSL_KEY_PATH", "/etc/ssl/miclink.8686c.com/miclink.8686c.com.key");
	
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline channelPipeline = socketChannel.pipeline();
        SslContext sslCtx =  SslContextBuilder.forServer(new FileInputStream(new File(DOMAIN_CRT_PATH)), 
        		new FileInputStream(new File(KEY_PATH)), null).trustManager(new File(ROOT_CRT_PATH))
        		.sslProvider(SslProvider.OPENSSL).clientAuth(ClientAuth.NONE).build();
        channelPipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        channelPipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
        //// 聚合器，把多个消息转换为一个单一的FullHttpRequest或是FullHttpRespon
        channelPipeline.addLast("httpAggergator", new HttpObjectAggregator(65536));
        channelPipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
        channelPipeline.addLast("holderConnHandler", new HolderConnHandler());
    }
}
