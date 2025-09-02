package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Section;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.SectionMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 版区服务实现类
 */
@Service
public class SectionService extends ServiceImpl<SectionMapper, Section> {

    @Resource
    private SectionMapper sectionMapper;

    /**
     * 分页查询版区列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param sectionName 版区名称（可选，用于模糊查询）
     * @return 分页结果
     */
    public IPage<Section> listSections(Integer current, Integer size, String sectionName, Integer status) {
        LambdaQueryWrapper<Section> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(sectionName)) {
            queryWrapper.like(Section::getSectionName, sectionName);
        }
        if (status != null) {
            queryWrapper.eq(Section::getStatus, status);
        }
        queryWrapper.orderByDesc(Section::getCreateTime);
        
        return sectionMapper.selectPage(new Page<>(current, size), queryWrapper);
    }
    
    /**
     * 根据ID查询版区
     *
     * @param sectionId 版区ID
     * @return 版区信息
     */
    public Section getSectionById(Long sectionId) {
        Section section = sectionMapper.selectById(sectionId);
        if (section == null) {
            throw new ServiceException("版区不存在");
        }
        return section;
    }
    
    /**
     * 新增版区
     *
     * @param section 版区信息
     * @return 是否成功
     */
    public boolean addSection(Section section) {
        // 检查版区名称是否存在
        if (sectionMapper.selectOne(
                new LambdaQueryWrapper<Section>()
                    .eq(Section::getSectionName, section.getSectionName())
            ) != null) {
            throw new ServiceException("版区名称已存在");
        }
        
        // 设置初始值
        section.setCreateTime(new Date());
        if (section.getStatus() == null) {
            section.setStatus(1); // 默认状态为正常
        }
        
        return sectionMapper.insert(section) > 0;
    }
    
    /**
     * 更新版区信息
     *
     * @param section 版区信息
     * @return 是否成功
     */
    public boolean updateSection(Section section) {
        // 检查版区是否存在
        if (sectionMapper.selectById(section.getId()) == null) {
            throw new ServiceException("要更新的版区不存在");
        }
        
        // 检查版区名称是否与其他版区重复
        Section existSection = sectionMapper.selectOne(
            new LambdaQueryWrapper<Section>()
                .eq(Section::getSectionName, section.getSectionName())
        );
        if (existSection != null && !existSection.getId().equals(section.getId())) {
            throw new ServiceException("版区名称已被使用");
        }
        
        section.setUpdateTime(new Date());
        
        return sectionMapper.updateById(section) > 0;
    }
    
    /**
     * 删除版区
     *
     * @param sectionId 版区ID
     * @return 是否成功
     */
    public boolean deleteSection(Long sectionId) {
        // 检查版区是否存在
        if (sectionMapper.selectById(sectionId) == null) {
            throw new ServiceException("要删除的版区不存在");
        }
        
        // TODO: 可以在这里添加检查是否有关联数据的逻辑
        
        return sectionMapper.deleteById(sectionId) > 0;
    }
    
    /**
     * 批量删除版区
     *
     * @param ids 版区ID列表
     * @return 是否成功
     */
    public boolean batchDeleteSections(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("请选择要删除的版区");
        }
        
        return sectionMapper.deleteBatchIds(ids) > 0;
    }
    
    /**
     * 更新版区状态
     *
     * @param sectionId 版区ID
     * @param status 状态（0-禁用，1-正常）
     * @return 是否成功
     */
    public boolean updateSectionStatus(Long sectionId, Integer status) {
        // 检查版区是否存在
        if (sectionMapper.selectById(sectionId) == null) {
            throw new ServiceException("版区不存在");
        }
        
        Section section = new Section();
        section.setId(sectionId);
        section.setStatus(status);
        section.setUpdateTime(new Date());
        
        return sectionMapper.updateById(section) > 0;
    }

    /**
     * 搜索版块
     * @param keyword 关键词
     * @param limit 限制返回数量，null表示不限制
     * @return 版块列表
     */
    public List<Section> searchSections(String keyword, Integer limit) {
        LambdaQueryWrapper<Section> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Section::getSectionName, keyword)
                      .or()
                      .like(Section::getDescription, keyword);
        }
        queryWrapper.eq(Section::getStatus, 1)
                   .orderByDesc(Section::getCreateTime);
        
        if (limit != null) {
            Page<Section> page = new Page<>(1, limit);
            return sectionMapper.selectPage(page, queryWrapper).getRecords();
        } else {
            return sectionMapper.selectList(queryWrapper);
        }
    }
} 