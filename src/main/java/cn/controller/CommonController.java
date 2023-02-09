package cn.controller;

import cn.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
/**
 * 上传图片
 */
public class CommonController {

    @Value("${reggie.path}")
    private String realPath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {          //请求中的图片是临时文件, 将其转换成正常图片并存入到指定文件夹下, 下载输出展示时方便读取数据

        //获取临时图片原名称
        String originalFilename = file.getOriginalFilename();       //abc.png
        log.info("originalFilename={}", originalFilename);

        //截取后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        log.info("后缀={}", suffix);

        //图片原名可能重复, 使用雪花算法重置原名
        String lastName = UUID.randomUUID().toString() + suffix;
        log.info("最后名称={}", lastName);

        //指定文件夹, 若不存在则创建文件夹    路径采用全局配置
        File createFile = new File(realPath);
        if (!createFile.exists()) {
            createFile.mkdirs();
        }

        //将图片保存目录下
        file.transferTo(new File(realPath+lastName));
        log.info("文件位置={}",realPath+lastName);

        return R.success(lastName);     //前端获取文件名
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws Exception {     //name是upload时返回的名称
        //若直接从点击图片的位置下载并输出到页面需要指定文件夹路径, 所以获取临时文件上传到后端指定目录下
        log.info("name={}", name);

        //输入流读取存放在目录下的图片
        FileInputStream inputStream = new FileInputStream(new File(realPath+name));     //读指定路径

        byte[] bytes = new byte[1024];      //输入 进, 输出 出  容器

        int len = 0;

        while ((len = inputStream.read(bytes)) != -1) {
            response.getOutputStream().write(bytes, 0, len);        //输出到页面
        }

        //关闭流
        inputStream.close();
    }
}
