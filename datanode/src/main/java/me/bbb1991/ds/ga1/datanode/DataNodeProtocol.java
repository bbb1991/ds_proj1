package me.bbb1991.ds.ga1.datanode;

import org.apache.hadoop.ipc.VersionedProtocol;

public interface DataNodeProtocol extends VersionedProtocol {

    long versionID = 1L;
}
