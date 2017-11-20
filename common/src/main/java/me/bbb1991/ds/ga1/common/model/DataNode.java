package me.bbb1991.ds.ga1.common.model;

public class DataNode {

    private String host;

    private int commandPort;

    public DataNode() {}

    public DataNode(String host, int commandPort) {
        this.host = host;
        this.commandPort = commandPort;
    }

    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getCommandPort() {
        return commandPort;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, commandPort);
    }
}
