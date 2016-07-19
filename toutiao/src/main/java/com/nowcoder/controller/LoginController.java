package com.nowcoder.controller;


import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by zfx on 2016/7/7.
 */
@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @RequestMapping(path={"/reg/"})
    @ResponseBody
    public String reg(Model model, @RequestParam("username") String username,
                      @RequestParam("password")String password,
                      @RequestParam(value="rember",defaultValue = "0")int rememberme) {
        try{
            Map<String,Object> map = userService.register(username, password);
            if(map.containsKey("ticket")){
                return ToutiaoUtil.getJSONString(0,"注册成功！");
            }else{
                return ToutiaoUtil.getJSONString(1,map);
            }
        }catch(Exception e){
            logger.error("注册异常"+e.getMessage());
            return ToutiaoUtil.getJSONString(1,"注册异常");
        }

    }

    @RequestMapping(path={"/login/"})
    @ResponseBody
    public String login(Model model, @RequestParam("username") String username,
                        @RequestParam("password")String password,
                        @RequestParam(value="rember",defaultValue = "0")int rememberme,
                        HttpServletResponse response) {
        try{
            Map<String,Object> map = userService.login(username, password);
            if(map.containsKey("ticket")){
                Cookie cookie = new Cookie("ticket",map.get("ticket").toString());

                cookie.setPath("/");//全栈有效
                if(rememberme>0){
                    cookie.setMaxAge(3600*24*5);//cookie有效时间
                }
                response.addCookie(cookie);
                System.out.println("cookie:"+cookie);
                return ToutiaoUtil.getJSONString(0,"登录成功！");
            }else{
                return ToutiaoUtil.getJSONString(1,map);
            }
        }catch(Exception e){
            logger.error("登录异常"+e.getMessage());
            return ToutiaoUtil.getJSONString(1,"登录异常");
        }

    }

    @RequestMapping(path={"/logout/"})
    public String logout(@CookieValue("ticket") String ticket){
        System.out.println(ticket);
        userService.logout(ticket);

        return "redirect:/";

    }
}
