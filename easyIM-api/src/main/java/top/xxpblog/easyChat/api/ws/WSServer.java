package top.xxpblog.easyChat.api.ws;

import top.xxpblog.easyChat.api.vo.req.WSMessageReqVO;
import top.xxpblog.easyChat.api.vo.req.WSBaseReqVO;
import top.xxpblog.easyChat.api.vo.req.WSUserReqVO;
import top.xxpblog.easyChat.common.protobuf.WSBaseResProtoOuterClass;
import top.xxpblog.easyChat.common.protobuf.WSMessageResProtoOuterClass;
import top.xxpblog.easyChat.common.protobuf.WSUserResProtoOuterClass;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.Date;

@Component
@Slf4j
public class WSServer {

    @Value("${ws.port}")
    private int wsPort;

    private final EventLoopGroup boss = new NioEventLoopGroup();
    private final EventLoopGroup work = new NioEventLoopGroup();

    /**
     * 启动 ws server
     *
     * @return
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(wsPort))
                //保持长连接
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new WSServerInitializer());

        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            log.info("启动 ws server 成功");
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        boss.shutdownGracefully().syncUninterruptibly();
        work.shutdownGracefully().syncUninterruptibly();
        log.info("关闭 ws server 成功");
    }

    /**
     * 发送 Google Protocol 编码消息
     * @param fromUid 发送给谁
     * @param wsBaseReqVO 消息
     * @return
     */
    public Boolean sendMsg(Long fromUid, WSBaseReqVO wsBaseReqVO) {
        Channel channel = WSSocketHolder.get(fromUid);

        if (null == channel) {
            log.info("用户ID[" + fromUid + "]不在线！");
            //TODO 消息离线存储
            return false;
        }
        WSMessageReqVO wsMessageReqVO = wsBaseReqVO.getMessage();
        WSMessageResProtoOuterClass.WSMessageResProto wsMessageResProto = WSMessageResProtoOuterClass.WSMessageResProto.newBuilder()
                .setReceiveId(wsMessageReqVO.getReceiveId())
                .setMsgType(wsMessageReqVO.getMsgType())
                .setMsgContent(wsMessageReqVO.getMsgContent())
                .build();
    
        WSUserReqVO wsUserReqVO = wsBaseReqVO.getUser();
        WSUserResProtoOuterClass.WSUserResProto wsUserResProto = WSUserResProtoOuterClass.WSUserResProto.newBuilder()
                .setUid(wsUserReqVO.getUid())
                .setName(wsUserReqVO.getName())
                .setAvatar(wsUserReqVO.getAvatar())
                .setRemark(wsUserReqVO.getRemark())
                .build();
    
        WSBaseResProtoOuterClass.WSBaseResProto wsBaseResProto = WSBaseResProtoOuterClass.WSBaseResProto.newBuilder()
                .setType(wsBaseReqVO.getType())
                .setMessage(wsMessageResProto)
                .setUser(wsUserResProto)
                .setCreateTime(new Date().toString())
                .build();

        channel.writeAndFlush(wsBaseResProto);
        return true;
    }

}
