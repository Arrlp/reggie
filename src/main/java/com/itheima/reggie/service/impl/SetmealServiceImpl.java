package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

   @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐 保存套餐和菜品关系
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息 操作setmeal表
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //当前setmealId还没有值 所以需要每个setmealDish都设置一下
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品关联信息 操作setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐 同时需要删除套餐和菜品关联数据
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {
//        select count(*) from setmeal where id in (?,?,?) and status = 1
        //通过id 判断status是否为0（停售状态)
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        //this.count指ServiceImpl框架中的方法
        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除 抛出业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除 先删除套餐表中的数据--setmeal
        this.removeByIds(ids);

//        delete from setmeal_dish where setmealId id in (?,?,?)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据--setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);

    }

    /**
     * 根据id查询套餐信息dish和对应的菜品关联信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //查询套餐基本信息 setmeal表
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        //把除菜品外的其他信息先赋值到setmealDto中
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询菜品关联信息 通过套餐id查询setmeal_dish表中对应的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);

        //将菜品信息赋值给setmealDto
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    /**
     * 修改套餐信息
     *  @param setmealDto
     * @return
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //修改setmeal表
        this.updateById(setmealDto);

        //通过id查询setmeal_dish表中所有关联的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        //删除原套餐关联的菜品信息
        setmealDishService.remove(queryWrapper);

        //获取修改后的菜品关联信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //设置菜品信息中的setmealId
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //新增修改后的菜品关联信息
        setmealDishService.saveBatch(setmealDishes);
    }
}
