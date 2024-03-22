package com.zecola.cleme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zecola.cleme.dto.DishDto;
import com.zecola.cleme.pojo.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据
    public void saveWithFlavor(DishDto dishDto);
}
