package com.zgj.mps.controller;

import com.zgj.mps.model.Media;
import com.zgj.mps.model.Mgroup;
import com.zgj.mps.model.User;
import com.zgj.mps.service.MediaService;
import com.zgj.mps.tool.MD5Util;
import com.zgj.mps.tool.OSSClientUtil;
import com.zgj.mps.tool.ResultUtil;
import com.zgj.mps.tool.ShiroSecurityUtil;
import com.zgj.mps.vo.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Random;

/**
 * Created by user on 2019/9/19.
 */
@Slf4j
@RestController
@Api(description = "上传文件接口")
@RequestMapping("/oss")
@Transactional
public class OssController {

    @Autowired
    private OSSClientUtil ossClientUtil;

    @Value("${video.dir}")
    private String videoDir;

    @Autowired
    private MediaService mediaService;

    @Value("${oss.accessUrl}")
    private String accessUrl;

//    @Autowired
//    private TransFlvProcesser transFlvProcesser;

    @Autowired
    private ShiroSecurityUtil shiroSecurityUtil;

    @PostMapping(value = "/uploadAvatar")
    public Result<String> uploadAvatar(@RequestParam(name = "file") MultipartFile file){
        String result = "";
        try {
            Random random = new Random();
            String fileName = random.nextInt(10000) + System.currentTimeMillis()+"";
            result = ossClientUtil.uploadFileToOSS(file, "sysimg/"+fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResultUtil<String>().setErrorMsg("上传文件失败，请稍后再试");
        }
        return new ResultUtil<String>().setData(result);
    }

    @RequestMapping(value = "/uploadOss", method = RequestMethod.POST)
    public Result<String> uploadOss(@RequestParam(name = "file") MultipartFile file,
                                    @RequestParam(name = "mGroupId", required = true) long mGroupId,
                                    @RequestParam(name = "type", required = true) short type,
                                    @RequestParam(name = "size", required = true) int size,
                                    @RequestParam(name = "name", required = true) String name) {
        log.info("{},{},{},{}", type, size, name, mGroupId);
        String result = "";
        Media media = new Media();
        Random random = new Random();
        String tmpFileName = random.nextInt(10000) + System.currentTimeMillis() + "";
        try {

            //1.上传oss,视频文件先上传服务器，转换为flv再上传oss
            if (type != 1) {
                result = ossClientUtil.uploadFileToOSS(file);
                media.setPath(result);
                media.setMd5(MD5Util.getMyMd5(media.getPath()));
            }else {
                media.setRemark("transitioning");
                media.setPath(accessUrl);
            }
            //2.保存数据库
            User user = shiroSecurityUtil.getCurrentUser();
            media.setUser(user);
            Mgroup mgroup = new Mgroup();
            mgroup.setId(mGroupId);
            media.setMgroup(mgroup);
            media.setType(type);
            media.setSize(size);
            media.setName(name);
            media.setIsDelete((short) 0);
            media.setCreateTime(new Timestamp(System.currentTimeMillis()));
            if (type == 0) {
                try {
                    BufferedImage sourceImg = ImageIO.read(new URL(media.getPath()).openStream());
                    media.setWidth(sourceImg.getWidth());
                    media.setHeight(sourceImg.getHeight());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaService.save(media);
            if (type == 1) {
                Media newMedia = new Media();
                newMedia.setId(media.getId());
                //3.ftp
                String fileName = file.getOriginalFilename();
                String prefix = fileName.substring(fileName.lastIndexOf("."));
                File tmpFile = new File(videoDir);

                File f = File.createTempFile(tmpFileName, prefix, tmpFile);
                file.transferTo(f);
                newMedia.setName(f.getName());
                newMedia.setPath(videoDir  + File.separator + f.getName());
                newMedia.setUser(media.getUser());
//                ByteArrayOutputStream byteArrayOutputStream = FFmpegUtil.getVideoImage(f, 600, 800, 5);
//                ossClientUtil.uploadByByteToOSS(byteArrayOutputStream.toByteArray(), f.getName().substring(0,f.getName().lastIndexOf(".")) + ".jpg");
//                transFlvProcesser.produce(newMedia);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResultUtil<String>().setErrorMsg("上传文件失败，请稍后再试");
        }
        return new ResultUtil<String>().setSuccessMsg("上传成功");
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public void delete(@RequestParam(name = "filePath") String filePath) {
        ossClientUtil.deleteFile(filePath);
    }
}
