package com.zecola.cleme.dto;

import com.zecola.cleme.pojo.Dish;
import com.zecola.cleme.pojo.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
