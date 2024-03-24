package com.zecola.cleme.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.mapper.OrderDetailMapper;
import com.zecola.cleme.pojo.OrderDetail;
import com.zecola.cleme.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}