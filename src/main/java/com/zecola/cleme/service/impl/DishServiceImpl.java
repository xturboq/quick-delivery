package com.zecola.cleme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.common.R;
import com.zecola.cleme.dto.DishDto;
import com.zecola.cleme.mapper.DishMapper;
import com.zecola.cleme.pojo.Dish;
import com.zecola.cleme.pojo.DishFlavor;
import com.zecola.cleme.pojo.Setmeal;
import com.zecola.cleme.pojo.SetmealDish;
import com.zecola.cleme.service.DishFlavorService;
import com.zecola.cleme.service.DishService;
import com.zecola.cleme.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

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

    /**
     * 根据id查询菜品信息，包含菜品口味
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询对应菜品的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);


        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应的口味数据--dish_flavor的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加新的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();

        //这段代码有待看看
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }



    /**
     * 删除菜品，同时需要删除菜品和对应的口味的关联数据
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithFlavor(List<Long> ids) {
        //查询菜品状态，是否可以删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);

        if (this.list(queryWrapper).size() > 0){
            throw new RuntimeException("菜品正在售卖中，不能删除");
        }
        //查询菜品是否包含在套餐中，是否可以删除
        LambdaQueryWrapper<SetmealDish>setmealqueryWrapper = new LambdaQueryWrapper<>();
        setmealqueryWrapper.in(SetmealDish::getDishId,ids);
        int count = setmealDishService.count(setmealqueryWrapper);

        if (count != 0){
            throw new RuntimeException("菜品包含在套餐中，不能删除");
        }
        //删除菜品信息
        super.removeByIds(ids);

        //删除菜品口味信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lambdaQueryWrapper);

    }
}
