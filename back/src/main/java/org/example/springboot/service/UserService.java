package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;

import org.example.springboot.DTO.*;
import org.example.springboot.entity.Comment;
import org.example.springboot.entity.Post;
import org.example.springboot.entity.PostFavorite;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.enumClass.UserRole;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.PostMapper;
import org.example.springboot.mapper.CommentMapper;
import org.example.springboot.mapper.PostFavoriteMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private PostFavoriteMapper postFavoriteMapper;

    @Value("${user.defaultPassword:123456}")
    private String DEFAULT_PWD;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    public User getByEmail(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        return user;
    }

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录成功的用户信息
     */
    public User login(UserLoginDTO loginDTO) {
        User user = getByUsername(loginDTO.getUsername());
        // 用户存在性检查已经在 getByUsername 中处理
        
        if (user.getStatus().equals(AccountStatus.PENDING_REVIEW.getValue())) {
            throw new ServiceException("账号正在审核");
        }
        if (user.getStatus().equals(AccountStatus.REVIEW_FAILED.getValue())) {
            throw new ServiceException("账号审核不通过，请修改个人信息");
        }
        if (user.getStatus().equals(AccountStatus.DISABLED.getValue())) {
            throw new ServiceException("账号已被禁用，请联系管理员");
        }
        
        if (!bCryptPasswordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        
        String token = JwtTokenUtils.genToken(String.valueOf(user.getId()), user.getPassword());
        user.setToken(token);
        return user;
    }

    /**
     * 根据角色查询用户
     * @param role 角色类型（1-管理员，2-教师，3-学生）
     * @return 用户列表
     */
    public List<User> getUserByRole(Integer role) {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getRole, role)
        );
        if (users.isEmpty()) {
            throw new ServiceException("未找到该角色的用户");
        }
        return users;
    }

    /**
     * 用户注册
     * @param registerDTO 注册信息
     */
    public void register(UserRegisterDTO registerDTO) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, registerDTO.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 检查邮箱是否被使用
        if (StringUtils.isNotBlank(registerDTO.getEmail()) && userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, registerDTO.getEmail())
            ) != null) {
            throw new ServiceException("邮箱已被使用");
        }

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        // 设置默认值
        user.setPassword(bCryptPasswordEncoder.encode(registerDTO.getPassword()));
        user.setCreateTime(new Date());
        user.setStatus(AccountStatus.NORMAL.getValue()); // 默认状态为正常
        user.setAvatar("/avatars/default.png"); // 默认头像
        
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户注册失败");
        }
    }

    /**
     * 创建用户（管理员操作）
     * @param user 用户信息
     */
    public void createUser(User user) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 检查邮箱是否被使用
        if (StringUtils.isNotBlank(user.getEmail()) && userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail())
            ) != null) {
            throw new ServiceException("邮箱已被使用");
        }

        // 设置默认值
        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setCreateTime(new Date());
        
        if (user.getStatus() == null) {
            user.setStatus(AccountStatus.NORMAL.getValue());
        }
        
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户创建失败");
        }
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 更新的用户信息
     */
    public void updateUser(Long id, User user) {
        // 检查用户是否存在
        if (userMapper.selectById(id) == null) {
            throw new ServiceException("要更新的用户不存在");
        }
        
        // 检查新用户名是否与其他用户重复
        if (user.getUsername() != null) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            );
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new ServiceException("新用户名已被使用");
            }
        }
        
        // 检查新邮箱是否与其他用户重复
        if (StringUtils.isNotBlank(user.getEmail())) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail())
            );
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new ServiceException("新邮箱已被使用");
            }
        }
        
        user.setId(id);
        user.setUpdateTime(new Date());
        
        // 不更新密码
        user.setPassword(null);
        
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("用户更新失败");
        }
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    public User getByUsername(String username) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        if (userMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }

    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    public List<User> getUserList() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        return users;
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 分页查询用户
     * @param username 用户名（模糊查询）
     * @param realName 真实姓名（模糊查询）
     * @param role 角色类型
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<User> getUsersByPage(String username, String realName, Integer role, Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.isNotBlank(realName)) {
            queryWrapper.like(User::getRealName, realName);
        }
        if (role != null) {
            queryWrapper.eq(User::getRole, role);
        }
        
        return userMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    /**
     * 更新用户密码
     * @param id 用户ID
     * @param update 密码更新信息
     */
    public void updatePassword(Long id, UserPasswordUpdateDTO update) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证旧密码
        if (!bCryptPasswordEncoder.matches(update.getOldPassword(), user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        
        // 更新新密码
        user.setPassword(bCryptPasswordEncoder.encode(update.getNewPassword()));
        user.setUpdateTime(new Date());
        
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码修改失败");
        }
    }

    /**
     * 忘记密码（重置密码）
     * @param email 邮箱
     * @param newPassword 新密码
     */
    public void forgetPassword(String email, String newPassword) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        user.setUpdateTime(new Date());
        
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码重置失败");
        }
    }

    /**
     * 根据ID删除用户
     * @param id 用户ID
     */
    public void deleteUserById(Long id) {
        if (userMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态（0-禁用，1-正常）
     */
    public void updateUserStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(new Date());
        
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("状态更新失败");
        }
    }

    /**
     * 获取用户统计数据
     * @param userId 用户ID
     * @return 用户统计数据
     */
    public UserStatsDTO getUserStats(Long userId) {
        // 检查用户是否存在
        if (userMapper.selectById(userId) == null) {
            throw new ServiceException("用户不存在");
        }
        
        UserStatsDTO stats = new UserStatsDTO();
        
        // 获取帖子数量
        stats.setPostCount(Math.toIntExact(postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, userId)
                        .eq(Post::getStatus, 1)
        )));
        
        // 获取评论数量
        stats.setCommentCount(Math.toIntExact(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, userId)
                        .eq(Comment::getStatus, 1)
        )));
        
        // 获取收藏数量
        stats.setFavoriteCount(Math.toIntExact(postFavoriteMapper.selectCount(
                new LambdaQueryWrapper<PostFavorite>()
                        .eq(PostFavorite::getUserId, userId)
                        .eq(PostFavorite::getStatus, 1)
        )));
        
        return stats;
    }
    
    /**
     * 更新用户个人资料
     * @param userId 用户ID
     * @param profileUpdate 更新的资料信息
     */
    public void updateUserProfile(Long userId, UserProfileUpdateDTO profileUpdate) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 检查邮箱是否被其他用户使用
        if (StringUtils.isNotBlank(profileUpdate.getEmail())) {
            User emailUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, profileUpdate.getEmail())
            );
            if (emailUser != null && !emailUser.getId().equals(userId)) {
                throw new ServiceException("该邮箱已被其他用户使用");
            }
        }
        
        // 更新用户信息
        User user = new User();
        user.setId(userId);
        user.setEmail(profileUpdate.getEmail());
        user.setSignature(profileUpdate.getSignature());
        user.setAvatar(profileUpdate.getAvatar());
        user.setProfile(profileUpdate.getProfile());
        user.setUpdateTime(new Date());
        
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("个人资料更新失败");
        }
    }
}
