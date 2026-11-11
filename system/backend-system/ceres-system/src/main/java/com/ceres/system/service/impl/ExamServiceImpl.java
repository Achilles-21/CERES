package com.ceres.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ceres.common.basic.LambdaQueryWrapperX;
import com.ceres.common.core.domain.entity.SysUser;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.StringUtils;
import com.ceres.common.utils.bean.BeanUtils;
import com.ceres.system.constant.ModelConstant;
import com.ceres.system.domain.*;
import com.ceres.system.domain.vo.req.*;
import com.ceres.system.domain.vo.resp.*;
import com.ceres.system.mapper.*;
import com.ceres.system.service.IExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamServiceImpl implements IExamService {

    @Resource
    private ExamMapper examMapper;

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private ExamResultMapper examResultMapper;

    @Resource
    private ExamCateMapper examCateMapper;

    @Resource
    private ExamCateRelMapper examCateRelMapper;


    @Resource
    private SysUserMapper sysUserMapper;



    @Override
    public void addExam(ExamReq req) {
        Exam exam = new Exam();
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Exam::getDelFlag, "0")
               .eq(Exam::getContent, req.getContent())
               .eq(Exam::getCateId, req.getCateId())
               .last("limit 1");
        if (examMapper.selectOne(wrapper) != null) {
            log.error("添加失败，该题库已存在");
            throw new ServiceException("添加失败，该题目已存在");
        }
        BeanUtils.copyProperties(req, exam);
        exam.setCreateBy(SecurityUtils.getUsername());
        examMapper.insert(exam);
    }

    @Override
    public void deleteExam(Long id) {
        Exam exam = new Exam();
        exam.setId(id);
        exam.setDelFlag("2");
        examMapper.updateById(exam);
    }

    @Override
    public void updateExam(ExamReq req) {
        Exam exam = new Exam();
        if (StringUtils.isNotEmpty(req.getContent())) {
            LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Exam::getDelFlag, "0")
                   .eq(Exam::getContent, req.getContent())
                   .eq(Exam::getCateId, req.getCateId())
                   .ne(Exam::getId, req.getId())
                   .last("limit 1");
            if (examMapper.selectOne(wrapper) != null) {
                log.error("更新失败，该题目内容已存在");
                throw new ServiceException("更新失败，该题目内容已存在");
            }
        }
        BeanUtils.copyProperties(req, exam);
        examMapper.updateById(exam);
    }

    @Override
    public void updateExamStatus(Long id, String status) {

    }

    @Override
    public PageResult getExamList(Integer pageNum, Integer pageSize, String type, String content, String cateId) {
        List<ExamResp> response = new ArrayList<>();
        Page<Exam> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapperX<Exam> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Exam::getDelFlag, "0")
               .eqIfPresent(Exam::getType, type)
               .eqIfPresent(Exam::getCateId, cateId)
               .likeIfPresent(Exam::getContent, content);
        IPage<Exam> examPages = examMapper.selectPage(page, wrapper);
        for (Exam exam : examPages.getRecords()) {
            ExamResp examResp = new ExamResp();
            BeanUtils.copyProperties(exam, examResp);
            response.add(examResp);
        }
        return new PageResult(response, examPages.getTotal(), pageNum, pageSize);
    }

    @Override
    public ExamResp getExamById(Long id) {
        ExamResp examResp = new ExamResp();
        Exam exam = examMapper.selectById(id);
        BeanUtils.copyProperties(exam, examResp);
        return examResp;
    }

    @Override
    public RecommendExamRespPage getRecommendExamList(PageReq pageReq, String cateId) {
        RecommendExamRespPage recommendExamRespPage = new RecommendExamRespPage();
        List<RecommendExamResp> response = new ArrayList<>();

        List<Exam> exams = examMapper.selectListByCateId(Long.parseLong(cateId));

        for (Exam exam : exams) {
            RecommendExamResp examResp = new RecommendExamResp();
            BeanUtils.copyProperties(exam, examResp);
            examResp.setQueId(exam.getId());

            List<ChoiceOptions> options = new ArrayList<>();
            options.add(new ChoiceOptions("A", exam.getChoiceA()));
            options.add(new ChoiceOptions("B", exam.getChoiceB()));
            options.add(new ChoiceOptions("C", exam.getChoiceC()));
            options.add(new ChoiceOptions("D", exam.getChoiceD()));
            if(StringUtils.isNotEmpty(exam.getChoiceE())) {
                options.add(new ChoiceOptions("E", exam.getChoiceE()));
            }
            if(StringUtils.isNotEmpty(exam.getChoiceF())) {
                options.add(new ChoiceOptions("F", exam.getChoiceF()));
            }
            examResp.setOptions(options);
            examResp.setFavoriteFlag("0");
            examResp.setType(exam.getType());
            response.add(examResp);
        }
        recommendExamRespPage.setRecommendExamRespList(response);


        ExamCate examCate = examCateMapper.selectById(Long.parseLong(cateId));
        recommendExamRespPage.setLimitTime(examCate.getLimitTime());

        return recommendExamRespPage;
    }

    @Override
    public void saveExamRecordList(ExamRecordListReq req) {
        List<ExamRecord> examRecordList = new ArrayList<>();
        Long userId = SecurityUtils.getUserId();
        Date now = new Date();
        for(ExamRecordReq examRecordRea : req.getExamRecordReqs()) {
            ExamRecord examRecord = new ExamRecord();
            BeanUtils.copyProperties(examRecordRea, examRecord);
            examRecord.setCateId(req.getCateId());
            examRecord.setUserId(userId);
            examRecord.setUpdateTime(now);

            examRecordList.add(examRecord);
        }
        examRecordMapper.insertRecordList(examRecordList);
    }

    @Override
    public List<Map<String, String>> listAll(String value) {
        LambdaQueryWrapper<Exam> lambdaQueryWrapper = new LambdaQueryWrapper<Exam>().eq(Exam::getDelFlag, 0).like(StrUtil.isNotBlank(value), Exam::getContent, value);
        List<Exam> examList = examMapper.selectList(lambdaQueryWrapper);
        List<Map<String, String>> list = new ArrayList<>();
        for (Exam exam : examList) {
            Map<String, String> map = new HashMap<>();
            map.put("label", exam.getContent());
            map.put("value", exam.getId().toString());
            list.add(map);
        }
        return list;
    }

    @Override
    public List<Long> examIdList(Long cateId) {
        List<ExamCateRel> examCateRels = examCateRelMapper.getExamRelList(cateId);
        List<Long> examIdList = new ArrayList<>();
        for(ExamCateRel examCateRel : examCateRels) {
            examIdList.add(examCateRel.getExamId());
        }
        return examIdList;
    }


    @Override
    public void addExamCate(ExamCateReq examCateReq) {
        ExamCate addExamCate = new ExamCate();
        LambdaQueryWrapper<ExamCate> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ExamCate::getName, examCateReq.getName()).last("limit 1");
        if (examCateMapper.selectOne(wrapper) != null) {
            log.error("添加失败，该类目已存在");
            throw new ServiceException("添加失败，该题目已存在");
        }
        BeanUtils.copyProperties(examCateReq, addExamCate);
        examCateMapper.insert(addExamCate);
        examCateRelMapper.setExamCateRel(examCateReq.getQueIds(), addExamCate.getId());
    }

    @Override
    public void deleteExamCate(Long id) {
        examCateMapper.deleteById(id);
    }

    @Override
    public void updateExamCate(ExamCateReq examCateReq) {
        ExamCate updateExamCate = new ExamCate();
        if(StringUtils.isEmpty(examCateReq.getName())) {
            throw new ServiceException("更新失败，试卷名不能为空");
        }

        examCateRelMapper.delExamCateRel(examCateReq.getId());
        examCateRelMapper.setExamCateRel(examCateReq.getQueIds(), examCateReq.getId());

        BeanUtils.copyProperties(examCateReq, updateExamCate);
        examCateMapper.updateById(updateExamCate);
    }

    @Override
    public List<ExamCateResp> getExamCateList() {
        Long userId = SecurityUtils.getUserId();
        return examCateMapper.getExamCateList(userId);
    }

    @Override
    public List<ExamCateResp> getFrontExamCateList() {
        Long userId = SecurityUtils.getUserId();
        String level = SecurityUtils.getLevel();
        if(StringUtils.isEmpty(level)) {
            level = "0";
        }
        return examCateMapper.getFrontExamCateList(userId, level);
    }
}
