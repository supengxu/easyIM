package top.xxpblog.easyChat.api.service.user.impl;

import top.xxpblog.easyChat.api.dao.user.UserQqDao;
import top.xxpblog.easyChat.api.service.user.UserQqService;
import top.xxpblog.easyChat.common.entity.user.UserQq;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class UserQqServiceImpl implements UserQqService {
    
    @Resource
    private UserQqDao userQqDao;
    
    @Override
    public UserQq findByOpenId(String openId) {
        return userQqDao.findByOpenId(openId);
    }
    
    @Override
    public boolean insertUserQq(UserQq userQq) {
        userQq.setCreateTime(new Date());
        return userQqDao.insertUserQq(userQq);
    }
    
    @Override
    public boolean deleteByUid(Long uid) {
        return userQqDao.deleteByUid(uid);
    }
}
