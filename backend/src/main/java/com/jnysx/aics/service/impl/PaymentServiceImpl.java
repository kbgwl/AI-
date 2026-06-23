package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.Payment;
import com.jnysx.aics.mapper.PaymentMapper;
import com.jnysx.aics.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {
}
