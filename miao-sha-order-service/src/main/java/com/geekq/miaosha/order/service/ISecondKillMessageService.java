package com.geekq.miaosha.order.service;

import java.util.Set;

public interface ISecondKillMessageService {
    boolean saveFailMessage(String content, long bussinessId, String businessType, int status);

    boolean updateFailMessageStatus(int id, int status);

    boolean batchUpdateFailMessageStatus(Set<Integer> ids, int status);
}
