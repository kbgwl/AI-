package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.Message;
import java.util.List;

/**
 * 消息 Service 接口
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
public interface MessageService extends IService<Message> {
    /**
     * 获取会话最近 N 条消息
     */
    List<Message> getRecentMessages(String sessionId, int limit);
}