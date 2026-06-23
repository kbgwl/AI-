package com.jnysx.aics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jnysx.aics.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper接口
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
