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
        //从要插入的对象中获取名为"createTime"的字段值
        Object createTime = getFieldValByName("createTime", metaObject);
        if (createTime == null) {
            //参数分别是：要操作的对象，要填充的字段名，字段的数据类型，具体的值
            this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        }

       Object updateTime = getFieldValByName("updateTime", metaObject);
        if(updateTime == null){
            this.strictInsertFill(metaObject,"updateTime",Date.class,new Date());
        }

        Object createBy = getFieldValByName("createBy", metaObject);
        if (createBy == null) {
            this.strictInsertFill(metaObject, "createBy", String.class, "system");
        }

        Object updateBy = getFieldValByName("updateBy", metaObject);
        if (updateBy == null) {
            this.strictInsertFill(metaObject, "updateBy", String.class, "system");
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object updateTime = getFieldValByName("updateTime", metaObject);
        if (updateTime == null) {
            this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
        }

        Object updateBy = getFieldValByName("updateBy", metaObject);
        if (updateBy == null) {
            this.strictUpdateFill(metaObject, "updateBy", String.class, "system");
        }
    }
}
