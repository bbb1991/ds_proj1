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
    private int port;

    /**
     * Marker that shows is datanode alive or not. Used by namenode for deleting dead datanodes from list
     */
    private boolean alive;

    public DataNode() {
    }

    public DataNode(String host, int port) {
        this(host, port, true);
    }

    public DataNode(String host, int port, boolean alive) {
        this.host = host;
        this.port = port;
        this.alive = alive;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
