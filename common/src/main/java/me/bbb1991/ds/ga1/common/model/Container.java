package me.bbb1991.ds.ga1.common.model;

import java.io.Serializable;

public class Container<T> implements Serializable {

    private CommandType commandType;

    private T object;

    private String objectName;

    public Container(CommandType commandType) {
        this(commandType, null);
    }

    public Container(CommandType commandType, T object) {
        this(commandType, object, null);
    }

    public Container(CommandType commandType, T object, String objectName) {
        this.commandType = commandType;
        this.object = object;
        this.objectName = objectName;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public CommandType getStatus() {
        return getCommandType();
    }

    @Override
    public String toString() {
        return commandType.name();
    }
}
