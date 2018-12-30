package com.leyou.service.impl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.UploadProperties;
import com.leyou.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    private Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Autowired
    private UploadProperties uploadProperties;

    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private ThumbImageConfig thumbImageConfig; //fdfs缩略图工具类


    @Override
    public String upLoadImage(MultipartFile file) {
        //校验文件是否是图片
        try {
            String contentType = file.getContentType();
            if (!uploadProperties.getAllowTypes().contains(contentType)) {
                logger.info("上传的不是图片文件" + contentType);
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件内容是否符合图片规范
            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read == null) {
                logger.info("上传的文件不符合规范");
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //将文件写入对应的储存地址
            // 2、将图片上传到FastDFS
            // 2.1、获取文件后缀名
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            // 2.2、上传
            StorePath storePath = fastFileStorageClient.uploadFile(
                    file.getInputStream(), file.getSize(), extension, null);
            log.info("原图完整访问路径：" + uploadProperties.getBaseUrl() + storePath.getFullPath());

            // 2.2、上传并生成缩略图
//            StorePath storePath = fastFileStorageClient.uploadImageAndCrtThumbImage(
//                    file.getInputStream(), file.getSize(), extension, null);
//            log.info("缩略图图完整访问路径：" + uploadProperties.getBaseUrl() + thumbImageConfig.getThumbImagePath(storePath.getFullPath()));

            // 2.3、返回完整路径
            return uploadProperties.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
