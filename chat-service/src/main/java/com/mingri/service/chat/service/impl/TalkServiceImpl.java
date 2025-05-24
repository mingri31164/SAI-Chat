package com.mingri.service.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.service.chat.repo.dto.TalkContentDto;
import com.mingri.service.chat.repo.dto.TalkListDto;
import com.mingri.service.chat.repo.entity.Talk;
import com.mingri.service.chat.repo.entity.TalkPermission;
import com.mingri.service.chat.repo.mapper.TalkMapper;
import com.mingri.service.chat.repo.req.talk.CreateTalkVo;
import com.mingri.service.chat.repo.req.talk.DeleteTalkVo;
import com.mingri.service.chat.repo.req.talk.DetailsTalkVo;
import com.mingri.service.chat.repo.req.talk.TalkListVo;
import com.mingri.service.chat.service.TalkPermissionService;
import com.mingri.service.chat.service.TalkService;
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
    public List<TalkListDto> talkList(String userId, TalkListVo talkListVo) {
        return talkMapper.talkList(userId, talkListVo.getIndex(), talkListVo.getNum(), talkListVo.getTargetId());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Talk createTalk(String userId, CreateTalkVo createTalkVo) {
        //新增说说
        Talk talk = new Talk();
        String talkId = IdUtil.randomUUID();
        talk.setId(talkId);
        talk.setUserId(userId);
        TalkContentDto talkContentDto = new TalkContentDto();
        talkContentDto.setText(createTalkVo.getText());
        talkContentDto.setImg(new ArrayList<>());
        talk.setContent(talkContentDto);
        save(talk);

        // 说说查看权限
        List<String> permissions = createTalkVo.getPermission();
        if (null == permissions || permissions.isEmpty()) {
            TalkPermission talkPermission = new TalkPermission();
            talkPermission.setId(IdUtil.randomUUID());
            talkPermission.setTalkId(talkId);
            talkPermission.setPermission("all");
            talkPermissionService.save(talkPermission);
        } else {
            List<TalkPermission> talkPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                TalkPermission talkPermission = new TalkPermission();
                talkPermission.setId(IdUtil.randomUUID());
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
    public boolean deleteTalk(String userId, DeleteTalkVo deleteTalkVo) {
        LambdaQueryWrapper<Talk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Talk::getId, deleteTalkVo.getTalkId()).eq(Talk::getUserId, userId);
        return remove(queryWrapper);
    }

    @Override
    public TalkListDto detailsTalk(String userId, DetailsTalkVo detailsTalkVo) {
        return talkMapper.detailsTalk(userId, detailsTalkVo.getTalkId());
    }

    @Override
    public TalkContentDto getFriendLatestTalkContent(String userId, String friendId) {
        Talk talk = talkMapper.getLatestTalkContent(userId, friendId);
        return Optional.ofNullable(talk).map(t -> t.getContent()).orElse(null);
    }
}
