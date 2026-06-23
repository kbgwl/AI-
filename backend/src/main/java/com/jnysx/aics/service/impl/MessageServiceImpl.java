package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.Message;
import com.jnysx.aics.mapper.MessageMapper;
import com.jnysx.aics.service.MessageService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 消息 Service 实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    
    @Override
    public List<Message> getRecentMessages(String sessionId, int limit) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getSessionId, sessionId)
               .orderByAsc(Message::getCreateTime)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }
}
