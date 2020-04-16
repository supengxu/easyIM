package top.xxpblog.easyChat.api.service.other;

import top.xxpblog.easyChat.api.vo.res.QqOpenIdResVO;
import top.xxpblog.easyChat.api.vo.res.QqUserInfoResVO;

public interface QqWebAuthService {
    
    /**
     * 获取 access_token
     *
     * @return
     */
    public String getAccessToken(String code, String redirect_uri) ;
    
    /**
     * 获取 openID
     *
     * @return
     */
    public QqOpenIdResVO getOpenID(String accessToken) ;
    
    /**
     * 获取用户信息
     * @return
     */
    public QqUserInfoResVO getUserInfo(String code, String redirect_uri);
}
