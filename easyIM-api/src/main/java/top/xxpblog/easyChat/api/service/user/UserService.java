package top.xxpblog.easyChat.api.service.user;

import top.xxpblog.easyChat.api.vo.res.UserInfoListResVO;
import top.xxpblog.easyChat.common.entity.user.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    User findByUid(Long uid);

    User findPwdByUid(Long uid);
    
    Map<Long, UserInfoListResVO> listByUidIn(List<Long> uids);

    boolean insertUser(User user);

    boolean updateUser(User user);

    boolean deleteByUid(Long uid);

}
