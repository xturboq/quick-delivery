package com.zecola.cleme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zecola.cleme.common.R;
import com.zecola.cleme.dto.DishDto;
import com.zecola.cleme.pojo.*;
import com.zecola.cleme.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("新增菜品，dishDto:{}", dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");

    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page:" + page + " pageSize:" + pageSize + "name:" + name);
        //构造分页构造器
        Page<Dish> pageinfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageinfo, queryWrapper);

        BeanUtils.copyProperties(pageinfo, dishDtoPage, "records");
        List<Dish> records = pageinfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto>get(@PathVariable Long id){
        log.info("根据id查询菜品信息和对应的口味信息，id为：{}",id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);


        return R.success(dishDto);
    }



    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("修改菜品，dishDto:{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功");

    }

    /**
     * (批量)删除菜品及其口味
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long>ids) {
        log.info("删除菜品，ids:{}", ids);
        dishService.removeWithFlavor(ids);
        return R.success("删除菜品成功");

    }

    /**
     * (批量)停售菜品信息
     * @param ids
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateMulStatus(@PathVariable Integer status, Long[] ids) {
        List<Long>list = Arrays.asList(ids);
        log.info("需要修改菜品状态，ids:{}", list);
        //设置条件构造器
        LambdaUpdateWrapper<Dish> updatequeryWrapper = new LambdaUpdateWrapper<>();
        updatequeryWrapper.set(Dish::getStatus, status).in(Dish::getId, list);
        dishService.update(updatequeryWrapper);
        return R.success("修改菜品状态成功");
    }

    /**
     * 获取菜品数据
     *
     * @param dish 用于查询条件的菜品实体，其中categoryId和status可选作为查询条件
     * @return 返回查询到的菜品列表的结果，成功返回包含列表的Result对象
     */
    @GetMapping("/list")
    public R<List<Dish>> get(Dish dish) {
        // 创建条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 根据传进来的categoryId查询，如果categoryId不为空
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 只查询状态为1的菜品（启售菜品）
        queryWrapper.eq(Dish::getStatus, 1);
        // 对查询结果进行排序，首先按排序字段升序，然后按更新时间降序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        // 执行查询并获取结果
        List<Dish> list = dishService.list(queryWrapper);
        // 将查询结果封装成成功结果并返回
        return R.success(list);
    }
}