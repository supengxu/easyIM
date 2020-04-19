package top.xxpblog.easyChat.api.utils;

import java.util.Map;

import okhttp3.*;

/**
 * 请求工具类
 **/
public class OkHttpUtil {

    /**
     * 根据map获取get请求参数
     *
     * @param queries
     * @return
     */
    public static StringBuffer getQueryString(String url, Map<String, String> queries) {
        StringBuffer sb = new StringBuffer(url);
        if (queries != null && queries.keySet().size() > 0) {
            boolean firstFlag = true;
            for (Object o : queries.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                if (firstFlag) {
                    sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }
        return sb;
    }

    /**
     * 调用okhttp的newCall方法
     *
     * @param request
     * @return
     */
    private static String execNewCall(Request request) {
        OkHttpClient okHttpClient = SpringBeanFactoryUtils.getBean(OkHttpClient.class);

        ResponseBody body = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                return "";
            }
            body = response.body();
            String json = "";
            if (body != null) {
                json = body.string();
            }
            return json;
        } catch (Exception exception) {
            return "";
        } finally {
            if (body != null) {
                body.close();
            }
        }

    }

    /**
     * get
     *
     * @param url     请求的url
     * @param queries 请求的参数，在浏览器？后面的数据，没有可以传null
     * @return
     */
    public static String get(String url, Map<String, String> queries) {
        StringBuffer sb = getQueryString(url, queries);
        Request request = new Request.Builder()
                .url(sb.toString())
                .build();
        return execNewCall(request);
    }


}
