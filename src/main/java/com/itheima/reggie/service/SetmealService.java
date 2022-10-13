package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐 保存套餐和菜品关系
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐 同时需要删除套餐和菜品关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    //根据id查询套餐信息和菜品关联信息
    public SetmealDto getByIdWithDish(Long id);

    //更新套餐信息和菜品关联信息
    public void updateWithDish(SetmealDto setmealDto);
}
