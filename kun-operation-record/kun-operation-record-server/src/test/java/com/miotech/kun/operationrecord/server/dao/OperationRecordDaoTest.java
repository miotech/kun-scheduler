package com.miotech.kun.operationrecord.server.dao;

import com.miotech.kun.operationrecord.common.event.OperationRecordEvent;
import com.miotech.kun.operationrecord.common.model.OperationRecord;
import com.miotech.kun.operationrecord.server.OperationRecordTestBase;
import com.miotech.kun.operationrecord.server.factory.MockTestOperationEventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OperationRecordDaoTest extends OperationRecordTestBase {

    @Autowired
    private OperationRecordDao operationRecordDao;

    @Test
    public void testCreate() {
        OperationRecordEvent testOperationEvent = MockTestOperationEventFactory.create();
        operationRecordDao.create(testOperationEvent);

        List<OperationRecord> operationRecords = operationRecordDao.findByOperator(testOperationEvent.getOperator());
        assertThat(operationRecords.size(), is(1));
        OperationRecord operationRecord = operationRecords.get(0);
        assertThat(operationRecord.getOperator(), is(testOperationEvent.getOperator()));
        assertThat(operationRecord.getStatus(), is(testOperationEvent.getStatus()));
    }

}
