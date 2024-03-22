package com.zecola.cleme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.mapper.DishFlavorMapper;
import com.zecola.cleme.pojo.DishFlavor;
import com.zecola.cleme.service.DishFlavorService;
import org.springframework.stereotype.Service;


@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
