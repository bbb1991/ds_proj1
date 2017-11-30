package me.bbb1991.ds.ga1.common.model;

public enum CommandType {
    LIST_FILES, // get a list of available files
    HEARTBEAT, // type of command, that name node sends to data node to check, is data node alive or not
    GET, // download file/chunk of file from data node
    MKDIR, // create dir
    UPLOAD_FILE,
    HELLO, // this command sends datanode to namenode, when datanode starts
    REMOVE, // remove file/folder
    GET_ID, // request file ID.
    RENAME, // rename file/folder
    SYNC, UPLOADED, // copy all files from master datanode to new connected datanodes
}
