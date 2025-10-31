package cn.sym.repository;

import cn.sym.entity.UserInfo;
import org.springframework.stereotype.Repository;

/**
 * 用户仓库接口
 *
 * @author user
 */
@Repository
public interface UserRepository {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return UserInfo
     */
    UserInfo findByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 保存用户信息
     *
     * @param userInfo 用户信息
     * @return 是否保存成功
     */
    boolean save(UserInfo userInfo);
}
