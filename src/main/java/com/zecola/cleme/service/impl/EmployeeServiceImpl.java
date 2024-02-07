package com.zecola.cleme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zecola.cleme.mapper.EmployeeMapper;
import com.zecola.cleme.pojo.Employee;
import com.zecola.cleme.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee>implements EmployeeService {

}
