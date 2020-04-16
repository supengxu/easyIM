package top.xxpblog.easyChat.api.service.user;

import top.xxpblog.easyChat.common.entity.user.UserFriendMsg;

import java.util.List;

public interface UserFriendMsgService {

    List<UserFriendMsg> listByUidAndToUid(Long uid, Long toUid, Integer offset, Integer limit);

    boolean insertUserFriendMsg(UserFriendMsg userFriendMsg);

}
