package com.jnysx.aics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jnysx.aics.entity.User;
import com.jnysx.aics.mapper.UserMapper;
import com.jnysx.aics.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现类
 * @author 济南空白格网络科技有限公司
 * @version 1.0.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
