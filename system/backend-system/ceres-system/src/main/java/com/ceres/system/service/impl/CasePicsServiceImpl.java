package com.ceres.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ceres.system.domain.CasePics;
import com.ceres.system.mapper.CasePicsMapper;
import com.ceres.system.service.ICasePicsService;
import org.springframework.stereotype.Service;

@Service
public class CasePicsServiceImpl extends ServiceImpl<CasePicsMapper, CasePics> implements ICasePicsService {
}
