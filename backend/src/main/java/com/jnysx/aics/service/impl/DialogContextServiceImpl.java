package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.DialogContext;
import com.jnysx.aics.mapper.DialogContextMapper;
import com.jnysx.aics.service.DialogContextService;
import org.springframework.stereotype.Service;

/**
 * 对话上下文Service实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class DialogContextServiceImpl extends ServiceImpl<DialogContextMapper, DialogContext> implements DialogContextService {
}
