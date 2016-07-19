package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.QiniuService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zfx on 2016/7/17.
 */
@Controller
public class NewsController {
    @Autowired
    NewsService newsService;
    @Autowired
    UserService userService;
    @Autowired
    QiniuService qiniuService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    CommentService commentService;

    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    @RequestMapping(path = "/news/{newsId}",method = {RequestMethod.GET})
    public String newsDetail(@PathVariable("newsId") int newsId, Model model){
        News news = newsService.getById(newsId);
        if(news != null){
          //评论
            List<Comment> comments = commentService.getCommentsByEntity(news.getId(), EntityType.ENTITY_NEWS);
            List<ViewObject> commentVos = new ArrayList<ViewObject>();
            for(Comment comment : comments){
                ViewObject vo = new ViewObject();
                vo.set("comment",comment);
                vo.set("user",userService.getUser(comment.getUserId()));
                commentVos.add(vo);
            }
            model.addAttribute("comments",commentVos);
        }
        model.addAttribute("news",news);
        model.addAttribute("owner",userService.getUser(news.getUserId()));
        System.out .print(userService.getUser(news.getUserId()).getName());
        return "detail";
    }

    @RequestMapping(path = "/addComment",method = {RequestMethod.POST})
    public String addComment(@RequestParam("newsId") int newsId,
                             @RequestParam("content") String content){
        try{
            Comment comment = new Comment();
            comment.setUserId(hostHolder.getUser().getId());
            comment.setContent(content);
            comment.setEntityId(newsId);
            comment.setEntityType(EntityType.ENTITY_NEWS);
            comment.setCreatedDate(new Date());
            comment.setStatus(0);

            commentService.addComment(comment);
            //更新news里面的数量
            int count = commentService.getCommentCount(comment.getEntityId(),comment.getEntityType());
            newsService.updateCommentCount(comment.getEntityId(),count);
            //异步化
        }catch(Exception e){
            logger.error("添加评论失败！"+e.getMessage());
        }
        return "redirect:/news/"+String.valueOf(newsId);
    }

    @RequestMapping(path = "/user/addNews/",method = {RequestMethod.POST})
    @ResponseBody
    public String addNews(@RequestParam("image") String image,
                          @RequestParam("title") String title,
                          @RequestParam("link") String link){
         try{
             News news = new News();
             if(hostHolder.getUser() != null){
                 news.setUserId(hostHolder.getUser().getId());
             }else{
                 //匿名id
                 news.setUserId(3);
             }
             news.setImage(image);
             news.setCreatedDate(new Date());
             news.setLink(link);
             news.setTitle(title);
             newsService.addNews(news);
             return ToutiaoUtil.getJSONString(0);
         }catch(Exception e){
            logger.error("添加资讯错误！" + e.getMessage());
             return ToutiaoUtil.getJSONString(1,"发布失败！");
        }
    }

    @RequestMapping(path={"/image"},method = {RequestMethod.GET})
    @ResponseBody
    public void getImage(@RequestParam("name") String imageName,
                         HttpServletResponse response){
        try {
            response.setContentType("image/jpeg");
            StreamUtils.copy(new FileInputStream(new File(ToutiaoUtil.IMAGE_DIR + imageName)), response.getOutputStream());
        }catch (Exception e ){
            logger.error("读取图片错误！！"+e.getMessage());
        }
    }

    @RequestMapping(path = {"/uploadImage/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String uploadImage(@RequestParam("file")MultipartFile file){
        try{
           // String fileUrl = newsService.saveImage(file);//本地上传
            String fileUrl = qiniuService.saveImage(file);
            if(fileUrl == null){
                return ToutiaoUtil.getJSONString(1,"上传图片失败！");
            }
            return ToutiaoUtil.getJSONString(0,fileUrl);
        }catch (Exception e){
            logger.error("上传图片失败"+e.getMessage());
            return ToutiaoUtil.getJSONString(1,"上传失败");
        }
    }
}
