package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.KbItem;
import com.jnysx.aics.mapper.KbItemMapper;
import com.jnysx.aics.service.KbItemService;
import org.springframework.stereotype.Service;

/**
 * 知识库条目Service实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class KbItemServiceImpl extends ServiceImpl<KbItemMapper, KbItem> implements KbItemService {
}
