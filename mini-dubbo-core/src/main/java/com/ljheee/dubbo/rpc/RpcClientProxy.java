package com.ljheee.dubbo.rpc;

import com.ljheee.dubbo.registry.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 客户端调用RPC是透明的 就像调用本地方法一样，
 * 如何能实现呢？——用动态代理。
 */
public class RpcClientProxy {

    private ServiceDiscovery serviceDiscover;

    public RpcClientProxy(ServiceDiscovery serviceDiscover) {
        this.serviceDiscover = serviceDiscover;
    }


    public <T> T create(final Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                // 封装RpcRequest 对象
                RpcRequest request = new RpcRequest();
                request.setClassName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setTypes(method.getParameterTypes());
                request.setParams(args);

                //服务发现，因为接下来需要进行通信了
                String serviceName = interfaceClass.getName();
                String serviceAddress = serviceDiscover.discovery(serviceName);

                String[] arrs = serviceAddress.split(":");
                String host = arrs[0];
                int port = Integer.parseInt(arrs[1]);

                //Socket Netty连接
                final RpcProxyHandler rpcProxyHandler = new RpcProxyHandler();

                //通过netty的方式进行连接和发送
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap();
                    b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel channel) throws Exception {
                                    ChannelPipeline pipeline = channel.pipeline();
                                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                    pipeline.addLast("encoder", new ObjectEncoder());
                                    pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                    pipeline.addLast("handler", rpcProxyHandler);
                                }
                            });

                    // 连接服务器
                    ChannelFuture future = b.connect(host, port).sync();
                    //将封装好的request对象写过去
                    future.channel().writeAndFlush(request);
                    future.channel().closeFuture().sync();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    group.shutdownGracefully();
                }

                return rpcProxyHandler.getResponse();
            }
        });
    }


    public class RpcProxyHandler extends ChannelInboundHandlerAdapter {
        private Object response;

        public Object getResponse() {
            return response;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //msg: 服务器端写回的内容
            response = msg;
        }
    }


}
