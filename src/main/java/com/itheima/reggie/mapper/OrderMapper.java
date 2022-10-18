package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Orders;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
