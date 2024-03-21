package com.zecola.cleme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zecola.cleme.pojo.Category;
import com.zecola.cleme.pojo.Employee;

public interface CategoryService extends IService <Category>{
    public void remove(Long id);
}
