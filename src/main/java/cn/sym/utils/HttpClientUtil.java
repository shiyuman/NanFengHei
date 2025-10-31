package cn.sym.utils;

import cn.sym.utils.JSONUtil;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * <p>
 *   HTTP客户端工具类
 * </p>
 * @author user
 */
@Slf4j
public class HttpClientUtil {

    /**
     * GET请求方法
     *
     * @param url 目标URL
     * @return 响应字符串
     */
    public static String get(String url) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.name());
        } finally {
            response.close();
            httpClient.close();
        }
    }

    /**
     * POST请求方法
     *
     * @param url 目标URL
     * @param params 请求参数
     * @return 响应字符串
     */
    public static String post(String url, Map<String, Object> params) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        // 设置请求体内容为JSON格式
        String jsonBody = JSONUtil.toJSONString(params);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.name());
        } finally {
            response.close();
            httpClient.close();
        }
    }
}