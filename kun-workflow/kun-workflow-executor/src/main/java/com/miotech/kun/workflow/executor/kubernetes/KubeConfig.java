package com.miotech.kun.workflow.executor.kubernetes;

public class KubeConfig {

    private String version;
    private String jarDirectory;
    private String logPath;
    private String url;
    private String oauthToken;
    private String caCertFile;
    private String nfsName;
    private String nfsClaimName;
    private String namespace;
    private String image;
    private String caCert;
    private String clientCert;
    private String clientKey;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJarDirectory() {
        return jarDirectory;
    }

    public void setJarDirectory(String jarDirectory) {
        this.jarDirectory = jarDirectory;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public String getCaCertFile() {
        return caCertFile;
    }

    public void setCaCertFile(String caCertFile) {
        this.caCertFile = caCertFile;
    }

    public String getNfsName() {
        return nfsName;
    }

    public void setNfsName(String nfsName) {
        this.nfsName = nfsName;
    }

    public String getNfsClaimName() {
        return nfsClaimName;
    }

    public void setNfsClaimName(String nfsClaimName) {
        this.nfsClaimName = nfsClaimName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCaCert() {
        return caCert;
    }

    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    public String getClientCert() {
        return clientCert;
    }

    public void setClientCert(String clientCert) {
        this.clientCert = clientCert;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }
}
