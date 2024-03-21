package com.zecola.cleme.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zecola.cleme.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
