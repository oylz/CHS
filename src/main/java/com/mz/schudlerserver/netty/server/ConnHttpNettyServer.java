package com.mz.schudlerserver.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.schudlerserver.netty.connhandler.NettyServerChildHandler;
import com.mz.schudlerserver.util.NettyConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

import java.util.concurrent.Executor;


public class ConnHttpNettyServer implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(ConnHttpNettyServer.class);

    private static EpollEventLoopGroup bossGroup = null;
    private static EpollEventLoopGroup workerGroup = null;
	static public EventLoopGroup getBoss(){
		return bossGroup;
	}
	static public EventLoopGroup getWorker(){
		return workerGroup;
	}
    
    @Override
    public void run() {
        openHttpNettyServer();
    }

    /**
     * 设定netty
     */
    private void openHttpNettyServer(){
        int bossGroupNum = Integer.parseInt(NettyConfig.getPropValue("BOSS_GROOP_THREAD_NUM", "2"));
        int workerGroupNum = Integer.parseInt(NettyConfig.getPropValue("WORKER_GROOP_THREAD_NUM", "45"));
        int soBackLog = Integer.parseInt(NettyConfig.getPropValue("SO_BACKLOG", "1024"));
        int soSndBuf = Integer.parseInt(NettyConfig.getPropValue("SO_SNDBUF", "32768"));
        int soRcvBuf = Integer.parseInt(NettyConfig.getPropValue("SO_RCVBUF", "32768"));
        int nettyServerPort = Integer.parseInt(NettyConfig.getPropValue("NETTY_SERVER_PORT", "7481"));
        String nettyServerIp = NettyConfig.getPropValue("NETTY_SERVER_IP", null);
        Executor boss = new ThreadPerTaskExecutor(
        		new DefaultThreadFactory("boss", false, Thread.MAX_PRIORITY));
        bossGroup = new EpollEventLoopGroup(bossGroupNum, boss);
        Executor work = new ThreadPerTaskExecutor(
        		new DefaultThreadFactory("work", false, Thread.MAX_PRIORITY));        
        workerGroup = new EpollEventLoopGroup(workerGroupNum, work);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // socket地址重用
            serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
            serverBootstrap.option(ChannelOption.ALLOW_HALF_CLOSURE, true);
            serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
            // 未完成连接队列大小
            serverBootstrap.option(ChannelOption.SO_BACKLOG, soBackLog);
            // 发送、接收缓存大小
            serverBootstrap.option(ChannelOption.SO_SNDBUF, soSndBuf);
            serverBootstrap.option(ChannelOption.SO_RCVBUF, soRcvBuf);
            /**
             * 内存池，则当A链路接收到新的数据报之后，从NioEventLoop的内存池中申请空闲的ByteBuf，
             * 解码完成之后，调用release将ByteBuf释放到内存池中，供后续B链路继续使用。使用Netty4
             * 的PooledByteBufAllocator进行GC优化,使用内存池之后，内存的申请和释放必须成对出现，
             * 即retain()和release()要成对出现，否则会导致内存泄露。
             */
            serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            serverBootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class).
                    handler(new LoggingHandler(LogLevel.INFO)).childHandler(new NettyServerChildHandler());
            Channel ch = serverBootstrap.bind(nettyServerIp, nettyServerPort).sync().channel();
            logger.info("开启netty服务器，开始绑定ip：" + nettyServerIp + ";port:" + nettyServerPort);
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("运行netty服务器时出现网络异常，程序即将退出" + e.getMessage(), e);
            System.exit(-1);
        } catch (Exception e) {
            logger.error("运行netty服务器时未知异常，程序即将退出" + e.getMessage(), e);
            System.exit(-1);
        }
    }
}
