package com.nowcoder;

import com.nowcoder.dao.CommentDao;
import com.nowcoder.dao.LoginTicketDao;
import com.nowcoder.dao.NewsDao;
import com.nowcoder.dao.UserDao;
import com.nowcoder.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Random;

/**
 * Created by zfx on 2016/7/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ToutiaoApplication.class)
@Sql("/init-schema.sql")
public class InitDataBaseTests {
    @Autowired
    UserDao userDao;
    @Autowired
    NewsDao newsDao;
    @Autowired
    LoginTicketDao loginTicketDao;
    @Autowired
    CommentDao commentDao;

    @Test
    public void initDate() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", random.nextInt(1000)));
            user.setName(String.format("user%d", i));
            user.setPassword("");
            user.setSalt("");
            userDao.addUser(user);

            News news = new News();
            news.setCommentCount(i);
            Date date = new Date();
            date.setTime(date.getTime() + 1000 * 3600 * 5 * i);
            news.setCreatedDate(date);
            news.setImage(String.format("http://images.nowcoder.com/head/%dm.png", random.nextInt(1000)));
            news.setLikeCount(i + 1);
            news.setUserId(i + 1);
            news.setTitle(String.format("TITLE{%d}", i));
            news.setLink(String.format("http://www.nowcoder.com/%d.html", i));
            newsDao.addNews(news);

            for(int j=0;j<3;++j){
                Comment comment = new Comment();
                comment.setUserId(i+1);
                comment.setEntityId(news.getId());
                comment.setEntityType(EntityType.ENTITY_NEWS);
                comment.setStatus(0);
                comment.setCreatedDate(new Date());
                comment.setContent("Content"+String.valueOf(j));
                commentDao.addComment(comment);
            }

            LoginTicket loginTicket = new LoginTicket();
            loginTicket.setUserId(i+1);
            loginTicket.setStatus(0);
            loginTicket.setExpired(date);
            loginTicket.setTicket(String.format("ticket%d",i+1));
            loginTicketDao.addTicket(loginTicket);
            loginTicketDao.updateStatus(loginTicket.getTicket(),2);

            user.setPassword("newPassword");
            userDao.updatePassword(user);
        }
        Assert.assertEquals(1,loginTicketDao.selectByTicket("ticket1").getUserId());
        Assert.assertEquals(2,loginTicketDao.selectByTicket("ticket2").getStatus());
        Assert.assertNotNull(commentDao.selectByEntity(1,EntityType.ENTITY_NEWS).get(0));
    }
}
