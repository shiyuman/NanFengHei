package cn.sym.service.impl;

import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.common.exception.BusinessException;
import cn.sym.dto.UserLoginDTO;
import cn.sym.dto.UserRegisterDTO;
import cn.sym.service.UserService;
import cn.sym.utils.JwtUtil;
import cn.sym.utils.PasswordEncoderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.io.IOException;
import cn.sym.dto.UserExportQueryDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.springframework.util.CollectionUtils;
import cn.sym.entity.UserDO;
import cn.sym.repository.UserMapper;
import org.springframework.transaction.annotation.Transactional;
import cn.sym.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import cn.sym.dto.UserQuery;
import cn.sym.common.response.RestResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 用户服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final PasswordEncoderUtil passwordEncoderUtil;

    private final UserMapper userMapper;

    private final JwtUtil jwtUtil;

    @Override
    public boolean register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserDO> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(UserDO::getUsername, registerDTO.getUsername());
        UserDO existingUser = userMapper.selectOne(userQueryWrapper);
        if (existingUser != null) {
            return false;
        }
        // 加密密码
        String encodedPassword = passwordEncoderUtil.encode(registerDTO.getPassword());
        // 构建用户信息对象
        UserDO userDO = new UserDO();
        userDO.setUsername(registerDTO.getUsername());
        userDO.setPassword(encodedPassword);
        userDO.setPhone(registerDTO.getPhone());
        // 默认启用状态
        userDO.setStatus(1);
        // 保存到数据库
        try {
            boolean result = this.save(userDO);
            if (result) {
                log.info("用户注册成功，用户名: {}", userDO.getUsername());
                return true;
            } else {
                log.error("用户注册失败，用户名: {}", userDO.getUsername());
                return false;
            }
        } catch (Exception e) {
            log.error("用户注册失败，用户名: {}", userDO.getUsername(), e);
            return false;
        }
    }

    @Override
    public String login(UserLoginDTO loginDTO) throws BusinessException {
        // 查找用户
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, loginDTO.getUsername());
        UserDO userDO = userMapper.selectOne(wrapper);
        if (userDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000002, ResultCodeConstant.CODE_000002_MSG);
        }
        // 验证密码
        if (!passwordEncoderUtil.matches(loginDTO.getPassword(), userDO.getPassword())) {
            throw new BusinessException(ResultCodeConstant.CODE_000003, ResultCodeConstant.CODE_000003_MSG);
        }
        // 生成JWT Token
        String token = jwtUtil.generateToken(userDO.getUsername());
        log.info("用户登录成功，用户名: {}", userDO.getUsername());
        return token;
    }

    @Override
    public UserDO findUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void exportUsers(UserExportQueryDTO query, HttpServletResponse response) throws IOException {
        // 构建查询条件
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            wrapper.like(UserDO::getUsername, query.getUsername());
        }
        if (query.getPhone() != null && !query.getPhone().isEmpty()) {
            wrapper.like(UserDO::getPhone, query.getPhone());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(UserDO::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(UserDO::getCreateTime, query.getEndTime());
        }
        // 获取所有匹配的用户
        List<UserDO> userList = userMapper.selectList(wrapper);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("用户数据");
        // 设置列标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("用户ID");
        headerRow.createCell(1).setCellValue("用户名");
        headerRow.createCell(2).setCellValue("手机号码");
        headerRow.createCell(3).setCellValue("账户状态");
        headerRow.createCell(4).setCellValue("创建时间");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 填充数据
        int rowNum = 1;
        for (UserDO user : userList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getPhone());
            row.createCell(3).setCellValue(user.getStatus() == 1 ? "正常" : "禁用");
            row.createCell(4).setCellValue(sdf.format(user.getCreateTime()));
        }
        // 自动调整列宽
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
        // 写入响应流
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String("用户数据.xlsx".getBytes("utf-8"), "iso-8859-1"));
        workbook.write(response.getOutputStream());
        try {
            workbook.close();
        } catch (IOException e) {
            log.error("关闭Workbook失败", e);
        }
    }

    @Transactional
    @Override
    public Boolean importUsers(List<UserDO> users) {
        if (CollectionUtils.isEmpty(users)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "导入的数据为空");
        }
        // 进行数据校验和插入操作
        boolean success = true;
        try {
            for (UserDO user : users) {
                // 校验必要字段
                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                    throw new BusinessException(ResultCodeConstant.CODE_000001, "用户名不能为空");
                }
                // 可以在这里添加更多校验逻辑
                // 插入数据库
                userMapper.insert(user);
            }
        } catch (Exception e) {
            log.error("批量导入用户数据失败", e);
            success = false;
        }
        return success;
    }

    @Override
    public Boolean addUser(UserDTO userDTO) {
        // 校验用户名是否已存在
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, userDTO.getUsername());
        UserDO existingUser = userMapper.selectOne(wrapper);
        if (existingUser != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 插入新用户
        UserDO userDO = new UserDO();
        userDO.setUsername(userDTO.getUsername());
        userDO.setPassword(userDTO.getPassword());
        userDO.setPhone(userDTO.getPhone());
        userDO.setStatus(userDTO.getStatus());
        return this.save(userDO);
    }

    @Override
    public Boolean deleteUser(Long userId) {
        // 检查用户是否存在
        UserDO userDO = this.getById(userId);
        if (userDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 删除用户
        return this.removeById(userId);
    }

    @Override
    public Boolean updateUser(UserDTO userDTO) {
        // 验证用户是否存在
        UserDO userDO = this.getById(userDTO.getUserId());
        if (userDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 更新用户信息
        userDO.setUsername(userDTO.getUsername());
        userDO.setPassword(userDTO.getPassword());
        userDO.setPhone(userDTO.getPhone());
        userDO.setStatus(userDTO.getStatus());
        return this.updateById(userDO);
    }

    @Override
    public UserDO userInfo(UserQuery userQuery) {
        UserDO userDO = this.getById(userQuery.getUserId());
        if (userDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        return userDO;
    }

    @Override
    public Page<UserDO> listUsers(int page, int size, String username) {
        Page<UserDO> userPage = new Page<>(page, size);
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(UserDO::getUsername, username);
        }
        userMapper.selectPage(userPage, wrapper);
        return userPage;
    }
}
