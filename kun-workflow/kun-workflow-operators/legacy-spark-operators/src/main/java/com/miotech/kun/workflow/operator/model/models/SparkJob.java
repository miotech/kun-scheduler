package com.miotech.kun.workflow.operator.model.models;


import java.util.List;
import java.util.Map;

public class SparkJob {
    /**
     * 必须有
     * 包含需要执行应用的文件，主要是jar包
     */
    private String file;
    /**
     * User to impersonate when running the job
     */
    private String proxyUser;
    /**
     * Application Java/Spark main class
     * 主类
     */
    private String className;

    /**
     * Command line arguments for the application
     * 参数
     */
    private List<String> args;

    /**
     * jars to be used in this session
     * 这个任务里面用到的其他 jar 包
     */
    private List<String> jars;

    /**
     * Python files to be used in this session
     */
    private List<String> pyFiles;
    /**
     * files to be used in this session
     */
    private List<String> files;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<String> getJars() {
        return jars;
    }

    public void setJars(List<String> jars) {
        this.jars = jars;
    }

    public List<String> getPyFiles() {
        return pyFiles;
    }

    public void setPyFiles(List<String> pyFiles) {
        this.pyFiles = pyFiles;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getDriverMemory() {
        return driverMemory;
    }

    public void setDriverMemory(String driverMemory) {
        this.driverMemory = driverMemory;
    }

    public String getExecutorMemory() {
        return executorMemory;
    }

    public void setExecutorMemory(String executorMemory) {
        this.executorMemory = executorMemory;
    }

    public List<String> getArchives() {
        return archives;
    }

    public void setArchives(List<String> archives) {
        this.archives = archives;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getConf() {
        return conf;
    }

    public void setConf(Map<String, String> conf) {
        this.conf = conf;
    }

    /**
     * Amount of memory to use for the driver process
     */
    private String driverMemory;

    /**
     * Amount of memory to use per executor process
     */
    private String executorMemory;
    /**
     * Archives to be used in this session
     */
    private List<String> archives;
    /**
     * The name of the YARN queue to which submitted
     */
    private String queue;
    /**
     * The name of this session
     * 任务名称
     */
    private String name;
    /**
     * Spark configuration properties
     * spark 配置文件
     */
    private Map<String, String> conf;
}
