package com.zecola.cleme.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zecola.cleme.common.R;
import com.zecola.cleme.dto.DishDto;
import com.zecola.cleme.pojo.Category;
import com.zecola.cleme.pojo.Dish;
import com.zecola.cleme.service.CategoryService;
import com.zecola.cleme.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.ClientInfoStatus;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;


    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增分类信息：{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */

    @GetMapping("/page")
    public R<Page>page(int page,int pageSize){
        Page pageinfo = new Page(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageinfo,queryWrapper);

        return R.success(pageinfo);
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类，id为：{}",id);
        //待完善
        categoryService.remove(id);
        //categoryService.removeById(id);

        return R.success("分类信息删除成功");
    }


    /**
     * 修改分类信息
     * @param category
     * @return
     */

    @PutMapping
    public  R<Category>update(@RequestBody Category category){
        log.info("修改分类信息：{}",category.toString());
        categoryService.updateById(category);
        return R.success(category);
    }

    @GetMapping("/list")
    public R <List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);

    }



}

