package me.bbb1991.ds.ga1.namenode;

import org.apache.hadoop.ipc.VersionedProtocol;

public interface NameNodeProtocol extends VersionedProtocol {

    long versionID = 1L;

}
