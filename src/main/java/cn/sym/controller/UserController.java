package cn.sym.controller;

import cn.sym.dto.UserDTO;
import cn.sym.dto.UserExportQueryDTO;
import cn.sym.dto.UserLoginDTO;
import cn.sym.dto.UserQuery;
import cn.sym.dto.UserRegisterDTO;
import cn.sym.entity.UserDO;
import cn.sym.service.UserService;
import cn.sym.common.response.RestResult;
import cn.sym.common.constant.ResultCodeConstant;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 *
 * @author user
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     * @return RestResult结果
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public RestResult<Boolean> register(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        try {
            Boolean result = userService.register(userRegisterDTO);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
        } catch (Exception e) {
            log.error("用户注册异常", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "注册失败: " + e.getMessage(), false);
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     * @return RestResult结果，包含JWT Token
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public RestResult<String> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        try {
            String token = userService.login(userLoginDTO);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, token);
        } catch (Exception e) {
            log.error("用户登录异常", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "登录失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取用户信息
     *
     * @param userQuery 用户查询条件
     * @return RestResult结果
     */
    @PostMapping("/info")
    public RestResult<UserDO> getUserInfo(@RequestBody @Valid UserQuery userQuery) {
        try {
            UserDO userDO = userService.userInfo(userQuery);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, userDO);
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "获取用户信息失败: " + e.getMessage(), null);
        }
    }

    /**
     * 更新用户信息
     *
     * @param userDTO 用户信息
     * @return RestResult结果
     */
    @PutMapping("/update")
    @ApiOperation("更新用户信息")
    public RestResult<Boolean> updateUserInfo(@RequestBody @Valid UserDTO userDTO) {
        try {
            Boolean result = userService.updateUser(userDTO);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
        } catch (Exception e) {
            log.error("更新用户信息异常", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "更新用户信息失败: " + e.getMessage(), false);
        }
    }

    /**
     * 分页查询用户列表
     *
     * @param userQuery 查询条件
     * @return RestResult结果
     */
    @PostMapping("/list/page")
    @ApiOperation("分页查询用户列表")
    public RestResult<Page<UserDO>> listUsers(@RequestBody @Valid UserQuery userQuery) {
        try {
            Page<UserDO> userPage = userService.listUsers(userQuery);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, userPage);
        } catch (Exception e) {
            log.error("分页查询用户列表异常", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "查询用户列表失败: " + e.getMessage(), null);
        }
    }

    /**
     * 导出用户信息
     *
     * @param query    查询条件
     * @param response HTTP响应对象
     * @return RestResult结果
     */
    @PostMapping("/export")
    @ApiOperation("导出用户信息")
    public RestResult<Boolean> exportUsers(@RequestBody @Valid UserExportQueryDTO query,
                                           HttpServletResponse response) {
        try {
            userService.exportUsers(query, response);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
        } catch (Exception e) {
            log.error("导出用户信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "导出失败: " + e.getMessage(), false);
        }
    }

    /**
     * 导入用户信息
     *
     * @param userList 用户列表
     * @return RestResult结果
     */
    @PostMapping("/import")
    @ApiOperation("导入用户信息")
    public RestResult<Boolean> importUsers(@RequestBody List<UserDO> userList) {
        try {
            Boolean result = userService.importUsers(userList);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
        } catch (Exception e) {
            log.error("导入用户信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, "导入失败: " + e.getMessage(), false);
        }
    }
}