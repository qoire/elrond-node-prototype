package network.elrond.p2p.model;

import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.p2p.RequestHandler;
import network.elrond.p2p.handlers.*;

public enum P2PRequestChannelName {
    ACCOUNT("ACCOUNT", new AccountRequestHandler()),
    BLOCK(BlockchainUnitType.BLOCK.name(), new BlockRequestHandler()),
    BLOCK_INDEX(BlockchainUnitType.BLOCK_INDEX.name(), new BlockIndexRequestHandler()),
    BLOCK_HEIGHT("BLOCK_HEIGHT", new BlockHeightRequestHandler()),
    TRANSACTION(BlockchainUnitType.TRANSACTION.name(), new TransactionRequestHandler()),
    BLOCK_TRANSACTIONS(BlockchainUnitType.BLOCK_TRANSACTIONS.name(), new BlockTransactionsHandler()),
    STATISTICS("STATISTICS", new StatisticsRequestHandler()),;

    private final String name;
    private final RequestHandler<?, P2PRequestMessage> handler;

    private P2PRequestChannelName(final String name, final RequestHandler<?, P2PRequestMessage> handler) {
        this.name = name;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public RequestHandler<?, P2PRequestMessage> getHandler() {
        return handler;
    }

    public static P2PRequestChannelName getFromName(String name) {
        for (P2PRequestChannelName channel : P2PRequestChannelName.values()) {
            if (name.equals(channel.getName())) {
                return channel;
            }
        }
        return null;
    }
}
