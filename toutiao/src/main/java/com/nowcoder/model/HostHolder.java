package com.nowcoder.model;

import org.springframework.stereotype.Component;

/**
 * Created by zfx on 2016/7/8.
 */
@Component
public class HostHolder {
    private static  ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser(){
        return users.get();
    }

    public void setUsers(User user){
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
