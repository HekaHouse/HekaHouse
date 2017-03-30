package house.heka.themislib.model.secure;

public class LocalEncryptedContent {
    public byte[] pwnonce;
    public byte[] iv;
    public byte[] nonce;
    public byte[] content;
    public LocalEncryptedContent(byte[] nonce, byte[] pwnonce, byte[] iv, byte[] content) {
        this.pwnonce = pwnonce;
        this.nonce = nonce;
        this.iv = iv;
        this.content = content;
    }
}

