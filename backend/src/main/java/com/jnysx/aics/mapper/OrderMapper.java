package com.jnysx.aics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jnysx.aics.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
