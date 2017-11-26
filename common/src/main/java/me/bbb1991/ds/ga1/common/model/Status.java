package me.bbb1991.ds.ga1.common.model;

/**
 * Enumeration of request responds
 *
 * @author Bagdat Bimaganbetov
 * @author b.bimaganbetov@innopolis.ru
 */
public enum Status {
    OK, // Request was successfully parsed
    NO_DATANODE_AVAILABLE, FILE_NOT_FOUND, // No datanode available for saving file
}
