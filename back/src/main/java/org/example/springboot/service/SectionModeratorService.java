package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.ModeratorDTO;
import org.example.springboot.entity.SectionModerator;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.SectionModeratorMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SectionModeratorService {
    @Resource
    private SectionModeratorMapper moderatorMapper;
    
    @Resource
    private SectionService sectionService;
    
    @Resource
    private UserService userService;

    /**
     * 任命版主
     * @param moderatorDTO 版主信息
     * @param appointBy 任命人ID
     */
    public void appointModerator(ModeratorDTO moderatorDTO, Long appointBy) {
        // 检查版区是否存在
        sectionService.getSectionById(moderatorDTO.getSectionId());
        
        // 检查用户是否存在
        User user = userService.getUserById(moderatorDTO.getUserId());
        
        // 检查用户是否已经是该版区的版主
        if (moderatorMapper.selectOne(
                new LambdaQueryWrapper<SectionModerator>()
                    .eq(SectionModerator::getSectionId, moderatorDTO.getSectionId())
                    .eq(SectionModerator::getUserId, moderatorDTO.getUserId())
                    .eq(SectionModerator::getStatus, 1)
            ) != null) {
            throw new ServiceException(user.getUsername() + " 已经是该版区的版主");
        }
        
        SectionModerator moderator = new SectionModerator();
        moderator.setSectionId(moderatorDTO.getSectionId());
        moderator.setUserId(moderatorDTO.getUserId());
        moderator.setAppointTime(new Date());
        moderator.setAppointBy(appointBy);
        moderator.setStatus(moderatorDTO.getStatus() != null ? moderatorDTO.getStatus() : 1);
        moderator.setRemark(moderatorDTO.getRemark());
        
        if (moderatorMapper.insert(moderator) <= 0) {
            throw new ServiceException("任命版主失败");
        }
    }
    
    /**
     * 更新版主信息
     * @param id 版主记录ID
     * @param moderatorDTO 更新的版主信息
     */
    public void updateModerator(Long id, ModeratorDTO moderatorDTO) {
        // 检查版主记录是否存在
        SectionModerator existModerator = moderatorMapper.selectById(id);
        if (existModerator == null) {
            throw new ServiceException("版主记录不存在");
        }
        
        SectionModerator moderator = new SectionModerator();
        moderator.setId(id);
        moderator.setStatus(moderatorDTO.getStatus());
        moderator.setRemark(moderatorDTO.getRemark());
        
        if (moderatorMapper.updateById(moderator) <= 0) {
            throw new ServiceException("更新版主信息失败");
        }
    }
    
    /**
     * 分页查询版主
     * @param sectionId 版区ID（可选）
     * @param username 用户名（模糊查询，可选）
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<SectionModerator> getModeratorsByPage(Long sectionId, String username, Integer currentPage, Integer size) {
        Page<SectionModerator> page = new Page<>(currentPage, size);
        return moderatorMapper.selectModeratorPage(page, sectionId, username);
    }
    
    /**
     * 根据ID获取版主记录
     * @param id 版主记录ID
     * @return 版主记录
     */
    public SectionModerator getModeratorById(Long id) {
        SectionModerator moderator = moderatorMapper.selectById(id);
        if (moderator == null) {
            throw new ServiceException("版主记录不存在");
        }
        return moderator;
    }
    
    /**
     * 根据版区ID获取该版区的所有版主
     * @param sectionId 版区ID
     * @return 版主列表
     */
    public List<SectionModerator> getModeratorsBySectionId(Long sectionId) {
        List<SectionModerator> sectionModerators = moderatorMapper.selectBySectionId(sectionId);
        sectionModerators.forEach(this::fillUserInfo);
        System.out.println("根据id获取版区,"+sectionModerators);
        return sectionModerators;
    }

    /*
     * 填充用户信息
     * @param moderator 版主记录
     */
    private void fillUserInfo(SectionModerator moderator) {
        if(moderator.getUserId()!=null) {
            User user = userService.getUserById(moderator.getUserId());
            moderator.setUser(user);
        }
    }
    
    /**
     * 根据用户ID获取该用户担任版主的所有版区
     * @param userId 用户ID
     * @return 版主列表
     */
    public List<SectionModerator> getModeratorsByUserId(Long userId) {
        return moderatorMapper.selectByUserId(userId);
    }
    
    /**
     * 根据ID删除版主记录
     * @param id 版主记录ID
     */
    public void deleteModeratorById(Long id) {
        if (moderatorMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 批量删除版主记录
     * @param ids 版主记录ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        if (moderatorMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }
    
    /**
     * 更新版主状态
     * @param id 版主记录ID
     * @param status 状态（0-停用，1-正常）
     */
    public void updateModeratorStatus(Long id, Integer status) {
        SectionModerator moderator = new SectionModerator();
        moderator.setId(id);
        moderator.setStatus(status);
        
        if (moderatorMapper.updateById(moderator) <= 0) {
            throw new ServiceException("状态更新失败");
        }
    }
    
    /**
     * 检查用户是否是某版区的版主
     * @param sectionId 版区ID
     * @param userId 用户ID
     * @return 是否是版主
     */
    public boolean isModerator(Long sectionId, Long userId) {
        return moderatorMapper.selectOne(
            new LambdaQueryWrapper<SectionModerator>()
                .eq(SectionModerator::getSectionId, sectionId)
                .eq(SectionModerator::getUserId, userId)
                .eq(SectionModerator::getStatus, 1)
        ) != null;
    }
} 