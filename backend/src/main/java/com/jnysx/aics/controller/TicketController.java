package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.Ticket;
import com.jnysx.aics.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/tickets")
@RequireLogin
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    /**
     * 创建工单
     */
    @PostMapping
    public Result<Ticket> createTicket(@RequestBody Ticket ticket) {
        try {
            Ticket created = ticketService.createTicket(ticket);
            return Result.ok(created);
        } catch (Exception e) {
            return Result.fail("工单创建失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取工单列表
     */
    @GetMapping
    public Result<Page<Ticket>> listTickets(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer priority
    ) {
        Page<Ticket> ticketPage = ticketService.listTickets(page, size, status, type, priority);
        return Result.ok(ticketPage);
    }
    
    /**
     * 获取工单详情
     */
    @GetMapping("/{id}")
    public Result<Ticket> getTicket(@PathVariable Long id) {
        Ticket ticket = ticketService.getById(id);
        if (ticket == null) {
            return Result.fail("工单不存在");
        }
        return Result.ok(ticket);
    }
    
    /**
     * 更新工单
     */
    @PutMapping("/{id}")
    public Result<Ticket> updateTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
        ticket.setId(id);
        Ticket updated = ticketService.updateTicket(ticket);
        return Result.ok(updated);
    }
    
    /**
     * 分配工单
     */
    @PutMapping("/{id}/assign")
    public Result<Ticket> assignTicket(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            Ticket ticket = ticketService.getById(id);
            if (ticket == null) {
                return Result.fail("工单不存在");
            }
            String agentId = (String) data.get("agentId");
            String agentName = (String) data.get("agentName");
            ticket.setAssigneeId(agentId);
            ticket.setAssigneeName(agentName);
            ticket.setStatus("processing");
            ticketService.updateTicket(ticket);
            return Result.ok("工单分配成功", null);
        } catch (Exception e) {
            return Result.fail("分配失败：" + e.getMessage());
        }
    }
    
    /**
     * 解决工单
     */
    @PutMapping("/{id}/resolve")
    public Result<Ticket> resolveTicket(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            Ticket ticket = ticketService.getById(id);
            if (ticket == null) {
                return Result.fail("工单不存在");
            }
            String solution = (String) data.get("solution");
            ticket.setSolution(solution);
            ticket.setStatus("resolved");
            ticketService.updateTicket(ticket);
            return Result.ok("工单已解决", null);
        } catch (Exception e) {
            return Result.fail("解决失败：" + e.getMessage());
        }
    }
    
    /**
     * 关闭工单
     */
    @PutMapping("/{id}/close")
    public Result<Void> closeTicket(@PathVariable Long id) {
        try {
            Ticket ticket = ticketService.getById(id);
            if (ticket == null) {
                return Result.fail("工单不存在");
            }
            ticket.setStatus("closed");
            ticketService.updateTicket(ticket);
            return Result.ok("工单已关闭", null);
        } catch (Exception e) {
            return Result.fail("关闭失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除工单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return Result.ok();
    }
}