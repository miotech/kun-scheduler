package com.miotech.kun.dataplatform.common.utils;

import com.miotech.kun.workflow.core.model.common.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TagUtils {
    private TagUtils() {}

    public static final String TAG_PROJECT_NAME = "project";
    public static final String TAG_PROJECT_VALUE = "KUN_DATA_PLATFORM";

    public static final String TAG_TASK_TYPE_NAME = "type";
    public static final String TAG_TASK_TYPE_MANUAL = "manual_run";
    public static final String TAG_TASK_TYPE_SCHEDULED = "scheduled";

    public static final String TAG_ENV_NAME = "env";
    public static final String TAG_ENV_PROD = "PROD";
    public static final String TAG_ENV_DEV = "DEV";

    public static final String TAG_TASK_TEMPLATE_NAME = "taskTemplateName";
    public static final String TAG_OWNER_NAME = "owner";
    public static final String TAG_CREATOR_NAME = "creator";
    public static final String TAG_TASK_DEFINITION_ID_NAME = "definitionId";
    public static final String TAG_TASK_COMMIT_ID_NAME = "commitId";
    public static final String TAG_TASK_IS_ARCHIVED_NAME = "isArchived";

    public static List<Tag> buildTryRunTags(Long creator, Long definitionId) {
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_PROJECT_NAME, TAG_PROJECT_VALUE);
        tags.put(TAG_ENV_NAME, TAG_ENV_DEV);
        tags.put(TAG_TASK_TYPE_NAME, TAG_TASK_TYPE_MANUAL);
        tags.put(TAG_CREATOR_NAME, creator.toString());
        tags.put(TAG_TASK_DEFINITION_ID_NAME, definitionId.toString());
        return mapToTags(tags);
    }
    public static List<Tag> buildScheduleRunTags(Long definitionId,
                                                 Long commitId,
                                                 String taskTemplateName,
                                                 Long owner) {
        return buildScheduleRunTags(definitionId, commitId, taskTemplateName, owner, false);
    }

    public static List<Tag> buildScheduleRunTags(Long definitionId,
                                                 Long commitId,
                                                 String taskTemplateName,
                                                 Long owner,
                                                 Boolean isArchived) {
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_PROJECT_NAME, TAG_PROJECT_VALUE);
        tags.put(TAG_ENV_NAME, TAG_ENV_PROD);
        tags.put(TAG_TASK_TYPE_NAME, TAG_TASK_TYPE_SCHEDULED);
        tags.put(TAG_OWNER_NAME, owner.toString());
        tags.put(TAG_TASK_COMMIT_ID_NAME, commitId.toString());
        tags.put(TAG_TASK_TEMPLATE_NAME,taskTemplateName);
        tags.put(TAG_TASK_DEFINITION_ID_NAME, definitionId.toString());
        tags.put(TAG_TASK_IS_ARCHIVED_NAME, isArchived ? "true": "false");
        return mapToTags(tags);
    }

    public static Optional<String> getTagValue(List<Tag> tags, String tagKey) {
        return tags.stream()
                .filter(t -> t.getKey().equals(tagKey))
                .findAny()
                .map(Tag::getValue);
    }

    public static List<Tag> buildScheduleSearchTags() {
        return buildScheduleSearchTags(null, null);
    }

    public static List<Tag> buildScheduleSearchTags(Long definitionId,
                                                    String taskTemplateName) {
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_PROJECT_NAME, TAG_PROJECT_VALUE);
        tags.put(TAG_ENV_NAME, TAG_ENV_PROD);
        tags.put(TAG_TASK_TYPE_NAME, TAG_TASK_TYPE_SCHEDULED);
        if (definitionId != null) {
            tags.put(TAG_TASK_DEFINITION_ID_NAME, definitionId.toString());
        }
        if (taskTemplateName != null) {
            tags.put(TAG_TASK_TEMPLATE_NAME, taskTemplateName);
        }
        return  mapToTags(tags);
    }

    private static List<Tag> mapToTags(Map<String, String> tags) {
        return tags.entrySet()
                .stream()
                .map( x -> new Tag(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }

}
