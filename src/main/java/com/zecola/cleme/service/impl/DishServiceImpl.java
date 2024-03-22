package com.zecola.cleme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.dto.DishDto;
import com.zecola.cleme.mapper.DishMapper;
import com.zecola.cleme.pojo.Dish;
import com.zecola.cleme.pojo.DishFlavor;
import com.zecola.cleme.service.DishFlavorService;
import com.zecola.cleme.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品信息到菜品表
        this.save(dishDto);
        //保存菜品口味数据到菜品口味表dish

        //菜品口味，添加id
        Long dishid = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.forEach(flavor -> {
            flavor.setDishId(dishid);
            dishFlavorService.save(flavor);
        });

    }
}
