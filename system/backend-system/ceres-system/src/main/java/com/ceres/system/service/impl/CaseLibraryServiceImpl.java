package com.ceres.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ceres.system.domain.CaseLibrary;
import com.ceres.system.mapper.CaseLibraryMapper;
import com.ceres.system.service.ICaseLibraryService;
import org.springframework.stereotype.Service;

@Service
public class CaseLibraryServiceImpl extends ServiceImpl<CaseLibraryMapper, CaseLibrary> implements ICaseLibraryService {

}
