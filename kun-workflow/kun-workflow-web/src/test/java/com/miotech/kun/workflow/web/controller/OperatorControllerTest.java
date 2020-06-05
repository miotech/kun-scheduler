package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.miotech.kun.workflow.common.operator.filter.OperatorSearchFilter;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.web.KunWebServerTestBase;
import com.miotech.kun.workflow.web.serializer.JsonSerializer;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;

public class OperatorControllerTest extends KunWebServerTestBase {

    private static final List<Operator> mockOperatorLists = new ArrayList<>();;

    static {
        // Create 200 mock operators as stored
        for (int i = 0; i < 200; ++i) {
            mockOperatorLists.add(MockOperatorFactory.createOperator());
        }
        // set 10 of the mock operators with keywords for search
        for (int i = 10; i < 20; ++i) {
            mockOperatorLists.set(i, mockOperatorLists.get(i).cloneBuilder().withName("Operator_KeywordForSearch_" + i).build());
        }
    }

    private final OperatorService operatorService = mock(OperatorService.class);

    @Inject
    private JsonSerializer jsonSerializer;

    private static final class IsEmptyOperatorSearchFilter implements ArgumentMatcher<OperatorSearchFilter> {
        @Override
        public boolean matches(OperatorSearchFilter argument) {
            return true;
        }
    }

    @Before
    public void defineBehaviors() {
        // mock fetch behavior of operator service
        Mockito.when(operatorService.fetchOperatorsWithFilter(argThat(new IsEmptyOperatorSearchFilter())))
                .thenAnswer((Answer<List<Operator>>) invocation -> {
                    OperatorSearchFilter filter = invocation.getArgument(0);
                    List<Operator> matchedResults = mockOperatorLists.stream().filter(operator -> {
                        if (StringUtils.isNotEmpty(filter.getKeyword())) {
                            return StringUtils.contains(operator.getName(), filter.getKeyword());
                        }
                        return true;
                    }).collect(Collectors.toList());
                    int startIndex = (filter.getPageNum() - 1) * filter.getPageSize();
                    int endIndex = Math.min(filter.getPageNum() * filter.getPageSize(), matchedResults.size());
                    return matchedResults.subList(startIndex, endIndex);
                });
    }

    @Test
    public void getOperators_withEmptyRequestParams_shouldReturnAtMost100ResultsPerPage () {
        String response = get("/operators");
        List<Operator> operatorList = Arrays.asList(jsonSerializer.toObject(response, Operator[].class));
        assertThat(operatorList.size(), is(100));
    }

    @Test
    public void getOperators_withPaginationQueryParams_shouldReturnCorrectPageSize() {
        String response = get("/operators?pageNum=2&pageSize=50");
        List<Operator> operatorList = Arrays.asList(jsonSerializer.toObject(response, Operator[].class));
        assertThat(operatorList.size(), is(50));

        String outOfRangeQueryResponse = get("/operators?pageNum=5&pageSize=50");
        List<Operator> emptyOperatorList = Arrays.asList(jsonSerializer.toObject(outOfRangeQueryResponse, Operator[].class));
        assertThat(emptyOperatorList.size(), is(0));
    }

    @Test
    public void getOpertors_withKeywordSearchQueryParam_shouldReturnMatchedResults() {
        String response = get("/operators?pageNum=1&pageSize=100&name=KeywordForSearch");
        List<Operator> operatorList = Arrays.asList(jsonSerializer.toObject(response, Operator[].class));
        assertThat(operatorList.size(), is(10));
    }
}
