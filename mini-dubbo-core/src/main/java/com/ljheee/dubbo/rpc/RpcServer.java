package com.ljheee.dubbo.rpc;

import com.ljheee.dubbo.annotation.RpcService;
import com.ljheee.dubbo.registry.ServiceRegistryCenter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务提供者端
 * 注册服务地址  绑定服务
 */
public class RpcServer {

    private ServiceRegistryCenter registryCenter;

    private String serviceAddress;

    // key服务名称   value服务对象
    Map<String, Object> handlerMap = new HashMap<>();

    public RpcServer(ServiceRegistryCenter registryCenter, String serviceAddress) {
        this.registryCenter = registryCenter;
        this.serviceAddress = serviceAddress;
    }


    /**
     * 服务名称---服务对象 IGpService-----实现类GpServiceImpl
     *
     * @param services
     */
    public void bind(Object... services) {
        //遍历 传入多少个服务对象      @Service  Autowired
        for (Object service : services) {
            //service.getClass---->com.ljheee.server.HelloServiceImpl
            RpcService annotation = service.getClass().getAnnotation(RpcService.class);
            String serviceName = annotation.value().getName();// TODO 注解处理
//            service.getClass().getInterfaces()[0].getName();

            //key(serviceName) : com.ljheee.api.IGpService
            //value(service): com.ljheee.impl.GpServiceImpl
            handlerMap.put(serviceName, service);
        }
    }


    /**
     * 此方法会 一直执行
     * 监听端口，等待客户端连接
     */
    public void registerAndListen() {

        // 注册服务
        for (String serviceName : handlerMap.keySet()) {
            registryCenter.register(serviceName, serviceAddress);
        }


        try {
            // 监听端口，与客户端通信
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);

            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {

                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
                    pipeline.addLast(new LengthFieldPrepender(4));
                    //发送和接收的 `object`通过`ObjectDecoder` `ObjectEncoder`进行加解密
                    pipeline.addLast("encode", new ObjectEncoder());
                    pipeline.addLast("decode", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                    pipeline.addLast(new RpcServerHandler(handlerMap));
                }
            }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] strings = serviceAddress.split(":");
            String ip = strings[0];
            int port = Integer.parseInt(strings[1]);
            ChannelFuture channelFuture = bootstrap.bind(ip, port).sync();

            System.out.println("netty启动成功，等待客户端连接");
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static class RpcServerHandler extends ChannelInboundHandlerAdapter {
        Map<String, Object> handlerMap;

        public RpcServerHandler(Map<String, Object> handlerMap) {
            this.handlerMap = handlerMap;
        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //msg 得到客户端传来的数据
            //ctx 向客户端写数据
            RpcRequest request = (RpcRequest) msg;
            System.out.println(request);

            Object result = new Object();

            // 下面就要根据这个request进行调用server的对应类的方法
            if (handlerMap.containsKey(request.getClassName())) {
                // 执行服务端对应的对象
                Object clazz = handlerMap.get(request.getClassName());
                Method method = clazz.getClass().getMethod(request.getMethodName(), request.getTypes());
                result = method.invoke(clazz, request.getParams());
            }

            // 执行结果回写 给客户端[调用者]
            ctx.write(result);
            ctx.flush();
            ctx.close();
        }

    }
}