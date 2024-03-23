package com.zecola.cleme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zecola.cleme.dto.SetmealDto;
import com.zecola.cleme.pojo.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal>{
    //新增套餐，同时保存套餐和菜品的新增关系
    public void saveWithDish(SetmealDto setmealDto);
    //删除套餐，同时删除套餐和菜品的关联数据
    public void removeWithDish(List<Long> ids);
}
