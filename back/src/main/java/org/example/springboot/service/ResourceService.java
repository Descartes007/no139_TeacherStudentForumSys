package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.ResourceDTO;
import org.example.springboot.entity.ForumResource;
import org.example.springboot.entity.ResourceCategory;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ResourceCategoryMapper;
import org.example.springboot.mapper.ResourceMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ResourceService {
    @Resource
    private ResourceMapper resourceMapper;
    
    @Resource
    private ResourceCategoryMapper categoryMapper;
    
    @Resource
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建资源
     * @param resourceDTO 资源信息
     * @param userId 上传用户ID
     */
    public void createResource(ResourceDTO resourceDTO, Long userId) {
        // 验证用户是否存在
        userService.getUserById(userId);
        
        // 验证分类是否存在
        ResourceCategory category = categoryMapper.selectById(resourceDTO.getCategoryId());
        if (category == null) {
            throw new ServiceException("资源分类不存在");
        }
        
        // 创建资源对象
        ForumResource resource = new ForumResource();
        resource.setResourceName(resourceDTO.getResourceName());
        resource.setDescription(resourceDTO.getDescription());
        resource.setCategoryId(resourceDTO.getCategoryId());
        resource.setFilePath(resourceDTO.getFilePath());
        resource.setFileSize(resourceDTO.getFileSize());
        resource.setFileType(resourceDTO.getFileType());
        resource.setUserId(userId);
        resource.setCreateTime(new Date());
        resource.setDownloadCount(0);
        resource.setStatus(resourceDTO.getStatus() != null ? resourceDTO.getStatus() : 1);
        
        // 插入数据库
        if (resourceMapper.insert(resource) <= 0) {
            throw new ServiceException("资源创建失败");
        }
    }
    
    /**
     * 更新资源信息
     * @param id 资源ID
     * @param resourceDTO 更新的资源信息
     */
    public void updateResource(Long id, ResourceDTO resourceDTO) {
        // 验证资源是否存在
        ForumResource existingResource = getResourceById(id);
        
        // 验证分类是否存在
        if (resourceDTO.getCategoryId() != null) {
            ResourceCategory category = categoryMapper.selectById(resourceDTO.getCategoryId());
            if (category == null) {
                throw new ServiceException("资源分类不存在");
            }
        }
        
        // 更新资源对象
        ForumResource resource = new ForumResource();
        resource.setId(id);
        resource.setResourceName(resourceDTO.getResourceName());
        resource.setDescription(resourceDTO.getDescription());
        resource.setCategoryId(resourceDTO.getCategoryId());
        
        // 只有在提供了文件信息时才更新文件相关字段
        if (StringUtils.isNotBlank(resourceDTO.getFilePath())) {
            resource.setFilePath(resourceDTO.getFilePath());
            resource.setFileSize(resourceDTO.getFileSize());
            resource.setFileType(resourceDTO.getFileType());
        }
        
        resource.setStatus(resourceDTO.getStatus());
        
        // 更新数据库
        if (resourceMapper.updateById(resource) <= 0) {
            throw new ServiceException("资源更新失败");
        }
    }
    
    /**
     * 获取所有资源列表
     * @return 资源列表
     */
    public List<ForumResource> getResourceList() {
        Page<ForumResource> page = new Page<>(1, Integer.MAX_VALUE);
        Page<ForumResource> resourcePage = resourceMapper.selectResourcePage(page, null, null, null, null, null);
        return resourcePage.getRecords();
    }
    
    /**
     * 分页查询资源
     * @param resourceName 资源名称（模糊查询）
     * @param categoryId 分类ID
     * @param fileType 文件类型
     * @param userId 上传用户ID
     * @param status 资源状态
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<ForumResource> getResourcesByPage(String resourceName, Long categoryId, String fileType, Long userId, Integer status, Integer currentPage, Integer size) {
        Page<ForumResource> page = new Page<>(currentPage, size);

        Page<ForumResource> forumResourcePage = resourceMapper.selectResourcePage(page, resourceName, categoryId, fileType, userId, status);
        forumResourcePage.getRecords().forEach(this::fillRelateInfo);

        return forumResourcePage;
    }
    
    /**
     * 根据ID获取资源
     * @param id 资源ID
     * @return 资源信息
     */
    public ForumResource getResourceById(Long id) {
        ForumResource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new ServiceException("资源不存在");
        }
        // 填充资源的用户信息
        fillRelateInfo(resource);
        return resource;
    }

    /**
     * 填充资源的信息
     * @param resource 资源
     */
    private void fillRelateInfo(ForumResource resource) {
        if (resource.getUserId()!= null) {
            resource.setUploadUsername(userMapper.selectById(resource.getUserId()).getUsername());
        }

        if (resource.getCategoryId()!= null) {
            resource.setCategoryName(categoryMapper.selectById(resource.getCategoryId()).getCategoryName());
        }
    }

    /**
     * 更新资源状态
     * @param id 资源ID
     * @param status 资源状态（0-删除，1-正常）
     */
    public void updateResourceStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new ServiceException("资源状态不合法");
        }
        
        ForumResource resource = new ForumResource();
        resource.setId(id);
        resource.setStatus(status);
        
        if (resourceMapper.updateById(resource) <= 0) {
            throw new ServiceException("资源状态更新失败");
        }
    }
    
    /**
     * 更新资源下载次数
     * @param id 资源ID
     */
    public void incrementDownloadCount(Long id) {
        // 验证资源是否存在
        getResourceById(id);
        
        if (resourceMapper.updateDownloadCount(id) <= 0) {
            throw new ServiceException("下载次数更新失败");
        }
    }
    
    /**
     * 根据用户ID获取资源列表
     * @param userId 用户ID
     * @return 资源列表
     */
    public List<ForumResource> getResourcesByUserId(Long userId) {
        return resourceMapper.selectByUserId(userId);
    }
    
    /**
     * 根据分类ID获取资源列表
     * @param categoryId 分类ID
     * @return 资源列表
     */
    public List<ForumResource> getResourcesByCategoryId(Long categoryId) {
        return resourceMapper.selectByCategoryId(categoryId);
    }
    
    /**
     * 根据ID删除资源
     * @param id 资源ID
     */
    public void deleteResourceById(Long id) {
        if (resourceMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 批量删除资源
     * @param ids 资源ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        if (resourceMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }
} 