package com.zecola.cleme.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zecola.cleme.common.R;
import com.zecola.cleme.dto.DishDto;
import com.zecola.cleme.dto.SetmealDto;
import com.zecola.cleme.pojo.Category;
import com.zecola.cleme.pojo.Dish;
import com.zecola.cleme.pojo.Setmeal;
import com.zecola.cleme.service.CategoryService;
import com.zecola.cleme.service.SetmealDishService;
import com.zecola.cleme.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page:" + page + " pageSize:" + pageSize + "name:" + name);
        //构造分页构造器对象
        Page<Setmeal> pageInfo = new Page(page, pageSize);
        /*在查询套餐信息时, 只包含套餐的基本信息, 并不包含套餐的分类名称,
          所以在这里查询到套餐的基本信息后, 还需要根据分类ID(categoryId),
          查询套餐分类名称(categoryName)，并最终将套餐的基本信息及分类名称信息封装到SetmealDto*/
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        //排序，根据更新时间降序排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);
        //对象拷贝，把分页信息拷贝过来
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            //根据分类id查询数据
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * (批量)删除套餐信息
     * @param ids
     * @return
     */

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long>ids)
    {
        log.info("要删除的套餐id为:",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

}
