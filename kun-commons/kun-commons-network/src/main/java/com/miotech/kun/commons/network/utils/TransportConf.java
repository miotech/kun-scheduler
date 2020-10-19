package com.miotech.kun.commons.network.utils;

import java.util.Locale;
import java.util.Properties;

public class TransportConf {

    public static final String NETWORK_IO_MODE_KEY = "network.io.mode";
    public static final String NETWORK_IO_SERVERTHREADS_KEY = "network.io.serverThreads";
    public static final String NETWORK_IO_CLIENTTHREADS_KEY = "network.io.clientThreads";
    public static final String NETWORK_IO_RECEIVEBUFFER_KEY = "network.io.receiveBuffer";
    public static final String NETWORK_IO_SENDBUFFER_KEY = "network.io.sendBuffer";
    public static final String NETWORK_IO_BACKLOG_KEY = "network.io.backLog";
    public static final String NETWORK_IO_ENABLETCPKEEPALIVE_KEY = "network.io.enableTCPKeepAlive";
    public static final String NETWORK_IO_PREFER_DIRECTBUFS_FORSHAREDBYTEBUFALLOCATORS = "network.io.preferDirectBufsForSharedByteBufAllocators";
    public static final String NETWORK_IO_PREFER_DIRECTBUFS = "network.io.preferDirectBufs";
    public static final String NETWORK_IO_CONNECTIONTIMEOUT_KEY = "network.io.connectionTimeout";
    public static final String NETWORK_VERBOSE_METRICS = "network.verbose.metrics";
    public static final String NETWORK_SHARED_BYTEBUF_ALLOCATORS = "network.sharedByteBufAllocators.enabled";
    public static final String NETWORK_IO_RETRYWAIT_KEY = "network.io.retryWait";
    public static final String NETWORK_IO_NUMCONNECTIONSPERPEER_KEY = "network.io.numConnectionsPerPeer";
    private final String module;
    private final Properties props;

    public TransportConf(String module, Properties props) {
        this.module = module;
        this.props = props;
    }

    public String ioMode() {
        return get(NETWORK_IO_MODE_KEY, "NIO").toUpperCase(Locale.ROOT);
    }

    public String getModuleName() {
        return module;
    }

    public int serverThreads() {
        return getInt(NETWORK_IO_SERVERTHREADS_KEY, 0);
    }

    public int clientThreads() { return getInt(NETWORK_IO_CLIENTTHREADS_KEY, 0); }

    public int backLog() { return getInt(NETWORK_IO_BACKLOG_KEY, -1); }

    public int receiveBuf() { return getInt(NETWORK_IO_RECEIVEBUFFER_KEY, -1); }

    public int sendBuf() {
        return getInt(NETWORK_IO_SENDBUFFER_KEY, -1);
    }

    public boolean enableTcpKeepAlive() {
        return getBoolean(NETWORK_IO_ENABLETCPKEEPALIVE_KEY, false);
    }

    public boolean verboseMetrics() {
        return getBoolean(NETWORK_VERBOSE_METRICS, false);
    }

    public boolean sharedByteBufAllocators() {
        return getBoolean(NETWORK_SHARED_BYTEBUF_ALLOCATORS, false);
    }

    public boolean preferDirectBufsForSharedByteBufAllocators() {
        return getBoolean(NETWORK_IO_PREFER_DIRECTBUFS_FORSHAREDBYTEBUFALLOCATORS, false);
    }

    public boolean preferDirectBufs() {
        return getBoolean(NETWORK_IO_PREFER_DIRECTBUFS, false);
    }

    /** Connect timeout in milliseconds. Default 120 secs. */
    public int connectionTimeoutMs() {
        return (int) getLong(NETWORK_IO_CONNECTIONTIMEOUT_KEY, 120) * 1000;
    }

    public int ioRetryWaitTimeMs() {
        return (int) getLong(NETWORK_IO_RETRYWAIT_KEY, 5) * 1000;
    }

    public int numConnectionsPerPeer() {
        return getInt(NETWORK_IO_NUMCONNECTIONSPERPEER_KEY, 5) * 1000;
    }

    private String get(String name, String defaultValue) {
        return props.containsKey(name) ? props.getProperty(name) : defaultValue;
    }

    private int getInt(String name, int defaultValue) {
        return Integer.parseInt(get(name, Integer.toString(defaultValue)));
    }

    private long getLong(String name, long defaultValue) {
        return Long.parseLong(get(name, Long.toString(defaultValue)));
    }

    private double getDouble(String name, double defaultValue) {
        return Double.parseDouble(get(name, Double.toString(defaultValue)));
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return Boolean.parseBoolean(get(name, Boolean.toString(defaultValue)));
    }
}
