package me.bbb1991.ds.ga1.namenode;

import org.apache.hadoop.ipc.ProtocolSignature;

import java.io.IOException;

public class NameNodeProtocolImpl implements NameNodeProtocol {
    public long getProtocolVersion(String s, long l) throws IOException {
        return 1L;
    }

    public ProtocolSignature getProtocolSignature(String s, long l, int i) throws IOException {
        return new ProtocolSignature(1L, null);
    }
}
