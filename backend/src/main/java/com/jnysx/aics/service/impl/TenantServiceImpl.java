package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.Tenant;
import com.jnysx.aics.mapper.TenantMapper;
import com.jnysx.aics.service.TenantService;
import org.springframework.stereotype.Service;

@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {
}
