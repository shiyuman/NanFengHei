package cn.sym.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    //重写插入填充方法。当执行数据库插入操作时，会自动调用这个方法来填充字段。
    @Override
    public void insertFill(MetaObject metaObject) {
        //TODO 将system改为当前用户
        //参数分别是：要操作的对象，要填充的字段名，字段的数据类型，具体的值
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());

        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());

        this.strictInsertFill(metaObject, "createBy", String.class, "system");

        this.strictInsertFill(metaObject, "updateBy", String.class, "system");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());

        this.strictUpdateFill(metaObject, "updateBy", String.class, "system");

    }

    /**
     * 获取当前用户 - 多种安全框架支持
     */
//    private String getCurrentUsername() {
//        // 优先级1: Spring Security
//        try {
//            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            if (principal instanceof UserDetails) {
//                return ((UserDetails) principal).getUsername();
//            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
//                return (String) principal;
//            }
//        } catch (Exception e) {
//            log.trace("Spring Security上下文获取失败");
//        }
//
//        // 优先级2: 从请求头或ThreadLocal中获取
//        try {
//            // 如果你有自定义的用户上下文
//            // return UserContextHolder.getUserName();
//        } catch (Exception e) {
//            log.trace("自定义用户上下文获取失败");
//        }
//
//        // 优先级3: 从JWT Token中解析
//        try {
//            // 如果你使用JWT，可以从请求头中解析
//            // return JwtUtil.getUsernameFromToken();
//        } catch (Exception e) {
//            log.trace("JWT解析失败");
//        }
//
//        // 默认值
//        return "unknown";
//    }
}