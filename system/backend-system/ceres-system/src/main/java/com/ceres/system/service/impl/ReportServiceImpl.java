package com.ceres.system.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.StringUtils;
import com.ceres.common.utils.bean.BeanUtils;
import com.ceres.system.constant.ModelConstant;
import com.ceres.system.domain.Report;
import com.ceres.system.domain.vo.resp.ReportResp;
import com.ceres.system.mapper.ReportMapper;
import com.ceres.system.service.IReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements IReportService {

    @Resource
    private ReportMapper reportMapper;

    @Override
    public PageResult getReportList(Integer pageNum, Integer pageSize, String title) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        IPage<Report> reportList = reportMapper.getReportList(page, title, SecurityUtils.getUsername());
        List<ReportResp> result = new ArrayList<>();
        for (Report record : reportList.getRecords()) {
            ReportResp resp = new ReportResp();
            BeanUtils.copyProperties(record, resp);
            result.add(resp);
        }
        return new PageResult(result, reportList.getTotal(), pageNum, pageSize);
    }

    @Override
    public ReportResp getReportById(Long id) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        ReportResp resp = new ReportResp();
        queryWrapper.eq(Report::getId, id)
                    .eq(Report::getDelFlag, "0");
        Report report = reportMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(report, resp);
        return resp;
    }

    @Override
    public void deleteReport(Long id) {
        Report report = new Report();
        report.setId(id);
        report.setDelFlag("2");
        reportMapper.updateById(report);
    }

    @Override
    public void addReport(Report report) {
        report.setCreateBy(SecurityUtils.getUsername());
        reportMapper.insert(report);
    }

    @Override
    public ReportResp getReportByVideoId(Long videoId) {
        ReportResp resp = new ReportResp();
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Report::getVideoId, videoId)
                .eq(Report::getCreateBy, SecurityUtils.getUsername())
                    .eq(Report::getDelFlag, "0");
        Report report = reportMapper.selectOne(queryWrapper);
        if (StringUtils.isNull(report)) {
            String url = ModelConstant.SUMMARY_REPORT_URL;
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put(ModelConstant.USER_ID, SecurityUtils.getUserId().toString());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timeSpan = LocalDateTime.now().format(dateTimeFormatter);
            paramMap.put(ModelConstant.TIME_SPAN, timeSpan);
            paramMap.put(ModelConstant.COURSE_ID, videoId.toString());
            String result = HttpUtil.post(url, JSONObject.toJSONString(paramMap));
            JSONObject jsonObject = JSONObject.parseObject(result);
            String summary = jsonObject.getString(ModelConstant.SUMMARY);
            report = new Report();
            report.setVideoId(videoId);
            report.setContent(summary);
            report.setCreateBy(SecurityUtils.getUsername());
            reportMapper.insert(report);
        }
        BeanUtils.copyProperties(report, resp);
        return resp;
    }
}
