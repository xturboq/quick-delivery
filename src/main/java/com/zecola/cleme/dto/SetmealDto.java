package com.zecola.cleme.dto;

import com.zecola.cleme.pojo.Setmeal;
import com.zecola.cleme.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
