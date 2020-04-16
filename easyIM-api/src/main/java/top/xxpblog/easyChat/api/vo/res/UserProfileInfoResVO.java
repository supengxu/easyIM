package top.xxpblog.easyChat.api.vo.res;

import lombok.Data;

/**
 * 用户额外信息
 */
@Data
public class UserProfileInfoResVO {
    
    /**
     * 好友请求的数量
     */
    private Integer friendAskCount;
    /**
     * 好友数量
     */
    private Integer friendCount;

}
