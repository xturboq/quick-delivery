package com.zecola.cleme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.mapper.ShoppingCartMapper;
import com.zecola.cleme.pojo.ShoppingCart;
import com.zecola.cleme.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
