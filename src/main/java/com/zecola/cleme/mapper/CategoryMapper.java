package com.zecola.cleme.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zecola.cleme.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
