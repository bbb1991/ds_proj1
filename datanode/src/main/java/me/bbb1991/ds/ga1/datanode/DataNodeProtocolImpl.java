package me.bbb1991.ds.ga1.datanode;

import org.apache.hadoop.ipc.ProtocolSignature;

import java.io.IOException;

public class DataNodeProtocolImpl implements DataNodeProtocol {
    public long getProtocolVersion(String s, long l) throws IOException {
        return 1;
    }

    public ProtocolSignature getProtocolSignature(String s, long l, int i) throws IOException {
        return new ProtocolSignature(1, null);
    }
}
