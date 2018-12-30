package com.leyou.service;

import com.leyou.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;

@Service
public class FileService {
    @Autowired
    private GoodsPageService goodsPageService;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${ly.thymeleaf.destPath}")
    private String destPath;//D:\javaWebSofeware\nginx-1.12.2\html
    public void createHtml(Long id)throws Exception{
        //创建上下文
        Context context=new Context();
        //将数据加入到上下文
        context.setVariables(this.goodsPageService.loadItem(id));
        //创建输出流，关联到一个临时文件
        File temp=new File(id+".html") ;
        //目标页面文件
        File dest=createPath(id);
        //备份原页面文件
        File bak=new File(id+"_bak.html");
        try( PrintWriter writer=new PrintWriter(temp,"UTF-8");){
            templateEngine.process("item",context,writer);
            if(dest.exists()){
                //如果目标文件已经存在，先备份
                dest.renameTo(bak);
            }
            //将新页面覆盖旧页面
            FileCopyUtils.copy(temp,dest);
            //成功后将备份页面删除
            bak.delete();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            if(temp.exists()){
                temp.delete();
            }
        }

    }

    private File createPath(Long id) {
        if (id == null) {
            return null;
        }
        File dest = new File(this.destPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        return new File(dest, id + ".html");
    }

    /**
     * 判断某个商品的页面是否存在
     * @param id
     * @return
     */
    public boolean exists(Long id){
        return this.createPath(id).exists();
    }

    /**
     * 异步创建html页面
     * @param id
     */
    public void syncCreateHtml(Long id){
        ThreadUtils.execute(() -> {
            try {
                createHtml(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteHtml(Long id) {
        File file=new File(destPath,id+".html");
        file.deleteOnExit();
    }
}

