package com.zecola.cleme.dto;


import com.zecola.cleme.pojo.OrderDetail;
import com.zecola.cleme.pojo.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
