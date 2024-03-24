package com.zecola.cleme.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zecola.cleme.pojo.Orders;

public interface OrderService extends IService<Orders> {

    void submit(Orders orders);
}
