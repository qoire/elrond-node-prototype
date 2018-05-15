package network.elrond.crypto;

import network.elrond.core.Util;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrivateKey {
    private BigInteger privateKey;
    private static final X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");

    public static BigInteger getCurveOrder() {
        return EC_PARAMETERS.getN();
    }

    public static final X9ECParameters getEcParameters() {
        return EC_PARAMETERS;
    }

    /**
     * Default constructor
     * Creates a new private key
     */
    public PrivateKey() {
        AsymmetricCipherKeyPair keyPair;
        ECDomainParameters domainParameters;
        ECKeyGenerationParameters keyParameters;
        ECKeyPairGenerator keyPairGenerator;

        domainParameters = new ECDomainParameters(EC_PARAMETERS.getCurve(),
                EC_PARAMETERS.getG(),
                EC_PARAMETERS.getN(),
                EC_PARAMETERS.getH(),
                EC_PARAMETERS.getSeed());

        keyParameters = new ECKeyGenerationParameters(
                domainParameters,
                new SecureRandom());

        keyPairGenerator = new ECKeyPairGenerator();
        keyPairGenerator.init(keyParameters);
        keyPair = keyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        this.privateKey = privateKey.getD();
    }

    /**
     * Checks if the private key is valid
     *
     * @return true if private key is valid, false otherwise
     */
    public boolean isValid() {
        BigInteger unsignedPrivateKey = new BigInteger(1, privateKey.toByteArray());
        if ((-1 != unsignedPrivateKey.compareTo(EC_PARAMETERS.getN())) &&
                (1 != unsignedPrivateKey.compareTo(BigInteger.ZERO))) {
            return false;
        }
        return true;
    }

    /**
     * Creates a private key from a byte array
     *
     * @param src The byte array
     */
    public PrivateKey(byte[] src) {
        privateKey = new BigInteger(src);
    }

    /**
     * Constructor
     * Generates a private key starting from String
     *
     * @param seed String seed to generate the private key
     */
    public PrivateKey(String seed) {
        BigInteger primeOrder = EC_PARAMETERS.getN();
        byte[] seedArray = seed.getBytes();
        boolean validPrivateKey = false;

        seedArray = Util.SHA3.digest(seedArray);
        privateKey = new BigInteger(1, seedArray);

        // to be a valid private key it needs to verify:
        // 0 < pk < n, where n is the order of the largest prime order subgroup
        // consider BigInteger will be interpreted as negative when first bit is 1
        // so do the check on unsigned but don't store the extra 00 byte
        while ((-1 != privateKey.compareTo(primeOrder)) && (1 != privateKey.compareTo(BigInteger.ZERO))) {
            seedArray = Util.SHA3.digest(seedArray);
            privateKey = new BigInteger(1, seedArray);
        }

        // store without the extra byte in case of unsigned
        privateKey = new BigInteger(seedArray);
    }

    /**
     * Getter for the private key
     *
     * @return the private key
     */
    public BigInteger getValue() {
        return privateKey;
    }

    /**
     * Setter for the private key
     *
     * @param privateKey
     */
    public void setPrivateKey(BigInteger privateKey) {
        this.privateKey = privateKey;
    }
}