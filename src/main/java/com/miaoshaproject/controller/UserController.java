package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.responce.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Handler;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{
    @Resource
    private UserService userService;
    @Resource
    private  HttpServletRequest httpServletRequest;
    //用户登陆接口
    @RequestMapping(value="/login",method={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String  telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)|| StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"参数错误");
        }
        //校验用户登陆是否合法
        UserModel userModel = userService.validateLogin(telphone,this.EncodeByMd5(password));

        //将凭证加入到用户登陆成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        return CommonReturnType.create(null);
    }

    //用户注册接口
    @RequestMapping(value="/register",method={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public  CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                      @RequestParam(name="otpCode")String otpCode,
                                      @RequestParam(name="name")String name,
                                      @RequestParam(name="gender")Integer gender,
                                      @RequestParam(name="age")Integer age,
                                      @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //验证手机号和对应的optcode相符合
        String inSessionOptCode= (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOptCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //用户注册流程

        UserModel userModel=new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setRegisterMode("byphone");
        userModel.setTelphone(telphone);
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userService.register(userModel);

        return CommonReturnType.create(null);
    }
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64En =new BASE64Encoder();
        String newStr = base64En.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }
    //用户获取otp短信接口
    @RequestMapping(value="/getotp",method={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone){
        //按照一定的规则生成otp验证码
        Random random =new Random();
        int randomint=random.nextInt(99999);
        randomint+=10000;
        String optcode=String.valueOf(randomint);
        //将otp验证码同对应用户的手机号关联
        //使用httpsession的方式绑定
        httpServletRequest.getSession().setAttribute(telphone, optcode);

        //将otp验证码通过短信通道发送给用户（省略）
        System.out.println("telphone= "+telphone+" & optcode = "+optcode);
        return CommonReturnType.create(null);

    }
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service服务获取对应的id的用户对象并返回给前端
        UserModel userModel= userService.getUserById(id);
        //若获取的用户信息不存在
        if(userModel == null){
            userModel.setEncrptPassword("12333");
            //throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //将核心领域模型用户对象转换为可供UI使用的UIobject
        UserVO userVO = convertFromModel(userModel);
        return  CommonReturnType.create(userVO);
    }
    private UserVO convertFromModel(UserModel userModel){
        if(userModel==null)
            return null;
        UserVO userVO =new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
