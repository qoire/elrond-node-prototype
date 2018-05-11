package network.elrond.crypto;

public class ECKeyPair implements KeyPair {

    PrivateKey privateKey;
    PublicKey publicKey;

    /**
     * Default constructor
     * Creates a new pair of (private, public) keys
     */
    public ECKeyPair() {

    }

    /**
     * Constructor
     * Creates the pair of (private, public) keys from the private key
     *
     * @param privateKey the private key
     */
    public ECKeyPair(PrivateKey privateKey){
        this.privateKey = privateKey;
        publicKey = new PublicKey(privateKey);
    }

    /**
     * Getter for the private key
     *
     * @return the private key
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Getter for the public key
     *
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public KeyPair clone() throws CloneNotSupportedException {
        return (KeyPair) super.clone();
    }
}
