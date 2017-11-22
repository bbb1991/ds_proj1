package me.bbb1991.ds.ga1.common.model;

public enum CommandType {
    LIST_FILES, // get a list of available files
    HEARTBEAT, // type of command, that namenode sends to datanode to check, is datanode alive or not
    GET, // download file/chunk of file from datanode
    PUT, // upload file/chunk of file from datanode
    OK,
    MKDIR,
    ERROR,
}
