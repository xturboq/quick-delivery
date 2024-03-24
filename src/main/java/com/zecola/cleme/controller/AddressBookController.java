package com.zecola.cleme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zecola.cleme.common.BaseContext;
import com.zecola.cleme.common.CustomException;
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

    /**
     * 获取当前用户的默认地址
     *
     * @return R<AddressBook> 返回一个包含默认地址信息的响应对象，如果找不到默认地址，则返回空的响应对象。
     */
    @GetMapping("/default")
    public R<AddressBook> defaultAddress() {
        // 获取当前用户id
        Long userId = BaseContext.getCurrentId();
        // 条件构造器，用于查询当前用户且标记为默认的地址信息
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        // 根据用户id进行查询条件设置，如果用户id存在
        queryWrapper.eq(userId != null, AddressBook::getUserId, userId);
        // 设置查询默认地址的条件
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        // 从地址簿服务中根据条件查询一个地址信息，返回第一个匹配的结果
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        // 返回查询结果，如果存在则包含在成功的响应对象中，否则为空响应对象
        return R.success(addressBook);
    }

    /**
     * 根据ID获取地址薄信息。
     *
     * @param id 地址薄的唯一标识符。
     * @return 返回一个包含地址薄信息的响应对象。如果地址薄不存在，则抛出自定义异常。
     */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id) {
        // 通过ID从地址薄服务中获取地址信息
        AddressBook addressBook = addressBookService.getById(id);
        // 如果地址信息不存在，则抛出定制异常
        if (addressBook == null){
            throw new CustomException("地址信息不存在");
        }
        // 返回成功的响应，包含地址薄信息
        return R.success(addressBook);
    }

    /**
     * 更新地址信息
     *
     * @param addressBook 用户提交的地址信息对象，通过RequestBody接收前端传来的JSON数据
     * @return 返回操作结果，如果成功更新地址信息，则返回成功消息；如果地址信息为空，则抛出自定义异常
     */
    @PutMapping
    public R<String> updateAdd(@RequestBody AddressBook addressBook) {
        // 检查传入的地址信息是否为空
        if (addressBook == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        // 调用服务层方法，根据传入的地址信息更新数据库中的记录
        addressBookService.updateById(addressBook);
        // 返回成功响应，通知前端地址修改成功
        return R.success("地址修改成功");
    }


    /**
     * 删除地址信息
     *
     * @param id 地址信息的ID，必须为Long类型，不能为空
     * @return R<String> 返回操作结果，成功则返回成功消息，失败则抛出异常
     */
    @DeleteMapping
    public R<String> deleteAdd(@RequestParam("ids") Long id) {
        // 检查ID是否为空
        if (id == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        // 根据ID查询地址信息，确认该地址存在
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        // 删除地址信息
        addressBookService.removeById(id);
        // 返回删除成功的消息
        return R.success("地址删除成功");
    }


}