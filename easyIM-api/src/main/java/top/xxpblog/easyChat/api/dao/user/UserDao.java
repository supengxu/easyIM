package top.xxpblog.easyChat.api.dao.user;

import top.xxpblog.easyChat.common.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserDao {

    /**
     * 根据Uid查询
     * @param uid
     * @return
     */
    User findByUid(Long uid);

    /**
     * 根据Uid查询密码
     * @param uid
     * @return
     */
    User findPwdByUid(Long uid);

    /**
     * 根据多个Uid查询
     * @param uids
     * @return
     */
     List<User> listByUidIn(List<Long> uids);

    /**
     * 插入
     * @param user
     * @return
     */
    Long insertUserAndReturnId(User user);

    /**
     * 更新
     * @param user
     * @return
     */
    boolean updateUser(User user);

    /**
     * 删除
     * @param uid
     * @return
     */
    boolean deleteByUid(Long uid);

}
