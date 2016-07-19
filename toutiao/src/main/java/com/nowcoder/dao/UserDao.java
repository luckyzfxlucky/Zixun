package com.nowcoder.dao;

import com.nowcoder.model.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * Created by zfx on 2016/7/5.
 */
@Mapper
@Repository
public interface UserDao {
    String TABLE_NAME = "user";
    String INSERT_FIELDS = "name,password,salt,head_url";
    String SELECT_FIELDS = "Id,name,password,salt,head_url";

     @Insert({"insert into",TABLE_NAME,"(", INSERT_FIELDS,
             ")values(#{name},#{password},#{salt},#{headUrl})"})
   /* @Insert({"insert into ",TABLE_NAME, "(", INSERT_FIELDS, ")values
            (#{name},#{password},#{salt},#{headUrl})"})*/
    int addUser(User user);

    @Select({"select" ,SELECT_FIELDS ,"from" ,TABLE_NAME ,"where id=#{id}"})
    User selectById(int id);

    @Select({"select" ,SELECT_FIELDS ,"from" ,TABLE_NAME ,"where name=#{name}"})
    User selectByName(String name);

    @Update({"update",TABLE_NAME,"set password=#{password} where id=#{id}"})
    void updatePassword(User user);

    @Delete({"delete from",TABLE_NAME,"where id=#{id}"})
    void deleteById(int id);

}
