package top.xxpblog.easyChat.api.utils;


import top.xxpblog.easyChat.api.config.PublicFileUrlConfig;

/**
 * 公共文件管理工具
 */
public class PublicFileUtils {

    public static String createUploadUrl(String filePath) {

        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        if (filePath.indexOf("http") == 0 || filePath.indexOf("/") == 0) {
            return filePath;
        }

        return PublicFileUrlConfig.getDomain() + "/" + filePath;
    }

}
