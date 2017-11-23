package me.bbb1991.ds.ga1.common.model;


import com.google.gson.Gson;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Model with info about file or chunk of file: in which server located and actually what name it has.
 *
 * @author Bagdat Bimaganbetov
 * @author b.bimaganebetov@innopolis.ru
 */

@Entity
public class Chunk implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * In what name user stored file. For example: file.txt
     */
    private String originalName;

    /**
     * In what name it stores in namenode. For example: 20171101080622
     */
    @Column
    private String filename;

    /**
     * If file splitted into chunks which chink it is, if file it will be always 1.
     */
    @Column
    private int seqNo;

    /**
     * Hostname, where client can download file
     */
    @Column
    private String nameNodeHost;

    /**
     * Which port listens datanode
     */
    @Column
    private int nameNodePort;

    /**
     * File size
     */
    @Column
    private long fileSize;

    /**
     * File type.
     *
     * @see FileType
     */
    @Column
    @Enumerated(EnumType.ORDINAL)
    private FileType datatype;

    /**
     * If it is file are subfolder, that it means, we have parent folder.
     */
    @Column
    private long parentId;

    /**
     * Is file available for access or not. File might be locked when file is processing, for examlpe,
     * moving or uploading.
     */
    @Column
    private boolean locked;

    public Chunk() {
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public String getNameNodeHost() {
        return nameNodeHost;
    }

    public void setNameNodeHost(String nameNodeHost) {
        this.nameNodeHost = nameNodeHost;
    }

    public int getNameNodePort() {
        return nameNodePort;
    }

    public void setNameNodePort(int nameNodePort) {
        this.nameNodePort = nameNodePort;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public FileType getDatatype() {
        return datatype;
    }

    public void setDatatype(FileType datatype) {
        this.datatype = datatype;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public static Builder builder() {
        return new Chunk().new Builder();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public class Builder {

        private Builder() {}

        public Builder setOriginalName(String name) {
            Chunk.this.setOriginalName(name);
            return this;
        }

        public Builder setDatatype(FileType fileType) {
            Chunk.this.setDatatype(fileType);
            return this;
        }

        public Builder setLocked(boolean locked) {
            Chunk.this.setLocked(locked);
            return this;
        }

        public Chunk build() {
            return Chunk.this;
        }
    }
}
