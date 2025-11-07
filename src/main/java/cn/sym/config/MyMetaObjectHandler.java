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
}
