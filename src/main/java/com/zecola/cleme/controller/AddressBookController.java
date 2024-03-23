package com.zecola.cleme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zecola.cleme.common.BaseContext;
import com.zecola.cleme.common.R;
import com.zecola.cleme.pojo.AddressBook;
import com.zecola.cleme.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 获取地址簿列表
     *
     * @param addressBook 查询条件对象，可选参数，用于构建查询条件
     * @return 返回地址簿列表的响应对象，其中包含查询到的地址簿列表
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        // 设置当前用户的ID到查询条件对象中
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook={}", addressBook);

        // 使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        // 如果用户ID已设置，则添加用户ID的查询条件
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        // 按更新时间降序排序
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        // 根据构建的查询条件查询地址簿列表
        List<AddressBook> addressBooks = addressBookService.list(queryWrapper);
        // 返回查询结果的成功响应
        return R.success(addressBooks);
    }

    /**
     * 添加地址到地址薄
     *
     * @param addressBook 用户提交的地址信息
     * @return 添加成功的地址信息以及操作状态
     */
    @PostMapping
    public R<AddressBook> addAddress(@RequestBody AddressBook addressBook) {
        // 设置当前操作用户的ID
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        // 保存地址信息到数据库
        addressBookService.save(addressBook);
        // 返回添加成功的状态及地址信息
        return R.success(addressBook);
    }


    /**
     * 设置默认地址
     *
     * @param addressBook 用户地址信息
     * @return 返回更新后的地址信息
     */
    @PutMapping("/default")
    public R<AddressBook> setDefaultAddress(@RequestBody AddressBook addressBook) {
        // 设置当前用户id
        addressBook.setUserId(BaseContext.getCurrentId());
        // 使用条件构造器更新用户的所有地址的is_default为0
        LambdaUpdateWrapper<AddressBook> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(queryWrapper);
        // 将当前地址的is_default设置为1
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }
}