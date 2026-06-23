package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.KbCategory;
import com.jnysx.aics.mapper.KbCategoryMapper;
import com.jnysx.aics.service.KbCategoryService;
import org.springframework.stereotype.Service;

/**
 * 知识库分类Service实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class KbCategoryServiceImpl extends ServiceImpl<KbCategoryMapper, KbCategory> implements KbCategoryService {
}
