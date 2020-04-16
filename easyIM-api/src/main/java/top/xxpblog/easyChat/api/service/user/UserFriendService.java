package top.xxpblog.easyChat.api.service.user;

import top.xxpblog.easyChat.common.entity.user.UserFriend;

import java.util.List;

public interface UserFriendService {

    List<UserFriend> listByUid(Long uid, Integer page, Integer limit);

    UserFriend findByUidAndFriendUid(Long uid, Long friendUid);

    boolean insertUserFriendAll(List<UserFriend> userFriends);

    boolean updateUserFriend(UserFriend userFriend);

    boolean deleteByUidAndFriendUid(Long uid, Long friendUid);

}
