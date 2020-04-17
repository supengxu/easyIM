package top.xxpblog.easyChat.api.utils;

import top.xxpblog.easyChat.api.dto.UserLoginDTO;
import io.jsonwebtoken.Claims;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户登录工具类
 */
public class UserLoginUtils {

  /**
   * 创建 SID
   * @param uid
   * @return
   */
  public static String createSid(Long uid, Long ttl) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("uid", uid);
    return JwtUtils.createToken(claims, ttl); // 一天后过期
  }

  /**
   * 创建 SID 没有过期时间
   * @param uid
   * @return
   */
  public static String createSid(Long uid) {
    return createSid(uid, null);
  }

  /**
   * 通过token获取用户ID
   * @param token
   * @return
   */
  public static Boolean checkToken(Long uid, String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }
    try {
      // 验证 token
      Claims claims = JwtUtils.parse(token);
      if (claims == null) {
        return false;
      }
      Long jwtUid = Long.valueOf(claims.get("uid").toString());
      if (uid.compareTo(jwtUid) != 0) {
        return false;
      }

    } catch (Exception e) {
      return false;
    }
    return true;
  }

    public static UserLoginDTO getUserLoginDTO(HttpServletRequest request) {
        String sUid = request.getParameter("UID");

        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("UID")) {
                sUid = cookie.getValue();
            }
        }

        Long uid = Long.valueOf(sUid);

        UserLoginDTO userLoginDTO = new UserLoginDTO();

        userLoginDTO.setUid(uid);
        return userLoginDTO;
    }
  /**
   * 验证用户是否登录
   * @param request
   * @return
   */
  public static Boolean check(HttpServletRequest request) {

      String sUid = request.getParameter("UID");
      String token = request.getParameter("SID");
      if (sUid == null || sUid.isEmpty() || token == null || token.isEmpty()) {
          Cookie[] cookies = request.getCookies();
          if (cookies == null) {
              return false;
          }
          for (Cookie cookie : cookies) {
              if (cookie.getName().equals("UID")) {
                  sUid = cookie.getValue();
              } else if (cookie.getName().equals("SID")) {
                  token = cookie.getValue();
              }
          }
      }

      if (sUid == null || token == null) {
          return false;
      }

      long uid;
      try {
          uid = Long.parseLong(sUid);
      } catch (NumberFormatException e) {
          return false;
      }

      if (!checkToken(uid, token)) {
          return false;
      }

      return true;
  }

}
