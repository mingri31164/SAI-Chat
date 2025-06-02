package com.mingri.service.talk.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.talk.repo.vo.dto.TalkContentDto;
import com.mingri.service.talk.repo.vo.dto.TalkListDto;
import com.mingri.service.talk.repo.vo.entity.Talk;
import com.mingri.service.talk.repo.vo.entity.TalkPermission;
import com.mingri.service.talk.repo.mapper.TalkMapper;
import com.mingri.service.talk.repo.vo.req.CreateTalkReq;
import com.mingri.service.talk.repo.vo.req.DeleteTalkReq;
import com.mingri.service.talk.repo.vo.req.DetailsTalkReq;
import com.mingri.service.talk.repo.vo.req.TalkListReq;
import com.mingri.service.talk.service.TalkPermissionService;
import com.mingri.service.talk.service.TalkService;
import com.mingri.toolkit.SnowflakeIdUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 说说 服务实现类
 */
@Service
public class TalkServiceImpl extends ServiceImpl<TalkMapper, Talk> implements TalkService {

    @Resource
    TalkMapper talkMapper;
    @Resource
    TalkPermissionService talkPermissionService;


    @Override
    public List<TalkListDto> talkList(String userId, TalkListReq talkListReq) {
        return talkMapper.talkList(userId, talkListReq.getIndex(), talkListReq.getNum(), talkListReq.getTargetId());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Talk createTalk(String userId, CreateTalkReq createTalkReq) {
        //新增说说
        Talk talk = new Talk();
        String talkId = SnowflakeIdUtil.nextIdStr();
        talk.setId(talkId);
        talk.setUserId(userId);
        TalkContentDto talkContentDto = new TalkContentDto();
        talkContentDto.setText(createTalkReq.getText());
        talkContentDto.setImg(new ArrayList<>());
        talk.setContent(talkContentDto);
        save(talk);

        // 说说查看权限
        List<String> permissions = createTalkReq.getPermission();
        if (null == permissions || permissions.isEmpty()) {
            TalkPermission talkPermission = new TalkPermission();
            talkPermission.setId(SnowflakeIdUtil.nextIdStr());
            talkPermission.setTalkId(talkId);
            talkPermission.setPermission("all");
            talkPermissionService.save(talkPermission);
        } else {
            List<TalkPermission> talkPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                TalkPermission talkPermission = new TalkPermission();
                talkPermission.setId(SnowflakeIdUtil.nextIdStr());
                talkPermission.setTalkId(talkId);
                talkPermission.setPermission(permission);
                talkPermissionList.add(talkPermission);
            }
            talkPermissionService.saveBatch(talkPermissionList);
        }
        return talk;
    }

    @Override
    public Talk updateTalkImg(String userId, String talkId, String imgName) {
        LambdaQueryWrapper<Talk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Talk::getUserId, userId).eq(Talk::getId, talkId);
        Talk talk = getOne(queryWrapper);
        TalkContentDto content = talk.getContent();
        List<String> imgs = content.getImg();
        if (null == imgs) {
            imgs = new ArrayList<>();
        }
        imgs.add(imgName);
        updateById(talk);
        return talk;
    }

    @Override
    public boolean deleteTalk(String userId, DeleteTalkReq deleteTalkReq) {
        LambdaQueryWrapper<Talk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Talk::getId, deleteTalkReq.getTalkId()).eq(Talk::getUserId, userId);
        return remove(queryWrapper);
    }

    @Override
    public TalkListDto detailsTalk(String userId, DetailsTalkReq detailsTalkReq) {
        return talkMapper.detailsTalk(userId, detailsTalkReq.getTalkId());
    }

    @Override
    public TalkContentDto getFriendLatestTalkContent(String userId, String friendId) {
        Talk talk = talkMapper.getLatestTalkContent(userId, friendId);
        return Optional.ofNullable(talk).map(t -> t.getContent()).orElse(null);
    }
}
