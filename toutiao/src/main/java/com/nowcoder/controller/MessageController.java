package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zfx on 2016/7/18.
 */
@Controller
public class MessageController {
    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService;
    @Autowired
    HostHolder hostHolder;


    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @RequestMapping(path = "/msg/list",method = {RequestMethod.GET})
    public String list(Model model){
        try{
            int localUserId = hostHolder.getUser().getId();
            List<ViewObject> conversations = new ArrayList<ViewObject>();
            List<Message> conversationList = messageService.getConversationList(localUserId,0,10);
            for(Message msg : conversationList){
                ViewObject vo = new ViewObject();
                vo.set("conversation",msg);
                int targetId = msg.getFromId()==localUserId ? msg.getToId() : msg.getFromId();
                User user = userService.getUser(targetId);
                vo.set("user",user);
                vo.set("unread",messageService.getConversationUnreadCount(localUserId,msg.getConversationId()));
                conversations.add(vo);
            }
            model.addAttribute("conversations",conversations);
        }catch(Exception e){
            logger.error("获取站内信失败！"+e.getMessage());
        }
        return "letter";
    }

    @RequestMapping(path = "/msg/detail",method = {RequestMethod.GET})
    public String detail(Model model,@RequestParam("conversationId")String conversationId){
        try {
            List<Message> conversationList = messageService.getConversationDetail(conversationId, 0, 10);
            List<ViewObject> messages = new ArrayList<ViewObject>();
            for(Message  msg : conversationList){
               ViewObject vo = new ViewObject();
                vo.set("message",msg);
                User user = userService.getUser(msg.getFromId());
                if(user == null){
                    continue;
                }
                vo.set("headUrl",user.getHeadUrl());
                vo.set("userId",user.getId());
                messages.add(vo);
                model.addAttribute("message",messages);
            }
            model.addAttribute("messages",messages);
        }catch(Exception e){
            logger.error("获取详情页失败！"+e.getMessage());
        }
        return "letterDetail";
    }

    @RequestMapping(path = "/msg/addMessage",method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("fromId") int fromId,
                             @RequestParam("toId") int toId,
                             @RequestParam("content") String content){
        try{
            Message message = new Message();
            message.setFromId(fromId);
            message.setToId(toId);
            message.setContent(content);
            message.setCreatedDate(new Date());
            message.setConversationId(fromId<toId ? String.format("%d_%d",fromId,toId) : String.format("%d_%d",toId,fromId));
            messageService.addMessage(message);
            return ToutiaoUtil.getJSONString(message.getId());
        }catch(Exception e){
            logger.error("添加message失败！"+e.getMessage());
            return ToutiaoUtil.getJSONString(1,"插入评论失败！");
        }
    }
}
