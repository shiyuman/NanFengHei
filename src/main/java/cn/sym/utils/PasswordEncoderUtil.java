package cn.sym.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码编码工具类
 * 
 * @author user
 */
@Component
public class PasswordEncoderUtil {

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 对密码进行加密
     * 
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 匹配密码是否正确
     * 
     * @param rawPassword 明文密码
     * @param encodedPassword 已加密的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}