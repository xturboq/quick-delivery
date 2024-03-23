package com.zecola.cleme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.mapper.UserMapper;
import com.zecola.cleme.pojo.User;
import com.zecola.cleme.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl <UserMapper, User>implements UserService {
}
