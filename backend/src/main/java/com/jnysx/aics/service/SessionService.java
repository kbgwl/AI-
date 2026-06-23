package com.jnysx.aics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jnysx.aics.entity.Session;

import java.util.List;

/**
 * 会话Service接口
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
public interface SessionService extends IService<Session> {
    /**
     * 根据sessionId获取会话
     */
    Session getBySessionId(String sessionId);
    
    /**
     * 分配客服给会话
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @return 是否成功
     */
    boolean assignAgent(String sessionId, Long agentId);
    
    /**
     * 获取客服的活跃会话列表
     */
    List<Session> getActiveSessionsByAgentId(Long agentId);
    
    /**
     * 获取等待分配的会话列表
     */
    List<Session> getPendingSessions();
}
