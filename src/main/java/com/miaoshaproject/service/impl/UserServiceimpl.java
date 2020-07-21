package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.miaoshaproject.dao.UserDOMapper;

import javax.annotation.Resource;

@Service
public class UserServiceimpl implements UserService {
    @Resource
    private UserDOMapper userDOMapper;
    @Autowired
    private ValidatorImpl validator;
    @Resource
    private UserPasswordDOMapper userPasswordDOMapper;
    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomapper获取到对应的用户dataobject
        UserDO userDo=userDOMapper.selectByPrimaryKey(id);
        if(userDo==null)
            return null;
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDo.getId());
        return convertFromDataObject(userDo,userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel==null)
            return ;
//        if(StringUtils.isEmpty(userModel.getName())
//                ||userModel.getAge()==null
//                ||userModel.getGender()==null
//                || StringUtils.isEmpty(userModel.getTelphone())){
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        UserDO userDO=convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException exception){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已经重复注册");
        }

        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO=convertPasswordFromModel(userModel);

        userPasswordDOMapper.insertSelective(userPasswordDO);
        return ;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptpassword) throws BusinessException {
        //通过用户的手机获取用户信息，比对用户信息内的密码和输入的密码相匹配
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO==null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel =convertFromDataObject(userDO,userPasswordDO);
        //比对用户信息内的密码和输入的密码相匹配
        if(!StringUtils.equals(encrptpassword,userModel.getEncrptPassword())){
            throw new BusinessException((EmBusinessError.USER_LOGIN_FAIL));
        }
        return userModel;

    }

    private  UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel==null)
            return null;
        UserPasswordDO userPasswordDO =new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        //BeanUtils.copyProperties(userModel,userPasswordDO);
        return  userPasswordDO;
    }
    private  UserDO convertFromModel(UserModel userModel){
        if(userModel==null)
            return null;
        UserDO userDO =new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return  userDO;
    }
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null)
            return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPasswordDO!=null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
