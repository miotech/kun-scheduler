package com.miotech.kun.workflow.core.model.resource;

import java.util.Objects;

public class ResourceQueue{
    private final String queueName;
    private final Integer cores;
    private final Integer memory;
    private final Integer workerNumbers;

    public ResourceQueue(String queueName, Integer cores, Integer memory, Integer workerNumbers) {
        this.queueName = queueName;
        this.cores = cores;
        this.memory = memory;
        this.workerNumbers = workerNumbers;
    }

    public String getQueueName() {
        return queueName;
    }

    public Integer getCores() {
        return cores;
    }

    public Integer getMemory() {
        return memory;
    }

    public Integer getWorkerNumbers() {
        return workerNumbers;
    }

    public ResourceQueueBuilder cloneBuilder(){
        return newBuilder()
                .withQueueName(queueName)
                .withCores(cores)
                .withMemory(memory)
                .withWorkerNumbers(workerNumbers);
    }

    public static ResourceQueueBuilder newBuilder(){
        return new ResourceQueueBuilder();
    }



    @Override
    public String toString() {
        return "ResourceQueue{" +
                "queueName='" + queueName + '\'' +
                ", cores=" + cores +
                ", memory=" + memory +
                ", workerNumbers=" + workerNumbers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceQueue)) return false;
        ResourceQueue that = (ResourceQueue) o;
        return queueName.equals(that.queueName) &&
                Objects.equals(cores, that.cores) &&
                Objects.equals(memory, that.memory) &&
                Objects.equals(workerNumbers, that.workerNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueName, cores, memory, workerNumbers);
    }


    public static final class ResourceQueueBuilder {
        private String queueName;
        private Integer cores;
        private Integer memory;
        private Integer workerNumbers;

        private ResourceQueueBuilder() {
        }

        public static ResourceQueueBuilder aResourceQueue() {
            return new ResourceQueueBuilder();
        }

        public ResourceQueueBuilder withQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public ResourceQueueBuilder withCores(Integer cores) {
            this.cores = cores;
            return this;
        }

        public ResourceQueueBuilder withMemory(Integer memory) {
            this.memory = memory;
            return this;
        }

        public ResourceQueueBuilder withWorkerNumbers(Integer workerNumbers) {
            this.workerNumbers = workerNumbers;
            return this;
        }

        public ResourceQueue build() {
            return new ResourceQueue(queueName, cores, memory, workerNumbers);
        }
    }
}
