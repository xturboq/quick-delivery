package com.zecola.cleme.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zecola.cleme.common.BaseContext;
import com.zecola.cleme.common.R;
import com.zecola.cleme.dto.OrdersDto;
import com.zecola.cleme.mapper.OrderDetailMapper;
import com.zecola.cleme.pojo.OrderDetail;
import com.zecola.cleme.pojo.Orders;
import com.zecola.cleme.pojo.ShoppingCart;
import com.zecola.cleme.service.OrderDetailService;
import com.zecola.cleme.service.OrderService;
import com.zecola.cleme.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 提交订单
     *
     * @param orders 用户提交的订单信息，以JSON格式通过请求体传入。
     * @return R<String> 返回一个结果对象，包含操作是否成功的信息。成功时，message字段为"用户下单成功"。
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        // 记录订单信息日志
        log.info("orders:{}", orders);
        // 提交订单到服务层处理
        orderService.submit(orders);
        // 返回成功响应
        return R.success("用户下单成功");
    }


    /**
     * 用户订单分页查询接口
     *
     * @param page     请求的页码
     * @param pageSize 每页显示的数量
     * @return R<Page>  返回一个包含订单信息的分页对象，其中R是响应封装类，Page是分页信息类
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize) {
        // 获取当前登录用户的ID
        Long userId = BaseContext.getCurrentId();
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 限定查询当前用户ID的订单数据
        queryWrapper.eq(userId != null, Orders::getUserId, userId);
        // 按订单时间降序排序
        queryWrapper.orderByDesc(Orders::getOrderTime);

        // 执行分页查询
        orderService.page(pageInfo, queryWrapper);

        // 处理查询结果，转换为订单详情分页列表
        List<OrdersDto> list = pageInfo.getRecords().stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            // 根据订单ID查询订单详情
            Long orderId = item.getId();
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> details = orderDetailService.list(wrapper);
            // 将订单信息复制到订单详情DTO中
            BeanUtils.copyProperties(item, ordersDto);
            // 设置订单详情列表
            ordersDto.setOrderDetails(details);
            return ordersDto;
        }).collect(Collectors.toList());

        // 将原始分页信息复制到转换后的分页DTO中
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        ordersDtoPage.setRecords(list);

        // 输出日志，用于调试和监控
        log.info("list:{}", list);

        // 返回订单详情的分页信息
        return R.success(ordersDtoPage);
    }

    /**
     * 处理再次下单的请求。
     * 将已下单的商品详情信息复制到购物车中。
     *
     * @param map 包含订单ID的Map对象，其中"id"键对应的值为订单ID。
     * @return 返回一个包含成功消息的R<String>对象。
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        // 从请求体中获取订单ID
        Long orderId = Long.valueOf(map.get("id"));
        // 使用条件构造器查询订单详情
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> details = orderDetailService.list(queryWrapper);
        // 获取当前用户ID，为购物车项设置用户关联
        Long userId = BaseContext.getCurrentId();
        // 将订单详情转换为购物车项，并批量保存到购物车中
        List<ShoppingCart> shoppingCarts = details.stream().map((item) ->{
            ShoppingCart shoppingCart = new ShoppingCart();
            // 复制订单详情属性到购物车项
            BeanUtils.copyProperties(item,shoppingCart);
            // 为购物车项设置用户ID和创建时间
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(shoppingCarts);
        // 执行成功，返回成功消息
        return R.success("再来一单操作成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long number, String beginTime, String endTime) {
        //获取当前id
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //按时间降序排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //订单号
        queryWrapper.eq(number != null, Orders::getId, number);
        //时间段，大于开始，小于结束
        queryWrapper.gt(!StringUtils.isEmpty(beginTime), Orders::getOrderTime, beginTime)
                .lt(!StringUtils.isEmpty(endTime), Orders::getOrderTime, endTime);
        orderService.page(pageInfo, queryWrapper);
        List<OrdersDto> list = pageInfo.getRecords().stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //获取orderId,然后根据这个id，去orderDetail表中查数据
            Long orderId = item.getId();
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> details = orderDetailService.list(wrapper);
            BeanUtils.copyProperties(item, ordersDto);
            //之后set一下属性
            ordersDto.setOrderDetails(details);
            return ordersDto;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }

    /**
     * 修改订单状态
     *
     * @param map 包含订单状态和订单ID的键值对，其中"status"对应订单状态，"id"对应订单ID
     * @return 返回一个结果对象，包含订单状态修改是否成功的信息
     */
    @PutMapping
    public R<String> changeStatus(@RequestBody Map<String, String> map) {
        // 从请求体中获取订单状态和ID
        int status = Integer.parseInt(map.get("status"));
        Long orderId = Long.valueOf(map.get("id"));
        // 记录修改订单状态的日志
        log.info("修改订单状态:status={status},id={id}", status, orderId);

        // 构建更新条件，设置订单状态
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId);
        updateWrapper.set(Orders::getStatus, status);

        // 执行订单状态更新操作
        orderService.update(updateWrapper);

        // 返回状态修改成功的消息
        return R.success("订单状态修改成功");
    }

}

