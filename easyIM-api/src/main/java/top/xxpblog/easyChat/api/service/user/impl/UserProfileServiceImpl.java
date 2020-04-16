package top.xxpblog.easyChat.api.service.user.impl;

import top.xxpblog.easyChat.api.dao.user.UserProfileDao;
import top.xxpblog.easyChat.api.service.user.UserProfileService;
import top.xxpblog.easyChat.common.entity.user.UserProfile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserProfileServiceImpl implements UserProfileService {
  
  @Resource
  private UserProfileDao userProfileDao;
  
  @Override
  public UserProfile findByUid(Long uid) {
    return userProfileDao.findByUid(uid);
  }
  
  @Override
  public List<UserProfile> listByUidIn(List<Long> uids) {
    return userProfileDao.listByUidIn(uids);
  }
  
  /**
   * 增加好友请求数量
   * @param count
   * @return
   */
  @Override
  public boolean incFriendAskCount(Long uid, Integer count) {
    UserProfile userProfile = new UserProfile();
    userProfile.setUid(uid);
    userProfile.setFriendAskCount(count);
    userProfile.setCreateTime(new Date());
    userProfile.setModifiedTime(new Date());
    return userProfileDao.insertUserProfile(userProfile);
  }
  
  @Override
  public boolean incFriendCountAll(List<UserProfile> userProfiles) {
    return userProfileDao.insertUserProfileAll(userProfiles);
  }
  
  /**
   * 清空好友请求数量
   * @return
   */
  @Override
  public boolean clearFriendAskCount(Long uid) {
    UserProfile userProfile = new UserProfile();
    userProfile.setUid(uid);
    userProfile.setFriendAskCount(0);
    userProfile.setModifiedTime(new Date());
    return userProfileDao.updateUserProfile(userProfile);
  }
}
