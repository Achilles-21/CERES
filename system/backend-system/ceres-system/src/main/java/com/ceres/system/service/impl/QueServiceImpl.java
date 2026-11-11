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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ceres.common.basic.LambdaQueryWrapperX;
import com.ceres.common.core.domain.entity.SysUser;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MessageUtils;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.StringUtils;
import com.ceres.common.utils.bean.BeanUtils;
import com.ceres.system.constant.ModelConstant;
import com.ceres.system.domain.*;
import com.ceres.system.domain.vo.req.QueRecordReq;
import com.ceres.system.domain.vo.req.QueReq;
import com.ceres.system.domain.vo.resp.*;
import com.ceres.system.mapper.*;
import com.ceres.system.service.IQueService;
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
public class QueServiceImpl implements IQueService {

    @Resource
    private QueMapper queMapper;

    @Resource
    private QueRecordMapper queRecordMapper;

    @Resource
    private FavoritesQueMapper favoritesQueMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private QuePopRecordMapper quePopRecordMapper;

    @Resource
    private QuestionRecordMapper questionRecordMapper;

    @Override
    public void addQue(QueReq req) {
        Que que = new Que();
        LambdaQueryWrapper<Que> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Que::getDelFlag, "0")
               .eq(Que::getContent, req.getContent())
               .last("limit 1");
        if (queMapper.selectOne(wrapper) != null) {
            throw new ServiceException(MessageUtils.message("question.exists"));
        }
        BeanUtils.copyProperties(req, que);
        que.setCreateBy(SecurityUtils.getUsername());
        queMapper.insert(que);
    }

    @Override
    public void deleteQue(Long id) {
        Que que = new Que();
        que.setId(id);
        que.setDelFlag("2");
        queMapper.updateById(que);
    }

    @Override
    public void updateQue(QueReq req) {
        Que que = new Que();
        if (StringUtils.isNotEmpty(req.getContent())) {
            LambdaQueryWrapper<Que> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Que::getDelFlag, "0")
                   .eq(Que::getContent, req.getContent())
                   .ne(Que::getId, req.getId())
                   .last("limit 1");
            if (queMapper.selectOne(wrapper) != null) {
                log.error("更新失败，该题目内容已存在");
                throw new ServiceException("更新失败，该题目内容已存在");
            }
        }
        BeanUtils.copyProperties(req, que);
        queMapper.updateById(que);
    }

    @Override
    public void updateQueStatus(Long id, String status) {

    }

    @Override
    public PageResult getQueList(Integer pageNum, Integer pageSize, String type, String content) {
        List<QueResp> response = new ArrayList<>();
        Page<Que> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapperX<Que> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Que::getDelFlag, "0")
               .eqIfPresent(Que::getType, type)
               .likeIfPresent(Que::getContent, content);
        IPage<Que> quePages = queMapper.selectPage(page, wrapper);
        for (Que que : quePages.getRecords()) {
            QueResp queResp = new QueResp();
            BeanUtils.copyProperties(que, queResp);
            response.add(queResp);
        }
        return new PageResult(response, quePages.getTotal(), pageNum, pageSize);
    }

    @Override
    public QueResp getQueById(Long id) {
        QueResp queResp = new QueResp();
        Que que = queMapper.selectById(id);
        BeanUtils.copyProperties(que, queResp);
        return queResp;
    }

    @Override
    public List<RecommendQueResp> getRecommendQueList() {
        List<RecommendQueResp> response = new ArrayList<>();
        Page<Que> page = new Page<>(1,10);
        LambdaQueryWrapper<Que> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<Que> ques = queMapper.selectPage(page, lambdaQueryWrapper).getRecords();
        List<Long> queIdList = ques.stream().map(Que::getId).collect(Collectors.toList());
        LambdaQueryWrapper<FavoritesQue> eq = new LambdaQueryWrapper<FavoritesQue>().eq(FavoritesQue::getUserId, SecurityUtils.getUserId()).in(FavoritesQue::getQueId, queIdList);
        List<FavoritesQue> favoritesQueList = favoritesQueMapper.selectList(eq);
        Map<Long, FavoritesQue> favoritesQueMap = favoritesQueList.stream().collect(Collectors.toMap(FavoritesQue::getQueId, q -> q));
        for (Que que : ques) {
            RecommendQueResp queResp = new RecommendQueResp();
            BeanUtils.copyProperties(que, queResp);
            queResp.setQueId(que.getId());
            List<ChoiceOptions> options = new ArrayList<>();
            options.add(new ChoiceOptions("A", que.getChoiceA()));
            options.add(new ChoiceOptions("B", que.getChoiceB()));
            options.add(new ChoiceOptions("C", que.getChoiceC()));
            options.add(new ChoiceOptions("D", que.getChoiceD()));
            queResp.setOptions(options);
            if (ObjectUtil.isNotEmpty(favoritesQueMap.get(que.getId()))) {
                queResp.setFavoriteFlag("1");
            } else {
                queResp.setFavoriteFlag("0");
            }
            response.add(queResp);
        }
        return response;
    }

    @Override
    @Transactional
    public void saveQueRecord(QueReq req) {
        List<QueRecordReq> queRecordReqs = req.getQueRecordReqs();
        Long userId = SecurityUtils.getUserId();
        log.info("用户 {} 提交了 {} 题", userId, queRecordReqs.size());
        for (QueRecordReq queRecordReq : queRecordReqs) {
            QueRecord queRecord = new QueRecord();
            queRecord.setAnswer(queRecordReq.getAnswer());
            queRecord.setQueId(queRecordReq.getQueId());
            queRecord.setUserId(userId);
            queRecord.setUpdateTime(LocalDateTime.now());
            queRecordMapper.insert(queRecord);
        }

        if(ObjUtil.isNotEmpty(queRecordReqs.get(0))){
            QueRecordReq recordReq = queRecordReqs.get(0);
            QuestionRecord questionRecord = new QuestionRecord();
            QueResp queResp = getQueById(recordReq.getQueId());
            questionRecord.setQueId(recordReq.getQueId());
            questionRecord.setQueContent(queResp.getContent());
            questionRecord.setUserId(SecurityUtils.getUserId());
            questionRecord.setUserOptions(recordReq.getAnswer());
                String answer = queResp.getAnswer();
                if(answer.equals(recordReq.getAnswer())){
                    questionRecord.setResult(1);
                }else {
                    questionRecord.setResult(0);
                }
            questionRecord.setCreateTime(LocalDateTime.now());
            questionRecordMapper.insert(questionRecord);
        }
    }

    @Override
    public PageResult getQueRecordList(Integer pageNum, Integer pageSize) {
        Page<QueAnswerRecordResp> page = new Page<>(pageNum, pageSize);
        return null;
    }

    @Override
    public PageResult getAnswer(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<QueRecord> eq = new LambdaQueryWrapper<>();
        List<QueRecord> queRecordList = queRecordMapper.selectList(eq);
        if(CollUtil.isEmpty(queRecordList)){
            return new PageResult(ListUtil.empty(), 0, pageNum, pageSize);
        }
        List<Long> userIds = queRecordList.stream().map(QueRecord::getUserId).collect(Collectors.toList());
        List<SysUser> sysUserList = sysUserMapper.selectUserByIds(userIds);
        Map<Long, String> userMap = sysUserList.stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName));
        Map<Long, Long> queRecordMap = queRecordList.stream().collect(Collectors.groupingBy(QueRecord::getUserId, Collectors.counting()));
        List<QueAnswerResp> queAnswerRespList = new ArrayList<>();
        queRecordMap.forEach((userId, answerCount)->{
            if(ObjectUtil.isNotEmpty(userMap.get(userId))){
                QueAnswerResp queAnswerResp = new QueAnswerResp();
                queAnswerResp.setAnswerCount(answerCount.intValue());
                queAnswerResp.setUserName(userMap.get(userId));
                queAnswerResp.setUserId(userId);
                queAnswerRespList.add(queAnswerResp);
            }
        });
        List<QueAnswerResp> answerRespList = queAnswerRespList.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
        return new PageResult(answerRespList, queAnswerRespList.size(), pageNum, pageSize);
    }

    @Override
    public List<RecommendQueResp> getAnswerDetails(Long userId) {
        LambdaQueryWrapper<QueRecord> eq = new LambdaQueryWrapper<QueRecord>().eq(QueRecord::getUserId, userId);
        List<QueRecord> queRecordList = queRecordMapper.selectList(eq);
        Set<Long> queIds = queRecordList.stream().map(QueRecord::getQueId).collect(Collectors.toSet());
        Map<Long, String> userAnswer = queRecordList.stream().collect(Collectors.toMap(QueRecord::getQueId, QueRecord::getAnswer, (a, b) -> a));
        List<RecommendQueResp> recommendQueRespList = new ArrayList<>();
        if(CollUtil.isNotEmpty(queIds)) {
            LambdaQueryWrapper<FavoritesQue> lambdaQueryWrapper = new LambdaQueryWrapper<FavoritesQue>().eq(FavoritesQue::getUserId, userId).in(FavoritesQue::getQueId, queIds);
            List<FavoritesQue> favoritesQueList = favoritesQueMapper.selectList(lambdaQueryWrapper);
            Map<Long, FavoritesQue> favoritesQueMap = favoritesQueList.stream().collect(Collectors.toMap(FavoritesQue::getQueId, f -> f));
            LambdaQueryWrapper<Que> in = new LambdaQueryWrapper<Que>().in(Que::getId, queIds);
            List<Que> queList = queMapper.selectList(in);
            for (Que que : queList) {
                RecommendQueResp queResp = BeanUtil.toBean(que, RecommendQueResp.class);
                queResp.setIsanswer(userAnswer.get(que.getId()));
                queResp.setQueId(que.getId());
                queResp.setDisabled(true);
                List<ChoiceOptions> choiceOptionsList = new ArrayList<>();
                if (StrUtil.isNotBlank(que.getChoiceA())) {
                    ChoiceOptions optionsA = new ChoiceOptions("A", que.getChoiceA());
                    choiceOptionsList.add(optionsA);
                }
                if (StrUtil.isNotBlank(que.getChoiceB())) {
                    ChoiceOptions optionsB = new ChoiceOptions("B", que.getChoiceB());
                    choiceOptionsList.add(optionsB);
                }
                if (StrUtil.isNotBlank(que.getChoiceC())) {
                    ChoiceOptions optionsC = new ChoiceOptions("C", que.getChoiceC());
                    choiceOptionsList.add(optionsC);
                }
                if (StrUtil.isNotBlank(que.getChoiceD())) {
                    ChoiceOptions optionsD = new ChoiceOptions("D", que.getChoiceD());
                    choiceOptionsList.add(optionsD);
                }
                if (StrUtil.isNotBlank(que.getChoiceE())) {
                    ChoiceOptions optionsE = new ChoiceOptions("E", que.getChoiceE());
                    choiceOptionsList.add(optionsE);
                }
                if (StrUtil.isNotBlank(que.getChoiceF())) {
                    ChoiceOptions optionsF = new ChoiceOptions("F", que.getChoiceF());
                    choiceOptionsList.add(optionsF);
                }
                queResp.setOptions(choiceOptionsList);
                if (ObjectUtil.isNotEmpty(favoritesQueMap.get(que.getId()))) {
                    queResp.setFavoriteFlag("1");
                } else {
                    queResp.setFavoriteFlag("0");
                }
                recommendQueRespList.add(queResp);
            }
        }

        return recommendQueRespList;
    }

    @Override
    public List<Map<String, String>> listAll(String value) {
        LambdaQueryWrapper<Que> lambdaQueryWrapper = new LambdaQueryWrapper<Que>().eq(Que::getDelFlag, 0).like(StrUtil.isNotBlank(value), Que::getContent, value);
        List<Que> queList = queMapper.selectList(lambdaQueryWrapper);
        List<Map<String, String>> list = new ArrayList<>();
        for (Que que : queList) {
            Map<String, String> map = new HashMap<>();
            map.put("label", que.getContent());
            map.put("value", que.getId().toString());
            list.add(map);
        }
        return list;
    }

    @Override
    public Map<String, String> send(Map<String, String> map) {
        String res = null;
        String detail = null;
        if(StrUtil.isNotBlank(map.get("userAnswer"))){
            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put(ModelConstant.USER_ID, SecurityUtils.getUserId().toString());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timeSpan = LocalDateTime.now().format(dateTimeFormatter);
            paramMap.put(ModelConstant.TIME_SPAN, timeSpan);
            paramMap.put(ModelConstant.QUESTION, map.get("content"));
            paramMap.put(ModelConstant.CORRECT_ANS, map.get("answer"));
            paramMap.put(ModelConstant.USER_ANS, map.get("userAnswer"));
            String result = HttpUtil.post(ModelConstant.VIDEO_POP_JUDGMENT_URL, JSONUtil.toJsonStr(paramMap));
            if("Internal Server Error".equals(result) || StrUtil.isBlank(result) || "null".equals(result)){
                throw new ServiceException(MessageUtils.message("model.server.error"));
            }
            JSONObject jsonObject = JSONUtil.parseObj(result);
            res = jsonObject.getStr("res");
            detail = jsonObject.getStr("explanation");
        }
        LambdaQueryWrapper<QuePopRecord> wrapper = new LambdaQueryWrapper<QuePopRecord>().eq(QuePopRecord::getUserId, SecurityUtils.getUserId())
                .eq(QuePopRecord::getVideoId, Long.valueOf(map.get("id"))).eq(QuePopRecord::getQueId, Long.valueOf(map.get("queId")));
        QuePopRecord quePopRecord = quePopRecordMapper.selectOne(wrapper);
        if(ObjUtil.isEmpty(quePopRecord)){
            quePopRecordMapper.insert(new QuePopRecord(null, SecurityUtils.getUserId(), Long.valueOf(map.get("id")), Integer.valueOf(StringUtils.isEmpty(res) ? "0" : res), Long.valueOf(map.get("queId"))));
        }
        Map<String, String> mapResp = new HashMap<>();
        mapResp.put("res", res);
        mapResp.put("detail", detail);
        return mapResp;
    }

    @Override
    public List<Que> selectBatchIds(List<Long> queIds) {
        List<Que> queList = queMapper.selectBatchIds(queIds);
        return queList;
    }

    @Override
    public List<RecommendQueResp> getQueByIds(ArrayList<Long> questionIds) {
        List<Que> queList = queMapper.selectBatchIds(questionIds);
        if(CollUtil.isEmpty(queList)){
            return ListUtil.empty();
        }
        List<Long> queIdList = queList.stream().map(Que::getId).collect(Collectors.toList());
        LambdaQueryWrapper<FavoritesQue> eq = new LambdaQueryWrapper<FavoritesQue>().eq(FavoritesQue::getUserId, SecurityUtils.getUserId()).in(FavoritesQue::getQueId, queIdList);
        List<FavoritesQue> favoritesQueList = favoritesQueMapper.selectList(eq);
        Map<Long, FavoritesQue> favoritesQueMap = favoritesQueList.stream().collect(Collectors.toMap(FavoritesQue::getQueId, q -> q));
        List<RecommendQueResp> respList = new ArrayList<>();
        for (Que que : queList) {
            RecommendQueResp queResp = new RecommendQueResp();
            BeanUtils.copyProperties(que, queResp);
            queResp.setQueId(que.getId());
            List<ChoiceOptions> options = new ArrayList<>();
            if (StrUtil.isNotBlank(que.getChoiceA())) {
                ChoiceOptions optionsA = new ChoiceOptions("A", que.getChoiceA());
                options.add(optionsA);
            }
            if (StrUtil.isNotBlank(que.getChoiceB())) {
                ChoiceOptions optionsB = new ChoiceOptions("B", que.getChoiceB());
                options.add(optionsB);
            }
            if (StrUtil.isNotBlank(que.getChoiceC())) {
                ChoiceOptions optionsC = new ChoiceOptions("C", que.getChoiceC());
                options.add(optionsC);
            }
            if (StrUtil.isNotBlank(que.getChoiceD())) {
                ChoiceOptions optionsD = new ChoiceOptions("D", que.getChoiceD());
                options.add(optionsD);
            }
            if (StrUtil.isNotBlank(que.getChoiceE())) {
                ChoiceOptions optionsE = new ChoiceOptions("E", que.getChoiceE());
                options.add(optionsE);
            }
            if (StrUtil.isNotBlank(que.getChoiceF())) {
                ChoiceOptions optionsF = new ChoiceOptions("F", que.getChoiceF());
                options.add(optionsF);
            }
            queResp.setOptions(options);
            if (ObjectUtil.isNotEmpty(favoritesQueMap.get(que.getId()))) {
                queResp.setFavoriteFlag("1");
            } else {
                queResp.setFavoriteFlag("0");
            }
            respList.add(queResp);
        }
        return respList;
    }
}
