package com.geekq.miaosha.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.geekq.miaosha.common.biz.entity.MiaoshaFailMessage;
import com.geekq.miaosha.common.biz.service.MiaoshaFailMessageService;
import com.geekq.miaosha.order.service.ISecondKillMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class MiaoShaMessageService implements ISecondKillMessageService {
    @Autowired
    private MiaoshaFailMessageService miaoshaFailMessageService;


//    @Autowired
//    private MiaoShaMessageDao messageDao;
//
//    public List<MiaoShaMessageInfo> getmessageUserList(Long userId , Integer status ){
//        return messageDao.listMiaoShaMessageByUserId(userId,status);
//    }
//
//
//    @Transactional(rollbackFor = Exception.class)
//    public void insertMs(MiaoShaMessageVo miaoShaMessageVo){
//        MiaoShaMessageUser mu = new MiaoShaMessageUser() ;
//        mu.setUserId(miaoShaMessageVo.getUserId());
//        mu.setMessageId(miaoShaMessageVo.getMessageId());
//        messageDao.insertMiaoShaMessageUser(mu);
//        MiaoShaMessageInfo miaoshaMessage = new MiaoShaMessageInfo();
//        miaoshaMessage.setContent(miaoShaMessageVo.getContent());
////        miaoshaMessage.setCreateTime(new Date());
//        miaoshaMessage.setStatus(miaoShaMessageVo.getStatus());
//        miaoshaMessage.setMessageType(miaoShaMessageVo.getMessageType());
//        miaoshaMessage.setSendType(miaoShaMessageVo.getSendType());
//        miaoshaMessage.setMessageId(miaoShaMessageVo.getMessageId());
//        miaoshaMessage.setCreateTime(new Date());
//        miaoshaMessage.setMessageHead(miaoShaMessageVo.getMessageHead());
//        messageDao.insertMiaoShaMessage(miaoshaMessage);
//    }

    @Override
    public boolean saveFailMessage(String content, long bussinessId, String businessType, int status){
        MiaoshaFailMessage miaoshaFailMessage=new MiaoshaFailMessage();
        miaoshaFailMessage.setBusinessContent(content);
        miaoshaFailMessage.setBusinessId(bussinessId);
        miaoshaFailMessage.setBusinessType(businessType);
        miaoshaFailMessage.setStatus(status);
        miaoshaFailMessage.setCreateTime(new Date());

        return  miaoshaFailMessageService.save(miaoshaFailMessage);
    }

    @Override
    public boolean updateFailMessageStatus(int id, int status){
        UpdateWrapper<MiaoshaFailMessage> updateWrapper=new UpdateWrapper<>();
        updateWrapper.lambda().eq(MiaoshaFailMessage::getId,id)
                .set(MiaoshaFailMessage::getStatus,status);
        return miaoshaFailMessageService.update(updateWrapper);
    }
    @Override
    public boolean batchUpdateFailMessageStatus(Set<Integer> ids, int status){
        List<MiaoshaFailMessage> messageList=new ArrayList<>();
        for(Integer id:ids){
            MiaoshaFailMessage miaoshaFailMessage=new MiaoshaFailMessage();
            miaoshaFailMessage.setStatus(status);
            miaoshaFailMessage.setId(id);
            messageList.add(miaoshaFailMessage);
        }
        return miaoshaFailMessageService.updateBatchById(messageList);

    }


}
