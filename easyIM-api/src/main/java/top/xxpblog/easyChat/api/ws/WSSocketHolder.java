package top.xxpblog.easyChat.api.ws;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 操作 在线用户的 Channel
 */
public class WSSocketHolder {

    // 存储所有在线的用户
    private static final Map<Long, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    public static void put(Long id, Channel channel) {
        CHANNEL_MAP.putIfAbsent(id, channel);
    }

    public static Channel get(Long id) {
        return CHANNEL_MAP.get(id);
    }

    public static Map<Long, Channel> getMAP() {
        return CHANNEL_MAP;
    }

    public static void remove(Channel channel) {
        CHANNEL_MAP.entrySet().stream().filter(entry -> entry.getValue() == channel).forEach(entry -> CHANNEL_MAP.remove(entry.getKey()));
    }
    public static Integer Count(){
        return CHANNEL_MAP.entrySet().size();
    }

}
