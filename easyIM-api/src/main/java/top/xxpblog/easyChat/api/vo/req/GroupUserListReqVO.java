package top.xxpblog.easyChat.api.vo.req;

import top.xxpblog.easyChat.common.vo.req.BaseLimitReqVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询群里的用户列表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupUserListReqVO extends BaseLimitReqVO {

    /**
     * 群ID
     */
    private Long groupId;

}
