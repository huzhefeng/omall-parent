package com.offcn.product.controller;

import com.offcn.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("admin/product/")
public class FileUploadController {

    //读取配置文件中，自定义文件存储服务器的地址和端口
    @Value("${fileServer.url}")
    private String fileUrl;

    //编写一个文件上传方法
    @PostMapping("fileUpload")
    public Result<String> fileUpload(MultipartFile file) throws Exception{
        //D:\JAVA0327-CODE\omall-parent\service\service-product\src\main\resources\tracker.conf
        String configFile  = this.getClass().getResource("/tracker.conf").getFile();
        String path="";
        System.out.println("aa");
        //判断配置文件路径是否为空
        if(configFile!=null){

            //加载配置文件
            ClientGlobal.init(configFile);
            //创建一个连接到Tracker server的客户端对象
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();//使用trackerClient连接到对应Tracker server
            //创建一个连接到Storage server的客户端对象
            StorageClient1 storageClient = new StorageClient1(trackerServer, null);

            //调用storageClient执行文件上传操作
            //1.txt 2.c 3.exe 4.xlsx
            path = storageClient.upload_appender_file1(file.getBytes(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
            System.out.println("上传文件成功:"+fileUrl+path);

        }

        return Result.ok(fileUrl+path);
    }
}
