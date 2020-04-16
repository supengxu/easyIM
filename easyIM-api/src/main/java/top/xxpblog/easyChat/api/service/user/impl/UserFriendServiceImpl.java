package top.xxpblog.easyChat.api.service.user.impl;

import top.xxpblog.easyChat.api.dao.user.UserFriendDao;
import top.xxpblog.easyChat.api.service.user.UserFriendService;
import top.xxpblog.easyChat.api.utils.PageUtils;
import top.xxpblog.easyChat.common.entity.user.UserFriend;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserFriendServiceImpl implements UserFriendService {

    @Resource
    private UserFriendDao userFriendDao;

    @Override
    public List<UserFriend> listByUid(Long uid, Integer page, Integer limit) {
        Integer offset = PageUtils.createOffset(page, limit);
        return userFriendDao.listByUid(uid, offset, limit);
    }

    @Override
    public UserFriend findByUidAndFriendUid(Long uid, Long friendUid) {
        return userFriendDao.findByUidAndFriendUid(uid, friendUid);
    }

    @Override
    public boolean insertUserFriendAll(List<UserFriend> userFriends) {
        return userFriendDao.insertUserFriendAll(userFriends);
    }

    @Override
    public boolean updateUserFriend(UserFriend userFriend) {
        userFriend.setModifiedTime(new Date());
        return userFriendDao.updateUserFriend(userFriend);
    }

    @Override
    public boolean deleteByUidAndFriendUid(Long uid, Long friendUid) {
        return userFriendDao.deleteByUidAndFriendUid(uid, friendUid);
    }
}
