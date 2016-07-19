package com.nowcoder.service;

import com.nowcoder.controller.LoginController;
import com.nowcoder.dao.LoginTicketDao;
import com.nowcoder.dao.UserDao;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by zfx on 2016/7/6.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private LoginTicketDao loginTicketDao;

    public Map<String,Object> register(String username, String password){
        Map<String,Object> map = new HashMap<String,Object>();
        if(StringUtils.isBlank(username)){
             map.put("msgname","用户名不能为空!");
             return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msgpwd","密码不能为空！");
            return map;
        }
        User user = userDao.selectByName(username);
        if(user != null){
            map.put("msg","用户名已存在！");
            return map;
        }
        //密码强度
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setPassword(ToutiaoUtil.MD5(password+user.getSalt()));
        userDao.addUser(user);

        //登录
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);

        return map;
    }

    public Map<String,Object> login(String username, String password){
        Map<String,Object> map = new HashMap<String,Object>();
        if(StringUtils.isBlank(username)){
            map.put("msgname","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msgpwd","密码不能为空！");
            return map;
        }
        User user = userDao.selectByName(username);
        if(user == null){
            map.put("msg","用户名不存在！");
            return map;
        }

        if(!ToutiaoUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msgpwd","密码不正确！");
            return map;
        }
       //ticket
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        System.out.println("ticket:"+ticket);
        return map;
    }

     private String addLoginTicket(int userId){
         LoginTicket loginTicket = new LoginTicket();
         loginTicket.setUserId(userId);
         Date date = new Date();
         date.setTime(date.getTime()+1000*3600*24);
         loginTicket.setExpired(date);
         loginTicket.setStatus(0);;
         loginTicket.setTicket(UUID.randomUUID().toString().replace("-",""));
         loginTicketDao.addTicket(loginTicket);
         return loginTicket.getTicket();
     }
    public User getUser(int id){

        return userDao.selectById(id);
    }

    public void logout(String ticket){
        loginTicketDao.updateStatus(ticket,1);
    }
}
