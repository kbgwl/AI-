package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.Session;
import com.jnysx.aics.mapper.SessionMapper;
import com.jnysx.aics.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话Service实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
    
    @Override
    public Session getBySessionId(String sessionId) {
        LambdaQueryWrapper<Session> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Session::getSessionId, sessionId)
               .eq(Session::getDeleted, 0)
               .orderByDesc(Session::getCreateTime)
               .last("LIMIT 1");
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional
    public boolean assignAgent(String sessionId, Long agentId) {
        Session session = getBySessionId(sessionId);
        if (session == null) {
            return false;
        }
        session.setAgentId(agentId);
        session.setStatus(2); // 人工服务中
        session.setMode(1); // AI+人工辅助
        session.setAgentJoinedAt(LocalDateTime.now());
        return this.updateById(session);
    }
    
    @Override
    public List<Session> getActiveSessionsByAgentId(Long agentId) {
        LambdaQueryWrapper<Session> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Session::getAgentId, agentId)
               .eq(Session::getStatus, 2) // 人工服务中
               .eq(Session::getDeleted, 0)
               .orderByDesc(Session::getAgentJoinedAt);
        return this.list(wrapper);
    }
    
    @Override
    public List<Session> getPendingSessions() {
        LambdaQueryWrapper<Session> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Session::getStatus, 1) // 等待人工
               .eq(Session::getDeleted, 0)
               .orderByAsc(Session::getCreateTime);
        return this.list(wrapper);
    }
}