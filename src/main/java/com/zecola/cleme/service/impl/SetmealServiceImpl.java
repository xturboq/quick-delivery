package com.zecola.cleme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.common.CustomException;
import com.zecola.cleme.dto.SetmealDto;
import com.zecola.cleme.mapper.SetmealMapper;
import com.zecola.cleme.pojo.Setmeal;
import com.zecola.cleme.pojo.SetmealDish;
import com.zecola.cleme.service.SetmealDishService;
import com.zecola.cleme.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    //新增套餐，同时保存套餐和菜品的新增关心
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息,操作setmeal表，执行insert操作
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品的关联信息，操作setmeal_dish表，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    //删除套餐，同时删除套餐和菜品的关联数据
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐在售状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);

        log.info("查询套餐在售状态，状态(在售)计数等于" + count);
        if (count > 0) {
            //不可删除
            throw new CustomException("该套餐正在使用中，不可删除");
        }
        //进行删除
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3);
        LambdaQueryWrapper<SetmealDish> setmealDishqueryWrapper = new LambdaQueryWrapper<>();
        setmealDishqueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishqueryWrapper);
    }
}
