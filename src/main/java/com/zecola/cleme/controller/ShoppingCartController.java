package com.zecola.cleme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.zecola.cleme.common.BaseContext;
import com.zecola.cleme.common.R;
import com.zecola.cleme.pojo.ShoppingCart;
import com.zecola.cleme.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     *
     * @param shoppingCart 购物车对象，包含菜品或套餐id，以及数量等信息
     * @return 返回购物车实体，包含添加或更新后的购物车信息
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart={}", shoppingCart);
        // 获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        // 设置当前用户id
        shoppingCart.setUserId(currentId);
        // 获取当前菜品或套餐id
        Long dishId = shoppingCart.getDishId();
        // 条件构造器，用于查询购物车中是否已存在当前菜品或套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 判断添加的是菜品还是套餐，设置相应的查询条件
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        // 查询当前菜品或套餐是否在购物车中
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null) {
            // 如果已存在，数量加1并更新
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            // 如果不存在，设置创建时间并添加到购物车，数量默认为1
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            // 为保持结果一致性，将新添加的购物车对象赋值给cartServiceOne
            cartServiceOne = shoppingCart;
        }
        // 返回操作结果
        return R.success(cartServiceOne);
    }

    /**
     * 获取当前用户购物车列表
     *
     * @return R<List<ShoppingCart>> 返回类型为封装了购物车列表的R对象，其中R是自定义的结果封装类，List<ShoppingCart>是购物车列表。
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        // 创建查询条件，筛选出当前用户的所有购物车项
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId(); // 获取当前用户ID
        queryWrapper.eq(ShoppingCart::getUserId, userId); // 根据用户ID进行查询
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper); // 查询并获取购物车列表
        return R.success(shoppingCarts); // 返回购物车列表的成功结果
    }

    /**
     * 清空购物车
     *
     * 本接口不需要接收任何参数，通过当前登录用户的ID，查询并删除该用户的购物车记录。
     *
     * @return R<String> 返回操作结果，成功则返回包含成功消息的R对象。
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        // 构建查询条件，查询当前用户ID的购物车记录
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(userId != null, ShoppingCart::getUserId, userId);
        // 调用服务层方法，执行购物车记录的删除操作
        shoppingCartService.remove(queryWrapper);
        // 返回成功结果
        return R.success("成功清空购物车");
    }

    /**
     * 从购物车中减少商品或套餐数量。
     *
     * @param shoppingCart 包含要操作的商品ID或套餐ID以及当前数量的对象。
     * @return 返回操作结果，如果成功则包含更新后的购物车信息，如果失败则返回错误信息。
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        // 条件构造器，用于查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件，只查询当前用户ID的购物车
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        // 针对菜品ID进行的操作逻辑
        if (dishId != null) {
            // 根据dishId查询购物车中的菜品数据
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            ShoppingCart dishCart = shoppingCartService.getOne(queryWrapper);
            // 将查询到的菜品数量减1
            dishCart.setNumber(dishCart.getNumber() - 1);
            Integer currentNum = dishCart.getNumber();
            // 判断数量后进行相应的更新或删除操作
            if (currentNum > 0) {
                // 数量大于0则更新
                shoppingCartService.updateById(dishCart);
            } else if (currentNum == 0) {
                // 数量等于0则删除
                shoppingCartService.removeById(dishCart.getId());
            }
            return R.success(dishCart);
        }

        // 针对套餐ID进行的操作逻辑
        if (setmealId != null) {
            // 根据setmealId查询购物车中的套餐数据
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
            ShoppingCart setmealCart = shoppingCartService.getOne(queryWrapper);
            // 将查询到的套餐数量减1
            setmealCart.setNumber(setmealCart.getNumber() - 1);
            Integer currentNum = setmealCart.getNumber();
            // 判断数量后进行相应的更新或删除操作
            if (currentNum > 0) {
                // 数量大于0则更新
                shoppingCartService.updateById(setmealCart);
            } else if (currentNum == 0) {
                // 数量等于0则删除
                shoppingCartService.removeById(setmealCart.getId());
            }
            return R.success(setmealCart);
        }
        // 若既没有商品ID也没有套餐ID，则返回操作异常
        return R.error("操作异常");
    }
}
