package top.xxpblog.easyChat.api.vo.req;

import top.xxpblog.easyChat.common.vo.req.BaseLimitReqVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取用户的群列表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserGroupListReqVO extends BaseLimitReqVO {

    /**
     * 用户的ID
     */
    private Long uid;

}
