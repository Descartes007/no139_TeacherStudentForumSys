package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.User;
import org.example.springboot.DTO.UserLoginDTO;
import org.example.springboot.DTO.UserPasswordUpdateDTO;
import org.example.springboot.DTO.UserRegisterDTO;
import org.example.springboot.DTO.UserStatsDTO;
import org.example.springboot.DTO.UserProfileUpdateDTO;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="用户管理接口")
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Resource
    private UserService userService;

    @Operation(summary = "根据id获取用户信息")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // 如果用户不存在会抛出异常，由全局异常处理器处理
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "根据username获取用户信息")
    @GetMapping("/username/{username}")
    public Result<?> getUserByUsername(@PathVariable String username) {
        // 不存在的用户会抛出异常
        User user = userService.getByUsername(username);
        return Result.success(user);
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        User loginUser = userService.login(loginDTO);
        return Result.success(loginUser);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    @Operation(summary = "密码修改")
    @PutMapping("/password/{id}")
    public Result<?> updatePassword(@PathVariable Long id, @RequestBody @Valid UserPasswordUpdateDTO userPasswordUpdate) {
        // 密码修改失败会抛出异常
        userService.updatePassword(id, userPasswordUpdate);
        return Result.success("密码修改成功");
    }

    @Operation(summary = "忘记密码")
    @PostMapping("/forget")
    public Result<?> forgetPassword(@RequestParam String email, @RequestParam String newPassword) {
        // 密码重置失败会抛出异常
        userService.forgetPassword(email, newPassword);
        return Result.success("密码重置成功");
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<?> getUsersByPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer role,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> page = userService.getUsersByPage(username, realName, role, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据角色获取用户列表")
    @GetMapping("/role/{role}")
    public Result<?> getUserByRole(@PathVariable Integer role) {
        List<User> users = userService.getUserByRole(role);
        return Result.success(users);
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "获取所有用户")
    @GetMapping("/all")
    public Result<?> getUserList() {
        List<User> list = userService.getUserList();
        return Result.success(list);
    }

    @Operation(summary = "创建新用户")
    @PostMapping("/add")
    public Result<?> createUser(@RequestBody @Valid User user) {
        userService.createUser(user);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        // 更新失败会抛出具体原因的异常
        userService.updateUser(id, user);
        return Result.success("更新成功");
    }

    @Operation(summary = "根据id删除用户")
    @DeleteMapping("/{id}")
    public Result<?> deleteUserById(@PathVariable Long id) {
        // 删除失败会抛出异常
        userService.deleteUserById(id);
        return Result.success("删除成功");
    }
    
    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "获取用户统计数据")
    @GetMapping("/{id}/stats")
    public Result<?> getUserStats(@PathVariable Long id) {
        UserStatsDTO stats = userService.getUserStats(id);
        return Result.success(stats);
    }
    
    @Operation(summary = "更新用户个人资料")
    @PutMapping("/{id}/profile")
    public Result<?> updateUserProfile(@PathVariable Long id, @RequestBody @Valid UserProfileUpdateDTO profileUpdate) {
        userService.updateUserProfile(id, profileUpdate);
        return Result.success("个人资料更新成功");
    }
}
