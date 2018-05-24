package network.elrond.application;


import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PConnection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppState implements Serializable {

    private boolean stillRunning = true;
    private boolean bootstrapping = false;

    private Blockchain blockchain;

    private Accounts accounts;

    private P2PConnection connection;
    private Map<String, P2PBroadcastChanel> channels = new HashMap<>();


    public P2PBroadcastChanel getChanel(String name) {
        return channels.get(name);
    }

    public void addChanel(String name, P2PBroadcastChanel chanel) {
        this.channels.put(name, chanel);
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        this.connection = connection;
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public void setStillRunning(boolean stillRunning) {
        this.stillRunning = stillRunning;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }

    public boolean isBootstrapping() {
        return bootstrapping;
    }

    public void setBootstrapping(boolean bootstrapping) {
        this.bootstrapping = bootstrapping;
    }

}