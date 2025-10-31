package cn.sym.service;

import cn.sym.common.exception.BusinessException;
import cn.sym.dto.UserLoginDTO;
import cn.sym.dto.UserRegisterDTO;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import cn.sym.entity.UserDO;
import java.io.IOException;
import cn.sym.dto.UserExportQueryDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.sym.dto.UserDTO;
import cn.sym.dto.UserQuery;

/**
 * 用户服务接口
 *
 * @author user
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 是否注册成功
     * @throws BusinessException 异常
     */
    boolean register(UserRegisterDTO registerDTO) throws BusinessException;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return JWT Token
     * @throws BusinessException 异常
     */
    String login(UserLoginDTO loginDTO) throws BusinessException;

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserDO findUserByUsername(String username);

    /**
     * 导出用户信息
     *
     * @param query 查询条件
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    void exportUsers(UserExportQueryDTO query, HttpServletResponse response) throws IOException;

    /**
     * 导入用户信息
     *
     * @param users Excel文件
     * @return 是否成功
     */
    Boolean importUsers(List<UserDO> users);

    /**
     * 添加用户
     *
     * @param userDTO 用户信息
     * @return 是否成功
     */
    Boolean addUser(UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUser(Long userId);

    /**
     * 更新用户信息
     *
     * @param userDTO 用户信息
     * @return 是否成功
     */
    Boolean updateUser(UserDTO userDTO);

    /**
     * 查询用户详情
     *
     * @param userQuery 查询参数
     * @return 用户信息
     */
    UserDO userInfo(UserQuery userQuery);

    /**
     * 分页查询用户列表
     *
     * @param page 当前页数
     * @param size 每页条数
     * @param username 用户名关键字
     * @return 用户分页结果
     */
    Page<UserDO> listUsers(int page, int size, String username);
}
