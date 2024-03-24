package com.zecola.cleme.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zecola.cleme.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
