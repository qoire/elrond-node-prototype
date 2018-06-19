package network.elrond.blockchain;

import junit.framework.TestCase;
import network.elrond.account.*;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.service.AppServiceProvider;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlockchainProcessTest extends BaseBlockchainTest {

    @Ignore
    @Test
    public void testProcessBlock() throws Exception {
        PrivateKey privateKeySender = new PrivateKey("PRIVATE KEY".getBytes());
        PublicKey publicKeySender = new PublicKey(privateKeySender);
        PrivateKey privateKeyReceiver = new PrivateKey("PRIVATE KEY2".getBytes());
        PublicKey publicKeyReceiver = new PublicKey(privateKeyReceiver);

        Blockchain blockchain = new Blockchain(getDefaultTestBlockchainContext());
        Accounts accounts = initAccounts(publicKeySender);

        byte[] prevBlockHash = null;
        List<String> blocksHashes = new ArrayList<>();

        String senderAddress = Util.getAddressFromPublicKey(publicKeySender.getValue());
        String receiverAddress = Util.getAddressFromPublicKey(publicKeyReceiver.getValue());
        AppBlockManager appBlockManager = new AppBlockManager();
        for (int i = 0; i < 10; i++) {
            Block block = new Block();
            BigInteger nonce = BigInteger.ZERO.add(BigInteger.valueOf(i));
            block.setNonce(nonce);

            if (prevBlockHash != null) {
                block.setPrevBlockHash(prevBlockHash);
            }


            Transaction transaction = AppServiceProvider.getTransactionService().generateTransaction(publicKeySender, publicKeyReceiver,
                    10, i);

            byte[] hash = AppServiceProvider.getSerializationService().getHash(transaction);
            block.getListTXHashes().add(hash);

            String hashString = AppServiceProvider.getSerializationService().getHashString(transaction);
            AppServiceProvider.getTransactionService().signTransaction(transaction, privateKeySender.getValue(), publicKeySender.getValue());
            AppServiceProvider.getBlockchainService().put(hashString, transaction, blockchain, BlockchainUnitType.TRANSACTION);

            appBlockManager.signBlock(block, privateKeySender);

            byte[] blockHash = AppServiceProvider.getSerializationService().getHash(block);
            String blockHashString = AppServiceProvider.getSerializationService().getHashString(block);
            AppServiceProvider.getBlockchainService().put(blockHashString, block, blockchain, BlockchainUnitType.BLOCK);

            blocksHashes.add(blockHashString);

            prevBlockHash = blockHash;
        }

        // Flush memory and read from database engine
        blockchain.flush();
        accounts.flush();


        for (String blockHash : blocksHashes) {
            Block block = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK);
            TestCase.assertTrue(AppServiceProvider.getExecutionService().processBlock(block, accounts, blockchain).isOk());
        }

        System.out.println("SenderAccountState Account state" + AccountAddress.fromBytes(publicKeySender.getValue()));
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromBytes(publicKeySender.getValue()), accounts);
        TestCase.assertEquals(senderAccountState.getBalance(), BigInteger.valueOf(123456689));

        System.out.println("ReceiverAccountState AccountAddress" + AccountAddress.fromBytes(publicKeyReceiver.getValue()));
        AccountState receiverAccountState = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromBytes(publicKeyReceiver.getValue()), accounts);
        TestCase.assertEquals(receiverAccountState.getBalance(),BigInteger.valueOf(100));




    }

    private Accounts initAccounts(PublicKey publicKey) throws IOException, ClassNotFoundException {
        AccountsContext accountContext = new AccountsContext();
        accountContext.setDatabasePath("blockchain.account.data-test");
        Accounts accounts = new Accounts(accountContext, new AccountsPersistenceUnit<>(accountContext.getDatabasePath()));

        AccountAddress address = AccountAddress.fromBytes(publicKey.getValue());
        AccountState accountState = AppServiceProvider.getAccountStateService()
                .getOrCreateAccountState(address, accounts);
        accountState.setBalance(BigInteger.valueOf(123456789));

        AppServiceProvider.getAccountStateService().setAccountState(address, accountState, accounts);

        System.out.println("Initial Account address" + address.toString());

        return accounts;
    }
}
