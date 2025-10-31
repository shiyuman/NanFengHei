package cn.sym.controller;

import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.common.exception.BusinessException;
import cn.sym.common.response.RestResult;
import cn.sym.dto.UserLoginDTO;
import cn.sym.dto.UserRegisterDTO;
import cn.sym.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.validation.groups.Default;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.sym.dto.UserDTO;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import cn.sym.entity.UserDO;
import cn.sym.dto.UserQuery;

/**
 * 用户控制器
 *
 * @author user
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(tags = "用户管理")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 结果
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public RestResult<Map<String,Object>> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        try {
            //注册，成功就会给用户分配ID
            boolean res = userService.register(registerDTO);
            if (res) {
                //这里创建的data映射是为了将用户ID包含在成功响应中返回给客户端。这样客户端就能立即知道新注册用户的ID，而不需要再发起额外的查询请求。
                Map<String, Object> data = new HashMap<>();
                data.put("userId", userService.findUserByUsername(registerDTO.getUsername()).getId());
                return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, data);
            } else {
                throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
            }
        } catch (BusinessException e) {
            log.error("注册失败: {}", e.getMessage(), e);
            return new RestResult<>(e.getCode(), e.getMsg());
        } catch (Exception e) {
            log.error("注册过程中发生未知错误: ", e);
            return new RestResult<>(ResultCodeConstant.CODE_000004, ResultCodeConstant.CODE_000004_MSG);
        }
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public RestResult<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            String token = userService.login(loginDTO);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, data);
        } catch (BusinessException e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return new RestResult<>(e.getCode(), e.getMsg());
        } catch (Exception e) {
            log.error("登录过程中发生未知错误: ", e);
            return new RestResult<>(ResultCodeConstant.CODE_000004, ResultCodeConstant.CODE_000004_MSG);
        }
    }

    /**
     * 新增用户
     *
     * @param userDTO 用户信息
     * @return RestResult 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增用户")
    public RestResult<Boolean> addUser(@RequestBody @Validated({ Default.class }) UserDTO userDTO) {
        Boolean result = userService.addUser(userDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return RestResult 结果
     */
    @DeleteMapping("/delete/{userId}")
    @ApiOperation("删除用户")
    public RestResult<Boolean> deleteUser(@PathVariable Long userId) {
        Boolean result = userService.deleteUser(userId);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 更新用户
     *
     * @param userDTO 用户信息
     * @return RestResult 结果
     */
    @PutMapping("/update")
    @ApiOperation("更新用户")
    public RestResult<Boolean> updateUser(@RequestBody @Validated({ Default.class }) UserDTO userDTO) {
        Boolean result = userService.updateUser(userDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 查询用户详情
     *
     * @param userQuery 查询参数
     * @return RestResult 结果
     */
    @GetMapping("/info")
    @ApiOperation("查询用户详情")
    public RestResult<UserDO> userInfo(@Validated({ Default.class }) UserQuery userQuery) {
        UserDO result = userService.userInfo(userQuery);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 分页查询用户列表
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param username 用户名关键词
     * @return RestResult 结果
     */
    @GetMapping("/list")
    @ApiOperation("分页查询用户列表")
    public RestResult<Page<UserDO>> listUsers(@RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String username) {
        Page<UserDO> result = userService.listUsers(pageNo, pageSize, username);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }
}