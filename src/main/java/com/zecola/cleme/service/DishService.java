package com.zecola.cleme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zecola.cleme.dto.DishDto;
import com.zecola.cleme.pojo.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询菜品信息和口味信息
    public  DishDto getByIdWithFlavor(Long id);
    //更新菜品信息，同时更新对应的口味数据
    public void updateWithFlavor(DishDto dishDto);
}
