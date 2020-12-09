package com.miotech.kun.workflow.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.utils.JSONUtils;
import okhttp3.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WorkflowApi {
    private final Logger logger = LoggerFactory.getLogger(WorkflowApi.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_OPERATORS = "/operators";
    private static final String API_TASKS = "/tasks";
    private static final String API_TASK_RUNS = "/taskruns";
    private static final String API_LINEAGES = "/lineages";

    private final String baseUrl;
    private final OkHttpClient client;

    private final Long READ_TIME_OUT = 3 * 60 * 1000L;
    private final Long WRITE_TIME_OUT = 60 * 1000l;

    public WorkflowApi(String url) {
        this.baseUrl = url;
        this.client = new OkHttpClient.Builder()
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .build();
    }

    private HttpUrl getUrl(String path) {
        return buildUrl(path).build();
    }

    private HttpUrl.Builder buildUrl(String path) {
        HttpUrl url = HttpUrl.get(baseUrl + path);
        return url.newBuilder();
    }

    private String sendRequest(Request request) {
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            int statusCode = response.code();
            if (statusCode >= 400 || statusCode < 200) {
                throw new WorkflowApiException(String.format("\"%s %s\" is not valid: %s", request.method(), request.url(), response.body().string()));
            }
            return response.body().string();
        } catch (IOException e) {
            logger.error("Failed to execute request {}", request, e);
            throw new WorkflowApiException("Failed to get response to " + request.url(), e);
        }
    }

    private <T> T sendRequest(Request request, Class<T> clz) {
        return JSONUtils.jsonToObject(sendRequest(request), clz);
    }

    private <T> T sendRequest(Request request, TypeReference<T> typeRef) {
        return JSONUtils.jsonToObject(sendRequest(request), typeRef);
    }

    private <T> T post(HttpUrl url, Object payload, Class<T> responseClz) {
        Request request = new Request.Builder().url(url)
                .post(jsonBody(payload))
                .build();
        return sendRequest(request, responseClz);
    }

    private <T> T put(HttpUrl url, Object payload, Class<T> responseClz) {
        Request request = new Request.Builder().url(url)
                .put(jsonBody(payload))
                .build();
        return sendRequest(request, responseClz);
    }

    private <T> T get(HttpUrl url, Class<T> responseClz) {
        Request request = new Request.Builder().url(url)
                .get()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(request, responseClz);
    }

    private void delete(HttpUrl url) {
        Request request = new Request.Builder().url(url)
                .delete()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        sendRequest(request, Object.class);
    }

    private RequestBody jsonBody(Object payload) {
        Preconditions.checkNotNull(payload, "request body should not be null");
        return RequestBody.create(APPLICATION_JSON, JSONUtils.toJsonString(payload));
    }

    // Operators
    public Operator createOperator(Operator createPayload) {
        HttpUrl url = getUrl(API_OPERATORS);
        return post(url, createPayload, Operator.class);
    }

    public void uploadJar(Long operatorId, File jarFile) {
        HttpUrl.Builder urlBuilder = buildUrl(API_OPERATORS)
                .addPathSegment(operatorId.toString())
                .addPathSegment("_upload");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "jarFile",
                        RequestBody.create(
                                MediaType.parse("multipart/form-data"),
                                jarFile))
                .build();
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(requestBody)
                .build();
        sendRequest(request, Object.class);
    }

    // searchByName
    public PaginationResult<Operator> getOperators(OperatorSearchRequest request) {
        HttpUrl.Builder urlBuilder = buildUrl(API_OPERATORS);
        if (StringUtils.isNoneBlank(request.getName())) {
            urlBuilder.addQueryParameter("name", request.getName());
        }
        if (request.getPageNum() > 0) {
            urlBuilder.addQueryParameter("pageNum", "" + request.getPageNum());
        }
        if (request.getPageSize() > 0) {
            urlBuilder.addQueryParameter("pageSize", "" + request.getPageSize());
        }
        Request getRequest = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();

        return sendRequest(getRequest, new TypeReference<PaginationResult<Operator>>() {
        });
    }

    public Operator getOperator(Long operatorId) {
        HttpUrl url = buildUrl(API_OPERATORS)
                .addPathSegment(operatorId.toString())
                .build();
        return get(url, Operator.class);
    }

    public Operator updateOperator(Long operatorId, Operator updatePayload) {
        HttpUrl url = buildUrl(API_OPERATORS)
                .addPathSegment(operatorId.toString())
                .build();
        return put(url, updatePayload, Operator.class);
    }

    public void deleteOperator(Long operatorId) {
        HttpUrl url = buildUrl(API_OPERATORS)
                .addPathSegment(operatorId.toString())
                .build();
        delete(url);
    }

    // Tasks
    public Task createTask(Task task) {
        HttpUrl url = getUrl(API_TASKS);
        return post(url, task, Task.class);
    }

    public Task getTask(Long taskId) {
        HttpUrl url = buildUrl(API_TASKS)
                .addPathSegment(taskId.toString())
                .build();
        return get(url, Task.class);
    }

    public List<Task> getTasks(TaskSearchRequest request) {
        HttpUrl url = buildUrl("/tasks/_search")
                .build();
        Request postRequest = new Request.Builder().url(url)
                .post(jsonBody(request))
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();

        return sendRequest(postRequest, new TypeReference<PaginationResult<Task>>() {
        }).getRecords();
    }

    public Task updateTask(Long taskId, Task task) {
        HttpUrl url = buildUrl(API_TASKS)
                .addPathSegment(taskId.toString())
                .build();
        return put(url, task, Task.class);
    }

    public void deleteTask(Long taskId) {
        HttpUrl url = buildUrl(API_TASKS)
                .addPathSegment(taskId.toString())
                .build();
        delete(url);
    }

    public PaginationResult<Task> searchTasks(TaskSearchRequest request) {
        HttpUrl url = buildUrl("/tasks/_search")
                .build();
        Request postRequest = new Request.Builder().url(url)
                .post(jsonBody(request))
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(postRequest, new TypeReference<PaginationResult<Task>>() {
        });
    }

    public List<Long> runTasks(RunTaskRequest request) {
        HttpUrl url = buildUrl(API_TASKS)
                .addPathSegment("_run")
                .build();
        Request postRequest = new Request.Builder().url(url)
                .post(jsonBody(request.getRunTasks()))
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(postRequest, new TypeReference<List<Long>>() {
        });
    }

    public TaskDAG getTaskDAG(Long taskId, int upstreamLevel, int downstreamLevel) {
        HttpUrl.Builder url = buildUrl(API_TASKS)
                .addPathSegment(taskId.toString())
                .addPathSegment("neighbors");
        if (upstreamLevel >= 0) {
            url.addQueryParameter("upstreamLevel", Integer.toString(upstreamLevel));
        }
        if (downstreamLevel >= 0) {
            url.addQueryParameter("downstreamLevel", Integer.toString(downstreamLevel));
        }
        return get(url.build(), TaskDAG.class);
    }

    // TaskRun
    public TaskRun getTaskRun(Long taskRunId) {
        HttpUrl url = buildUrl(API_TASK_RUNS)
                .addPathSegment(taskRunId.toString())
                .build();
        return get(url, TaskRun.class);
    }

    public Object stopTaskRun(Long taskRunId) {
        HttpUrl url = buildUrl(API_TASK_RUNS)
                .addPathSegment(taskRunId.toString())
                .addPathSegment("_abort")
                .build();
        return put(url, Maps.newHashMap(), Object.class);
    }

    public TaskRunState getTaskRunStatus(Long taskRunId) {
        HttpUrl url = buildUrl(API_TASK_RUNS)
                .addPathSegment(taskRunId.toString())
                .addPathSegment("status")
                .build();
        return get(url, TaskRunState.class);
    }

    public TaskRunDAG getTaskRunDAG(Long taskRunId, int upstreamLevel, int downstreamLevel) {
        HttpUrl.Builder url = buildUrl(API_TASK_RUNS)
                .addPathSegment(taskRunId.toString())
                .addPathSegment("neighbors");
        if (upstreamLevel >= 0) {
            url.addQueryParameter("upstreamLevel", Integer.toString(upstreamLevel));
        }
        if (downstreamLevel >= 0) {
            url.addQueryParameter("downstreamLevel", Integer.toString(downstreamLevel));
        }
        return get(url.build(), TaskRunDAG.class);
    }

    public TaskRunLog getTaskRunLog(TaskRunLogRequest logRequest) {
        Preconditions.checkNotNull(logRequest.getTaskRunId());
        HttpUrl.Builder urlBuilder = buildUrl(API_TASK_RUNS)
                .addPathSegment("" + logRequest.getTaskRunId())
                .addPathSegment("logs");

        if (logRequest.getAttempt() > 0) {
            urlBuilder.addQueryParameter("attempt", "" + logRequest.getAttempt());
        }
        if (logRequest.getStartLine() >= 0) {
            urlBuilder.addQueryParameter("startLine", "" + logRequest.getStartLine());
        }
        if (logRequest.getEndLine() > 0) {
            urlBuilder.addQueryParameter("endLine", "" + logRequest.getEndLine());
        }
        HttpUrl url = urlBuilder.build();
        return get(url, TaskRunLog.class);
    }

    public PaginationResult<TaskRun> getTaskRuns(TaskRunSearchRequest request) {
        HttpUrl.Builder urlBuilder = buildUrl(API_TASK_RUNS);
        if (CollectionUtils.isNotEmpty(request.getTaskIds())) {
            String taskIds = request.getTaskRunIds()
                    .stream().map(Object::toString)
                    .collect(Collectors.joining(","));
            urlBuilder.addQueryParameter("taskIds", taskIds);
        }
        if (CollectionUtils.isNotEmpty(request.getTaskRunIds())) {
            String taskRunIds = request.getTaskRunIds()
                    .stream().map(Object::toString)
                    .collect(Collectors.joining(","));
            urlBuilder.addQueryParameter("taskRunIds", taskRunIds);
        }
        if (request.getPageNum() > 0) {
            urlBuilder.addQueryParameter("pageNum", "" + request.getPageNum());
        }
        if (request.getPageSize() > 0) {
            urlBuilder.addQueryParameter("pageSize", "" + request.getPageSize());
        }
        Request getRequest = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(getRequest, new TypeReference<PaginationResult<TaskRun>>() {
        });
    }

    public PaginationResult<TaskRun> searchTaskRuns(TaskRunSearchRequest request) {
        HttpUrl url = buildUrl("/taskruns/_search")
                .build();
        Request postRequest = new Request.Builder().url(url)
                .post(jsonBody(request))
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(postRequest, new TypeReference<PaginationResult<TaskRun>>() {
        });
    }

    public Integer countTaskRuns(TaskRunSearchRequest request) {
        HttpUrl url = buildUrl("/taskruns/_count")
                .addQueryParameter("status", Objects.nonNull(request.getStatus()) ? request.getStatus().name() : "")
                .addQueryParameter("dateFrom", Objects.nonNull(request.getDateFrom()) ? request.getDateFrom().toString() : "")
                .addQueryParameter("dateTo", Objects.nonNull(request.getDateTo()) ? request.getDateTo().toString() : "")
                .addQueryParameter("includeStartedOnly", Objects.nonNull(request.getIncludeStartedOnly()) ? request.getIncludeStartedOnly().toString() : "false")
                .build();
        Request getRequest = new Request.Builder().url(url)
                .get()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(getRequest, Integer.class);
    }

    public Map<Long, List<TaskRun>> getLatestTaskRuns(List<Long> taskIds, int limit) {
        String taskIdsQueryString = String.join(",",
                taskIds.stream().map(Object::toString).collect(Collectors.toList())
        );
        HttpUrl url = buildUrl(API_TASK_RUNS + "/latest")
                .addQueryParameter("taskIds", taskIdsQueryString)
                .addQueryParameter("limit", String.valueOf(limit))
                .build();
        Request getRequest = new Request.Builder()
                .url(url).get()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(getRequest, new TypeReference<Map<Long, List<TaskRun>>>() {
        });
    }

    public DatasetLineageInfo getLineageNeighbors(Long datasetGid, LineageQueryDirection direction, int depth) {
        Preconditions.checkNotNull(datasetGid);
        Preconditions.checkNotNull(direction);
        Preconditions.checkArgument(depth > 0, "Invalid depth: {}, should be a positive integer");

        HttpUrl url = buildUrl(API_LINEAGES)
                .addQueryParameter("datasetGid", String.valueOf(datasetGid))
                .addQueryParameter("direction", direction.getQueryParam())
                .addQueryParameter("depth", String.valueOf(depth))
                .build();
        Request getRequest = new Request.Builder()
                .url(url).get()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(getRequest, new TypeReference<DatasetLineageInfo>() {
        });
    }


    public EdgeInfo getLineageEdgeInfo(Long upstreamDatasetGid, Long downstreamDatasetGid) {
        HttpUrl url = buildUrl(API_LINEAGES + "/edges")
                .addQueryParameter("upstreamDatasetGid", String.valueOf(upstreamDatasetGid))
                .addQueryParameter("downstreamDatasetGid", String.valueOf(downstreamDatasetGid))
                .build();
        Request getRequest = new Request.Builder()
                .url(url).get()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(getRequest, new TypeReference<EdgeInfo>() {
        });
    }
}
