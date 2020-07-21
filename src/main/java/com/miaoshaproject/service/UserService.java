package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     *
     * @param telphone 用户注册手机
     * @param encrptpassword 用户加密密码
     * @throws BusinessException
     */
    UserModel validateLogin(String telphone, String encrptpassword) throws BusinessException;
}
