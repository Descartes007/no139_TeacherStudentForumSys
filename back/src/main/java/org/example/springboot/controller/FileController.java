package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.enumClass.FileType;
import org.example.springboot.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "文件上传接口类")
@RequestMapping("/file")
@RestController
public class FileController {

    @Autowired
    private FileService fileService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Operation(summary = "文件上传")
    @PostMapping("/upload/img")
    public Result<?> upLoad(@RequestParam("file") MultipartFile file) {
      return   fileService.upLoad(file, FileType.IMG);
    }
    
    @Operation(summary = "普通文件上传")
    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        Result<?> result = fileService.upLoad(file, FileType.COMMON);
        if (result.getCode().equals("0")) {
            Map<String, Object> data = new HashMap<>();
            data.put("data", result.getData());
            data.put("size", file.getSize() / 1024); // 转换为KB
            data.put("type", file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1));
            return Result.success(data);
        } else {
            return result;
        }
    }
    
    @Operation(summary = "多文件上传，并且在有失败时删除已上传成功的文件")
    @PostMapping("/uploadMultiple")
    public Result<?> uploadMultiple(@RequestParam("files") List<MultipartFile> files) {
        List<String> strings = fileService.uploadMultiple(files);
        return !strings.isEmpty() ? Result.success(strings):Result.error("-1","文件上传失败！");
    }
}





