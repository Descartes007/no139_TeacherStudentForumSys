package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.ResourceCategoryDTO;
import org.example.springboot.entity.ResourceCategory;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ResourceCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ResourceCategoryService {
    @Resource
    private ResourceCategoryMapper categoryMapper;

    /**
     * 创建资源分类
     * @param categoryDTO 分类信息
     */
    public void createCategory(ResourceCategoryDTO categoryDTO) {
        // 检查分类名称是否已存在
        ResourceCategory existingCategory = categoryMapper.selectOne(
                new LambdaQueryWrapper<ResourceCategory>()
                        .eq(ResourceCategory::getCategoryName, categoryDTO.getCategoryName())
        );
        if (existingCategory != null) {
            throw new ServiceException("分类名称已存在");
        }
        
        // 创建分类对象
        ResourceCategory category = new ResourceCategory();
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setCreateTime(new Date());
        category.setStatus(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : 1);
        
        // 插入数据库
        if (categoryMapper.insert(category) <= 0) {
            throw new ServiceException("分类创建失败");
        }
    }
    
    /**
     * 更新资源分类
     * @param id 分类ID
     * @param categoryDTO 更新的分类信息
     */
    public void updateCategory(Long id, ResourceCategoryDTO categoryDTO) {
        // 验证分类是否存在
        ResourceCategory existingCategory = getCategoryById(id);
        
        // 检查新分类名称是否与其他分类重复
        if (StringUtils.isNotBlank(categoryDTO.getCategoryName())) {
            ResourceCategory categoryWithSameName = categoryMapper.selectOne(
                    new LambdaQueryWrapper<ResourceCategory>()
                            .eq(ResourceCategory::getCategoryName, categoryDTO.getCategoryName())
            );
            if (categoryWithSameName != null && !categoryWithSameName.getId().equals(id)) {
                throw new ServiceException("分类名称已被使用");
            }
        }
        
        // 更新分类对象
        ResourceCategory category = new ResourceCategory();
        category.setId(id);
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setStatus(categoryDTO.getStatus());
        
        // 更新数据库
        if (categoryMapper.updateById(category) <= 0) {
            throw new ServiceException("分类更新失败");
        }
    }
    
    /**
     * 获取所有资源分类列表
     * @return 分类列表
     */
    public List<ResourceCategory> getCategoryList() {
        return categoryMapper.selectList(new LambdaQueryWrapper<>());
    }
    
    /**
     * 获取所有有效分类（状态为1）
     * @return 分类列表
     */
    public List<ResourceCategory> getAllValidCategories() {
        return categoryMapper.selectAllValidCategories();
    }
    
    /**
     * 分页查询资源分类
     * @param categoryName 分类名称（模糊查询）
     * @param status 分类状态
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<ResourceCategory> getCategoriesByPage(String categoryName, Integer status, Integer currentPage, Integer size) {
        Page<ResourceCategory> page = new Page<>(currentPage, size);
        return categoryMapper.selectCategoryPage(page, categoryName, status);
    }
    
    /**
     * 根据ID获取资源分类
     * @param id 分类ID
     * @return 分类信息
     */
    public ResourceCategory getCategoryById(Long id) {
        ResourceCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ServiceException("资源分类不存在");
        }
        return category;
    }
    
    /**
     * 更新资源分类状态
     * @param id 分类ID
     * @param status 分类状态（0-禁用，1-正常）
     */
    public void updateCategoryStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new ServiceException("分类状态不合法");
        }
        
        ResourceCategory category = new ResourceCategory();
        category.setId(id);
        category.setStatus(status);
        
        if (categoryMapper.updateById(category) <= 0) {
            throw new ServiceException("分类状态更新失败");
        }
    }
    
    /**
     * 根据ID删除资源分类
     * @param id 分类ID
     */
    public void deleteCategoryById(Long id) {
        if (categoryMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 批量删除资源分类
     * @param ids 分类ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        if (categoryMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }
} 