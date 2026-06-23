package com.jnysx.aics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jnysx.aics.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * Message Mapper接口
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
