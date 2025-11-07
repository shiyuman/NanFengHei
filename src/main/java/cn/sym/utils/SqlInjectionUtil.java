package cn.sym.utils;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class SqlInjectionUtil {

    // 定义危险字符的正则表达式模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('|--)|(%27)|(%24)|(\\|)|(&&)|(\\|\\|)|(;)|(drop\\s)|" +
        "(create\\s)|(update\\s)|(delete\\s)|(insert\\s)|(select\\s)|" +
        "(union\\s)|(exec\\s)|(execute\\s)|(declare\\s)|(cast\\s)|" +
        "(chr\\s)|(mid\\s)|(master\\s)|(script\\s)|(javascript\\s)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 检查字符串是否包含SQL注入风险字符
     * @param str 待检查的字符串
     * @return 是否安全
     */
    public static boolean isSqlSafe(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        return !SQL_INJECTION_PATTERN.matcher(str).find();
    }

    /**
     * 过滤SQL注入风险字符
     * @param str 待过滤的字符串
     * @return 过滤后的字符串
     */
    public static String filterSqlInjection(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.replaceAll("('|--|%27|%24|\\||&&|\\|\\||;|drop\\s|create\\s|" +
                             "update\\s|delete\\s|insert\\s|select\\s|union\\s|" +
                             "exec\\s|execute\\s|declare\\s|cast\\s|chr\\s|mid\\s|" +
                             "master\\s|script\\s|javascript\\s)", "")
                 .trim();
    }
}

