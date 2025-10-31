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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.util.Date;
import lombok.RequiredArgsConstructor;
import cn.sym.dto.UserQuery;

/**
 * 用户服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoderUtil passwordEncoderUtil;

    private final UserMapper userMapper;

    private final JwtUtil jwtUtil;

    @Override
    public boolean register(UserRegisterDTO registerDTO) throws BusinessException {
        // 检查用户名是否已经存在
        UserDO existingUser = userMapper.selectOne(new QueryWrapper<UserDO>().eq("username", registerDTO.getUsername()));
        if (existingUser != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
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
        userDO.setCreateBy("system");
        userDO.setCreateTime(new Date());
        userDO.setUpdateBy("system");
        userDO.setUpdateTime(new Date());
        // 保存到数据库
        try {
            int result = userMapper.insert(userDO);
            if (result > 0) {
                log.info("用户注册成功，用户名: {}", userDO.getUsername());
                return true;
            } else {
                log.error("用户注册失败，用户名: {}", userDO.getUsername());
                throw new BusinessException(ResultCodeConstant.CODE_000004, ResultCodeConstant.CODE_000004_MSG);
            }
        } catch (Exception e) {
            log.error("用户注册失败，用户名: {}", userDO.getUsername(), e);
            throw new BusinessException(ResultCodeConstant.CODE_000004, ResultCodeConstant.CODE_000004_MSG);
        }
    }

    @Override
    public String login(UserLoginDTO loginDTO) throws BusinessException {
        // 查找用户
        UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().eq("username", loginDTO.getUsername()));
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
        return userMapper.selectOne(new QueryWrapper<UserDO>().eq("username", username));
    }

    @Override
    public void exportUsers(UserExportQueryDTO query, HttpServletResponse response) throws IOException {
        // 构建查询条件
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            wrapper.like("username", query.getUsername());
        }
        if (query.getPhone() != null && !query.getPhone().isEmpty()) {
            wrapper.like("phone", query.getPhone());
        }
        if (query.getStartTime() != null) {
            wrapper.ge("create_time", query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le("create_time", query.getEndTime());
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
        UserDO existingUser = userMapper.selectByUsername(userDTO.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 插入新用户
        UserDO userDO = new UserDO();
        userDO.setUsername(userDTO.getUsername());
        userDO.setPassword(userDTO.getPassword());
        userDO.setPhone(userDTO.getPhone());
        userDO.setStatus(userDTO.getStatus());
        userDO.setCreateBy("system");
        userDO.setCreateTime(new Date());
        userDO.setUpdateBy("system");
        userDO.setUpdateTime(new Date());
        return userMapper.insert(userDO) > 0;
    }

    @Override
    public Boolean deleteUser(Long userId) {
        // 检查用户是否存在
        UserDO userDO = userMapper.selectById(userId);
        if (userDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 删除用户
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public Boolean updateUser(UserDTO userDTO) {
        // 验证用户是否存在
        UserDO userDO = userMapper.selectById(userDTO.getUserId());
        if (userDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 更新用户信息
        userDO.setUsername(userDTO.getUsername());
        userDO.setPassword(userDTO.getPassword());
        userDO.setPhone(userDTO.getPhone());
        userDO.setStatus(userDTO.getStatus());
        userDO.setUpdateBy("system");
        userDO.setUpdateTime(new Date());
        return userMapper.updateById(userDO) > 0;
    }

    @Override
    public UserDO userInfo(UserQuery userQuery) {
        UserDO userDO = userMapper.selectById(userQuery.getUserId());
        if (userDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        return userDO;
    }

    @Override
    public Page<UserDO> listUsers(int page, int size, String username) {
        Page<UserDO> userPage = new Page<>(page, size);
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        userMapper.selectPage(userPage, wrapper);
        return userPage;
    }
}
