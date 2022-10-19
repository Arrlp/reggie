package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("套餐Dto接口")
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;

}
