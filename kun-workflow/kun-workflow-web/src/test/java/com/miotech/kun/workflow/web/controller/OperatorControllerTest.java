package com.miotech.kun.workflow.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.vo.OperatorVO;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.operator.NopOperator;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.web.KunWebServerTestBase;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.commons.web.serializer.JsonSerializer;
import okhttp3.*;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OperatorControllerTest extends KunWebServerTestBase {

    private static final TypeReference<PaginationVO<OperatorVO>> operatorPaginationVOTypeRef = new TypeReference<PaginationVO<OperatorVO>>() {};
    private static final String nopOperatorClassName = NopOperator.class.getSimpleName();
    private static final String nopOperatorPath = OperatorCompiler.compileJar(NopOperator.class,  nopOperatorClassName);

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    private DatabaseOperator dbOperator;

    @Before
    public void preapreData() {
        List<Operator> mockOperatorLists = new ArrayList<>();
        for (int i = 0; i < 200; ++i) {
            Operator op = MockOperatorFactory.createOperator();
            String name = op.getName();
            if (i < 10) {
                name = "Operator_KeywordForSearch_" + i;
            } else {
                name = name + i;
            }
            mockOperatorLists.add(op.cloneBuilder()
                    .withName(name)
                    .withClassName(nopOperatorClassName)
                    .withPackagePath(nopOperatorPath)
                    .build());
        }

        dbOperator.update("TRUNCATE TABLE kun_wf_operator");
        mockOperatorLists.forEach(operatorDao::create);
    }

    @Test
    public void getOperators_withEmptyRequestParams_shouldReturnAtMost100ResultsPerPage () {
        String response = get("/operators");
        PaginationVO<OperatorVO> operatorList = jsonSerializer.toObject(response, operatorPaginationVOTypeRef);
        assertThat(operatorList.getRecords().size(), is(100));
        assertThat(operatorList.getTotalCount(), is(200));
    }

    @Test
    public void test_upload_ok() throws IOException {
        String testPackagePath = "file:" + temporaryFolder.newFile("test").getPath();
        Operator operator = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withClassName(nopOperatorClassName)
                .withPackagePath(testPackagePath)
                .build();
        operatorDao.create(operator);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        File file = new File(nopOperatorPath.replace("file:", ""));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "test.txt",
                        RequestBody.create(
                                MediaType.parse("multipart/form-data"),
                                file))
                .build();
        Request request = new Request.Builder()
                .url(buildUrl("/operators/" + operator.getId() + "/_upload"))
                .post(requestBody)
                .build();

        try (Response resp = client.newCall(request).execute()) {
            Assert.assertThat(resp.body().string(),
                    Matchers.is("{\"ack\":true,\"message\":\"package uploaded\"}"));
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void getOperators_withPaginationQueryParams_shouldReturnCorrectPageSize() {
        String response = get("/operators?pageNum=2&pageSize=50");
        PaginationVO<OperatorVO> operatorListResponse = jsonSerializer.toObject(response, operatorPaginationVOTypeRef);
        assertThat(operatorListResponse.getRecords().size(), is(50));
        assertThat(operatorListResponse.getTotalCount(), is(200));

        String outOfRangeQueryResponse = get("/operators?pageNum=5&pageSize=50");
        PaginationVO<OperatorVO> emptyOperatorList = jsonSerializer.toObject(outOfRangeQueryResponse, operatorPaginationVOTypeRef);
        assertThat(emptyOperatorList.getRecords().size(), is(0));
    }

    @Test
    public void getOperators_withKeywordSearchQueryParam_shouldReturnMatchedResults() {
        String response = get("/operators?pageNum=1&pageSize=100&name=KeywordForSearch");
        PaginationVO<OperatorVO> operatorList = jsonSerializer.toObject(response, operatorPaginationVOTypeRef);
        assertThat(operatorList.getRecords().size(), is(10));
        assertThat(operatorList.getTotalCount(), is(10));
    }
}
