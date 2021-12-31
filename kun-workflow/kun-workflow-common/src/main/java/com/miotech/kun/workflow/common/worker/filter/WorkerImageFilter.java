package com.miotech.kun.workflow.common.worker.filter;

public class WorkerImageFilter {

    private final Long id;

    private final String name;

    private final Boolean active;

    private final Integer page;

    private final Integer pageSize;

    public WorkerImageFilter(Long id, String name, Boolean active, Integer page, Integer pageSize) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.page = page;
        this.pageSize = pageSize;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getActive() {
        return active;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public static WorkerImageFilterBuilder newBuilder() {
        return new WorkerImageFilterBuilder();
    }


    public static final class WorkerImageFilterBuilder {
        private Long id;
        private String name;
        private Boolean active;
        private Integer page;
        private Integer pageSize;

        private WorkerImageFilterBuilder() {
        }

        public WorkerImageFilterBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public WorkerImageFilterBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public WorkerImageFilterBuilder withActive(Boolean active){
            this.active = active;
            return this;
        }

        public WorkerImageFilterBuilder withPage(Integer page) {
            this.page = page;
            return this;
        }

        public WorkerImageFilterBuilder withPageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public WorkerImageFilter build() {
            return new WorkerImageFilter(id, name, active, page, pageSize);
        }
    }
}
