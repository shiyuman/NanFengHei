package cn.sym.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS工具类
 *
 * @author user
 */
@Slf4j
@Component
public class OssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到OSS
     *
     * @param content 文件内容
     * @param filename 文件名称
     * @param contentType MIME类型
     * @return 存储路径Key
     */
    public String upload(byte[] content, String filename, String contentType) {
        try {
            // 构建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 设置随机的object name防止重名
            String objectName = UUID.randomUUID().toString().replaceAll("-", "") + "_" + filename;

            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName,
                    new ByteArrayInputStream(content));

            // 设置Content-Type
            putObjectRequest.putCustomHeader("Content-Type", contentType);

            // 执行上传操作
            ossClient.putObject(putObjectRequest);

            // 关闭client
            ossClient.shutdown();

            return objectName;
        } catch (Exception e) {
            log.error("上传文件到OSS异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从OSS删除指定key的文件
     *
     * @param key 文件在OSS中的存储路径Key
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        try {
            // 构建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 删除文件
            ossClient.deleteObject(bucketName, key);

            // 关闭client
            ossClient.shutdown();

            return true;
        } catch (Exception e) {
            log.error("从OSS删除文件异常: {}", e.getMessage(), e);
            return false;
        }
    }
}