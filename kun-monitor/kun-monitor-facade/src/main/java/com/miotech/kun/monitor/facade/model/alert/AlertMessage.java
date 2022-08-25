package com.miotech.kun.monitor.facade.model.alert;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AlertMessage {

    private final AlertReason reason;

    private final String dataset;

    private final String task;

    private final String result;

    private final String owner;

    private final String deployer;

    private final String upstreamTask;

    private final Long numberOfContinuousFailure;

    private final String link;

    public AlertMessage(AlertReason reason, String dataset, String task, String result, String owner, String deployer,
                        String upstreamTask, Long numberOfContinuousFailure, String link) {
        Preconditions.checkNotNull(reason);
        this.reason = reason;
        this.dataset = dataset;
        this.task = task;
        this.result = result;
        this.owner = owner;
        this.deployer = deployer;
        this.upstreamTask = upstreamTask;
        this.numberOfContinuousFailure = numberOfContinuousFailure;
        this.link = link;
    }

    public String toMarkdown() {
        return toContent(true);
    }

    public String toContent(boolean isMarkdown) {
        List<String> infos = Lists.newArrayList();
        String reasonInfo = isMarkdown ? String.format("<font color=\"%s\">Reason: %s</font>", this.reason.getWeComMarkdownColor(), this.reason.getDisplayName()) :
                String.format("Reason: %s", this.reason.getDisplayName());
        infos.add(reasonInfo);

        if (StringUtils.isNotBlank(this.dataset)) {
            String datasetInfo = isMarkdown ? String.format("**Dataset**: %s", this.dataset) : String.format("Dataset: %s", this.dataset);
            infos.add(datasetInfo);
        }

        if (StringUtils.isNotBlank(this.task)) {
            String taskInfo = isMarkdown ? String.format("**Task**: %s", this.task) : String.format("Task: %s", this.task);
            infos.add(taskInfo);
        }

        if (StringUtils.isNotBlank(this.result)) {
            String resultInfo = isMarkdown ? String.format("**Result**: %s", this.result) : String.format("Result: %s", this.result);
            infos.add(resultInfo);
        }

        if (StringUtils.isNotBlank(this.owner)) {
            String ownerInfo = isMarkdown ? String.format("**Owner**: %s", this.owner) : String.format("Owner: %s", this.owner);
            infos.add(ownerInfo);
        }

        if (StringUtils.isNotBlank(this.deployer)) {
            String deployerInfo = isMarkdown ? String.format("**Deployer**: %s", this.deployer) : String.format("Deployer: %s", this.deployer);
            infos.add(deployerInfo);
        }

        if (StringUtils.isNotBlank(this.upstreamTask)) {
            String upstreamTaskInfo = isMarkdown ? String.format("**Upstream Task**: %s", this.upstreamTask) : String.format("Upstream Task: %s", this.upstreamTask);
            infos.add(upstreamTaskInfo);
        }

        if (this.numberOfContinuousFailure != null) {
            String numberOfContinuousFailureInfo = isMarkdown ? String.format("**Number of continuous failure**: %s", this.numberOfContinuousFailure) :
                    String.format("Number of continuous failure: %s", this.numberOfContinuousFailure);
            infos.add(numberOfContinuousFailureInfo);
        }

        if (StringUtils.isNotBlank(this.link)) {
            String linkInfo = isMarkdown ? String.format("**Link**: [%s](%s)", this.link, this.link) : String.format("Link: %s", this.link);
            infos.add(linkInfo);
        }

        return StringUtils.join(infos, "\n");
    }

    public enum AlertReason {
        NOTIFICATION("Notification", "info")
        , OVERDUE("Overdue", "warning")
        , FAILURE("Failure", "warning")
        ;

        private String displayName;
        private String weComMarkdownColor;

        AlertReason(String displayName, String weComMarkdownColor) {
            this.displayName = displayName;
            this.weComMarkdownColor = weComMarkdownColor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getWeComMarkdownColor() {
            return weComMarkdownColor;
        }

        public static AlertReason from(String displayName) {
            Preconditions.checkNotNull(displayName);
            for (AlertReason value : values()) {
                if (value.getDisplayName().equals(displayName)) {
                    return value;
                }
            }

            throw new IllegalArgumentException("Invalid displayName: " + displayName);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private AlertReason reason;
        private String dataset;
        private String task;
        private String result;
        private String owner;
        private String deployer;
        private String upstreamTask;
        private Long numberOfContinuousFailure;
        private String link;

        private Builder() {
        }

        public Builder withReason(AlertReason reason) {
            this.reason = reason;
            return this;
        }

        public Builder withDataset(String dataset) {
            this.dataset = dataset;
            return this;
        }

        public Builder withTask(String task) {
            this.task = task;
            return this;
        }

        public Builder withResult(String result) {
            this.result = result;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder withDeployer(String deployer) {
            this.deployer = deployer;
            return this;
        }

        public Builder withUpstreamTask(String upstreamTask) {
            this.upstreamTask = upstreamTask;
            return this;
        }

        public Builder withNumberOfContinuousFailure(Long numberOfContinuousFailure) {
            this.numberOfContinuousFailure = numberOfContinuousFailure;
            return this;
        }

        public Builder withLink(String link) {
            this.link = link;
            return this;
        }

        public AlertMessage build() {
            return new AlertMessage(reason, dataset, task, result, owner, deployer, upstreamTask, numberOfContinuousFailure, link);
        }
    }
}
