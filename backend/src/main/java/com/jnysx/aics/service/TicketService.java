package com.jnysx.aics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.entity.Ticket;
import com.jnysx.aics.mapper.TicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class TicketService {
    
    @Autowired
    private TicketMapper ticketMapper;

    private static final DateTimeFormatter TICKET_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();
    
    private String generateTicketNo() {
        return "WO" + LocalDateTime.now().format(TICKET_FMT)
               + String.format("%03d", RANDOM.nextInt(1000));
    }

    /**
     * 创建工单
     */
    public Ticket createTicket(Ticket ticket) {
        ticket.setTicketNo(generateTicketNo());
        ticket.setStatus("pending");
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        ticket.setDeleted(0);
        
        ticketMapper.insert(ticket);
        return ticket;
    }
    
    /**
     * 分页查询工单列表
     */
    public Page<Ticket> listTickets(Integer page, Integer size, String status, String type, Integer priority) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Ticket::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Ticket::getTicketType, type);
        }
        if (priority != null) {
            wrapper.eq(Ticket::getPriority, priority);
        }
        wrapper.orderByDesc(Ticket::getCreateTime);
        
        return ticketMapper.selectPage(new Page<>(page, size), wrapper);
    }
    
    /**
     * 根据 ID 获取工单
     */
    public Ticket getById(Long id) {
        return ticketMapper.selectById(id);
    }
    
    /**
     * 更新工单
     */
    public Ticket updateTicket(Ticket ticket) {
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        return ticket;
    }
    
    /**
     * 删除工单（逻辑删除）
     */
    public void deleteTicket(Long id) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket != null) {
            ticket.setDeleted(1);
            ticketMapper.updateById(ticket);
        }
    }

    /**
     * Auto create ticket
     */
    public Ticket autoCreateTicket(String sessionId, String userId, String type, String title, String description, Integer priority, String intentCode) {
        Ticket ticket = new Ticket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setSessionId(sessionId);
        ticket.setUserId(userId);
        ticket.setTicketType(type);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority != null ? priority : 2);
        ticket.setIntentCode(intentCode);
        ticket.setStatus("pending");
        ticket.setCreateTime(java.time.LocalDateTime.now());
        ticket.setUpdateTime(java.time.LocalDateTime.now());
        ticket.setDeleted(0);
        ticketMapper.insert(ticket);
        return ticket;
    }


    /**
     * Count tickets
     */
    public long count(LambdaQueryWrapper<Ticket> wrapper) {
        return ticketMapper.selectCount(wrapper);
    }
}
