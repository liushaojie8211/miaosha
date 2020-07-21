package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderService {
    //1.通过前端url上传过来活动id，如何下单接口内校验对应的id是否属于对应商品且活动已经开始（使用）
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException;
}
