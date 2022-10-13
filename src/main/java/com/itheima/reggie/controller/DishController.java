package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.InsertProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增菜品
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        Page<Dish>  pageInfo = new Page<>(page,pageSize);
        Page<DishDto>  dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);

        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list =records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //拷贝其他属性值
            BeanUtils.copyProperties(item,dishDto);
            //通过分类id去查分类表中所属对象 然后获取对应分类名称
            // 再赋给dishDto对象
            Long categoryId = item.getCategoryId();//分类id
            //根据id 查询分类对象
            Category category = categoryService.getById(categoryId);
           if(category != null){
               String categoryName = category.getName();
               dishDto.setCategoryName(categoryName);
           }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 修改数据--回显数据
     * 根据id查询菜品信息dish和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 保存修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }

    /**
     * 批量/单个删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){

        for (Long id : ids) {
            dishService.deleteWithFlavor(id);
        }
        return R.success("删除菜品成功");
    }

    /**
     *批量/单个修改菜品状态（启售/停售）
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status,Long[] ids){
        Dish dish = null;

        if(status == 0){
            for (Long id: ids) {
                dish = dishService.getById(id);
                dish.setStatus(0);
                dishService.updateById(dish);
            }
        }else{
            for (Long id: ids) {
                dish = dishService.getById(id);
                dish.setStatus(1);
                dishService.updateById(dish);
            }
        }
        return R.success("已修改");
    }


    /**
     * 根据条件查询菜品--回显到新增套餐页面
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //查询启售状态的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }
}
