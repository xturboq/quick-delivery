package com.zecola.cleme.controller;

import com.zecola.cleme.common.R;
import com.zecola.cleme.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件上传和下载
 *
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController{
    @Autowired
    private AliOSSUtils alioos;

    @Value("${zecola.path}")
    private String basepath;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        log.info(file.toString());
        //原始文件名
        String originalFilename = file.getOriginalFilename();

        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID生成新的文件名，防止重复
        String filename = java.util.UUID.randomUUID().toString() + substring;
        InputStream inputStream = file.getInputStream();
        //上传至阿里云OOS
        String url= alioos.upload(inputStream,filename);

        //file.transferTo(new File(basepath + filename));
        //String url = alioos.upload(originalFilename,filename);
        //转存文件并生成新的文件名
        log.info("文件上传成功,url = "+url);
        return R.success(url);
    }
}
