package com.miotech.kun.operationrecord.server.service;

import com.miotech.kun.operationrecord.common.event.BaseOperationEvent;
import com.miotech.kun.operationrecord.server.dao.OperationRecordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationRecordService {

    @Autowired
    private OperationRecordDao operationRecordDao;

    public void create(BaseOperationEvent operationEvent) {
        operationRecordDao.create(operationEvent);
    }

}
