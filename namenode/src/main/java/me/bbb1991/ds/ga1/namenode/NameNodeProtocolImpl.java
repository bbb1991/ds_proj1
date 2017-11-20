package me.bbb1991.ds.ga1.namenode;

import org.apache.hadoop.ipc.ProtocolSignature;

import java.io.IOException;

public class NameNodeProtocolImpl implements NameNodeProtocol {

    @Override
    public long getProtocolVersion(String s, long l) throws IOException {
        return versionID;
    }

    @Override
    public ProtocolSignature getProtocolSignature(String s, long l, int i) throws IOException {
        return new ProtocolSignature(versionID, null);
    }
}
