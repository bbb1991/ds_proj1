package me.bbb1991.ds.ga1.common.model;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Class for holding information about datanode.
 *
 * @author Bagdat Bimaganbetov
 * @author b.bimaganbetov@innopolis.ru
 */
public class DataNode implements Serializable {

    /**
     * Data node hostname
     */
    private String host;

    /**
     * Data node port
     */
    private int commandPort;

    public DataNode() {
    }

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
        return new Gson().toJson(this);
    }
}
