package com.zecola.cleme.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.UUID;


/**
 * 阿里云 OSS 工具类
 */
@Service
@Slf4j
public class AliOSSUtils {
    @Value("${aliyun.endpoint}")
    private String endpoint;
    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.bucketName}")
    private String bucketName;

    /**
     * 实现上传图片到OSS
     */
    public String upload(InputStream inputStream,String fileName) throws IOException {
        //String filePath= "D:\\hifz.jpg";

        // 获取上传的文件的输入流
        //InputStream inputStream = new FileInputStream(filePath);

        // 避免文件覆盖
        //String originalFilename = inputStream.getClass().getName()+".jpg";
        //String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        //文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
        // 关闭ossClient
        ossClient.shutdown();

        return url;// 把上传到oss的路径返回
    }

}
