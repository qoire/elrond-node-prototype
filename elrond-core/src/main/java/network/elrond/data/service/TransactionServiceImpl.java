package network.elrond.data.service;

import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.crypto.Signature;
import network.elrond.crypto.SignatureService;
import network.elrond.data.BlockUtil;
import network.elrond.data.model.Block;
import network.elrond.data.model.Transaction;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import network.elrond.util.console.AsciiPrinter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The TransactionServiceImpl class implements TransactionService and is used to maintain Transaction objects
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-16
 */
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);
    private SerializationService serializationService = AppServiceProvider.getSerializationService();

    private AsciiPrinter asciiPrinter = AsciiPrinter.instance();

    /**
     * Computes the hash of the complete tx info
     * Used as a mean of tx identification
     *
     * @param tx      transaction
     * @param withSig whether or not to include the signature parts in hash
     * @return hash as byte array
     */
//    public byte[] getHash(Transaction tx, boolean withSig) {
//        String json = AppServiceProvider.getSerializationService().encodeJSON(tx);
//        return (Util.SHA3.get().digest(json.getBytes()));
//    }

    /**
     * Signs the transaction using private keys
     *
     * @param transaction      transaction
     * @param privateKeysBytes private key as byte array
     */
    @Override
	public void signTransaction(Transaction transaction, byte[] privateKeysBytes, byte[] publicKeyBytes) {
        logger.traceEntry("params: {} {} {}", transaction, privateKeysBytes, publicKeyBytes);

        Util.check(transaction != null, "transaction is null");
        Util.check(privateKeysBytes != null, "privateKeysBytes is null");

        logger.trace("Setting signature data to null...");
        transaction.setSignature(null);
        transaction.setChallenge(null);

        byte[] hashNoSigLocal = serializationService.getHash(transaction);

//        tx.setSignature(signature);
//        tx.setChallenge(challenge);


        Signature sig;

        logger.trace("Signing transaction...");
        SignatureService schnorr = AppServiceProvider.getSignatureService();
        sig = schnorr.signMessage(hashNoSigLocal, privateKeysBytes, publicKeyBytes);

        transaction.setSignature(sig.getSignature());
        transaction.setChallenge(sig.getChallenge());
        transaction.setPubKey(Util.byteArrayToHexString(publicKeyBytes));

        logger.traceExit();
    }

    /**
     * Verify the data stored in tx
     *
     * @param transaction to be verified
     * @return true if tx passes all consistency tests
     */
    @Override
	public boolean verifyTransaction(Transaction transaction) {
        logger.traceEntry("params: {}", transaction);

        Util.check(transaction != null, "transaction is null");

        //test 1. consistency checks
        if ((transaction.getNonce().compareTo(BigInteger.ZERO) < 0) ||
                (transaction.getValue().compareTo(BigInteger.ZERO) < 0) ||
                (transaction.getSignature() == null) ||
                (transaction.getChallenge() == null) ||
                (transaction.getSignature().length == 0) ||
                (transaction.getChallenge().length == 0) ||
                (transaction.getSenderAddress().length() != Util.MAX_LEN_ADDR * 2) ||
                (transaction.getReceiverAddress().length() != Util.MAX_LEN_ADDR * 2) ||
                (transaction.getPubKey().length() != Util.MAX_LEN_PUB_KEY * 2)
                ) {
            logger.debug("Failed at conistency check (negative nonce, negative value, sig null or empty, wrong lengths for addresses and pub key)");
            logger.debug(asciiPrinter.transactionAsciiTable(transaction).render());
            return logger.traceExit(false);
        }

        //test 2. verify if sender address is generated from public key used to sign tx
        if (!transaction.getSenderAddress().equals(Util.getAddressFromPublicKey(Util.hexStringToByteArray(transaction.getPubKey())))) {
            logger.debug("Failed at sender address not being generated (or equal) to public key");
            logger.debug(asciiPrinter.transactionAsciiTable(transaction).render());
            return (false);
        }

        //test 3. verify the signature
        byte[] signature = transaction.getSignature();
        byte[] challenge = transaction.getChallenge();

        transaction.setSignature(null);
        transaction.setChallenge(null);

        byte[] message = serializationService.getHash(transaction);

        transaction.setSignature(signature);
        transaction.setChallenge(challenge);

        SignatureService schnorr = AppServiceProvider.getSignatureService();

        boolean isSignatureVerified = schnorr.verifySignature(transaction.getSignature(), transaction.getChallenge(), message, Util.hexStringToByteArray(transaction.getPubKey()));

        if (!isSignatureVerified) {
            logger.debug("Failed at signature verify");
            logger.debug(asciiPrinter.transactionAsciiTable(transaction).render());
        }

        return logger.traceExit(isSignatureVerified);
    }

    @Override
    public List<Transaction> getTransactions(Blockchain blockchain, Block block) throws IOException, ClassNotFoundException {
        logger.traceEntry("params: {} {}", blockchain, block);

        Util.check(blockchain != null, "blockchain is null");
        Util.check(block != null, "block is null");

        List<Transaction> transactions;

        //JLS 2018.05.29 - need to store fetched transaction!
        //BlockchainService appPersistenceService = AppServiceProvider.getAppPersistanceService();
        String blockHash = AppServiceProvider.getSerializationService().getHashString(block);

        List<String> hashes = BlockUtil.getTransactionsHashesAsString(block);

        transactions = AppServiceProvider.getBlockchainService().getAll(hashes, blockchain, BlockchainUnitType.TRANSACTION);

        logger.info("Getting transactions... transactions size: {} hashes size: {}", transactions.size(), hashes.size());
        if (transactions.size() != hashes.size()) {
            transactions = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK_TRANSACTIONS);
        }
        if (transactions != null) {
            for (Transaction transaction : transactions) {
                String transactionHash = AppServiceProvider.getSerializationService().getHashString(transaction);
                AppServiceProvider.getBlockchainService().putLocal(transactionHash, transaction, blockchain, BlockchainUnitType.TRANSACTION);
            }
        } else {
            transactions = new ArrayList<>();
        }
        return logger.traceExit(transactions);
    }

    @Override
    public Transaction generateTransaction(PublicKey sender, PublicKey receiver, long value, long nonce) {
        return generateTransaction(sender, receiver, BigInteger.valueOf(value), BigInteger.valueOf(nonce));
    }

    @Override
    public Transaction generateTransaction(PublicKey sender, PublicKey receiver, BigInteger value, BigInteger nonce) {
        logger.traceEntry("params: {} {} {} {}", sender, receiver, value, nonce);

        Shard senderShard = AppServiceProvider.getShardingService().getShard(sender.getValue());
        Shard receiverShard = AppServiceProvider.getShardingService().getShard(receiver.getValue());

        Transaction t = new Transaction(Util.getAddressFromPublicKey(sender.getValue()),
                Util.getAddressFromPublicKey(receiver.getValue()),
                value,
                nonce,
                senderShard, receiverShard
        );
        t.setPubKey(Util.getAddressFromPublicKey(sender.getValue()));


        return logger.traceExit(t);
    }


}