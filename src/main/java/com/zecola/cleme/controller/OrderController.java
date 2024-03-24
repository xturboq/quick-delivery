package com.zecola.cleme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        //获取order_id
        Long orderId = Long.valueOf(map.get("id"));
        //条件构造器
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        //查询订单的口味细节数据
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> details = orderDetailService.list(queryWrapper);
        //获取用户id，待会需要set操作
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCarts = details.stream().map((item) ->{
            ShoppingCart shoppingCart = new ShoppingCart();
            //Copy对应属性值
            BeanUtils.copyProperties(item,shoppingCart);
            //设置一下userId
            shoppingCart.setUserId(userId);
            //设置一下创建时间为当前时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //加入购物车
        shoppingCartService.saveBatch(shoppingCarts);
        return R.success("喜欢吃就再来一单吖~");
    }

}

