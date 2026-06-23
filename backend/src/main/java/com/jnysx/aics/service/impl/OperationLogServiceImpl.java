package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.OperationLog;
import com.jnysx.aics.mapper.OperationLogMapper;
import com.jnysx.aics.service.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志Service实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
