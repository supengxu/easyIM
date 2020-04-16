package top.xxpblog.easyChat.api.service.group;

import top.xxpblog.easyChat.common.entity.group.GroupMsg;

import java.util.Date;
import java.util.List;

public interface GroupMsgService {

    List<GroupMsg> listByGroupIdAndCreateTime(Long groupId, Date createTime, Integer page, Integer limit);

    boolean insertGroupMsg(GroupMsg groupMsg);

    boolean sendGroupMsg(Long uid, Long groupId, Integer type, Integer msgType, String msgContent);

}
