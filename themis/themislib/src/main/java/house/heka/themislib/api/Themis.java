package house.heka.themislib.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.SodiumJNI;
import org.libsodium.jni.crypto.Box;
import org.libsodium.jni.crypto.Hash;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.crypto.SecretBox;
import org.libsodium.jni.encoders.Encoder;
import org.libsodium.jni.encoders.Hex;
import org.libsodium.jni.keys.KeyPair;
import org.libsodium.jni.keys.PrivateKey;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import house.heka.local.models.AuthKey;
import house.heka.local.models.ThemisItem;
import house.heka.local.models.ThemisKey;
import house.heka.themislib.ThemisActivity;
import house.heka.themislib.model.secure.LocalEncryptedContent;
import house.heka.themislib.model.secure.RemoteEncryptedContent;
import house.heka.themislib.model.secure.keys.local.LocalEncryptionKey;
import house.heka.themislib.model.secure.keys.local.TransientKey;

public class Themis {

    public static final int BASE64_FLAGS = Base64.DEFAULT;
    private static final String TAG = "Themis";
    private final SharedPreferences storage;
    public KeyStore mAndroidKeys = null;
    private LocalEncryptionKey deviceEncryption;
    private LocalEncryptionKey passEncryption;

    private static byte[] decrypt(
            byte[] encrypted,
            byte[] nonce,
            byte[] pwnonce,
            byte[] iv,
            LocalEncryptionKey deviceEncryption,
            LocalEncryptionKey passEncryption,
            KeyStore androidKeys) {
        SecretBox crypto = new SecretBox(deviceEncryption.retrievePrivKeyBytes());
        SecretBox pwcrypto = new SecretBox(passEncryption.retrievePrivKeyBytes());

        //unwrap android encryption
        byte[] unwrapped = androidDecrypt(encrypted, iv, androidKeys);

        //unwrap password encryption
        byte[] pwunwrapped = pwcrypto.decrypt(pwnonce, unwrapped);

        //finally unwrap device encryption
        return crypto.decrypt(nonce, pwunwrapped);
    }

    private static byte[] decryptRemoteContent(RemoteEncryptedContent rec, KeyStore androidKeys, Themis themis) {
        List<ThemisKey> keyed = ThemisKey.findKeyForTag(rec.tag);
        if (keyed.size() < 1) {
            Log.d(TAG,"decryptRemoteContent failed: key not found");
            return new byte[0];
        }

        //the key will decrypt itself upon retrieval
        ThemisKey key = keyed.get(0);

        //unwrap device encryption
        Box crypto = new Box(rec.remote,key.getKey(themis));

        return crypto.decrypt(rec.nonce, rec.content);
    }

    public static boolean androidVerify(byte[] signed, byte[] toVerify, byte[] pubKey, KeyStore ks) {

        try {
            KeyStore.Entry entry = ks.getEntry(ThemisActivity.KEY_ALIAS_SIGN, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w(TAG, "Not an instance of a PrivateKeyEntry");
                return false;
            }
            Signature s = Signature.getInstance("SHA256withECDSA");
            PublicKey publicKey =
                    KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(pubKey));
            s.initVerify(publicKey);
            s.update(signed);
            return s.verify(toVerify);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | SignatureException | KeyStoreException | InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] androidSign(byte[] toSign, KeyStore ks) {
        KeyStore.Entry entry = null;
        try {
            entry = ks.getEntry(ThemisActivity.KEY_ALIAS_SIGN, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w(TAG, "Not an instance of a PrivateKeyEntry");
                return null;
            }
            Signature s = Signature.getInstance("SHA256withECDSA");
            s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            s.update(toSign);
            return s.sign();

        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | SignatureException | KeyStoreException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[][] androidEncrypt(byte[] to_enc, KeyStore androidKey) {
        byte[][] result  = new byte[2][0];
        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) androidKey.getEntry(ThemisActivity.KEY_ALIAS,null);
            SecretKey key = entry.getSecretKey();
            Cipher c =  Cipher.getInstance("AES/CBC/PKCS7Padding");

            c.init(Cipher.ENCRYPT_MODE, key);
            result[0] = c.getIV();
            result[1] = c.doFinal(to_enc);
            return result;
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | KeyStoreException | NoSuchPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] androidDecrypt(byte[] to_dec, byte[] iv, KeyStore androidKey) {

        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) androidKey.getEntry(ThemisActivity.KEY_ALIAS,null);
            SecretKey key = entry.getSecretKey();
            Cipher c =  Cipher.getInstance("AES/CBC/PKCS7Padding");

            c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return c.doFinal(to_dec);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException | UnrecoverableEntryException | BadPaddingException | KeyStoreException | NoSuchPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Themis(ThemisActivity active, KeyStore keyStore) {
        mAndroidKeys = keyStore;
        storage = active.getPreferences(Context.MODE_PRIVATE);
        generate();
    }

    private void generate() {
        generateDeviceEncryption();
        generateTransientEncryption();
    }

    private void generateDeviceEncryption() {

        if (LocalEncryptionKey.isSecure(storage)) {
            deviceEncryption = LocalEncryptionKey.restoreKeys(storage,mAndroidKeys);
        } else {
            byte[] hashSalt = new Random().randomBytes(32);
            storage.edit().putString("hashSalt",Base64.encodeToString(hashSalt,Themis.BASE64_FLAGS)).apply();

            //AES requires IV which is first element, content payload is second
            byte[][] encryptedSecret = androidEncrypt(
                    new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES),
                    mAndroidKeys);
            if (encryptedSecret != null && encryptedSecret.length > 1)
                deviceEncryption = new LocalEncryptionKey(
                        encryptedSecret[0],
                        encryptedSecret[1],
                        mAndroidKeys,
                        storage);
            else
                Log.e(TAG,"encryption failed");
        }

    }

    private void generateTransientEncryption() {
        String hashkey = new Hash().pwhash_scryptsalsa208sha256(
                Base64.encodeToString("newPassword".getBytes(), Themis.BASE64_FLAGS),
                new Hex(),
                Base64.decode(storage.getString("hashSalt",""),Themis.BASE64_FLAGS),
                32768,
                32768);

        byte[] hashSecret = Arrays.copyOfRange(
                Base64.decode(hashkey,Themis.BASE64_FLAGS),
                0,
                SodiumConstants.SECRETKEY_BYTES);

        byte[][] encryptedHashSecret = androidEncrypt(hashSecret, mAndroidKeys);

        assert encryptedHashSecret != null;

        passEncryption = new TransientKey(encryptedHashSecret[0], encryptedHashSecret[1], mAndroidKeys, storage);
    }


    private TransientKey generateTransientEncryption(byte[] pass) {
        String hashkey = new Hash().pwhash_scryptsalsa208sha256(
                Base64.encodeToString(pass, Themis.BASE64_FLAGS),
                new Hex(),
                Base64.decode(storage.getString("hashSalt",""),Themis.BASE64_FLAGS),
                32768,
                32768);

        byte[] hashSecret = Arrays.copyOfRange(
                Base64.decode(hashkey,Themis.BASE64_FLAGS),
                0,
                SodiumConstants.SECRETKEY_BYTES);

        byte[][] encryptedHashSecret = androidEncrypt(hashSecret, mAndroidKeys);

        assert encryptedHashSecret != null;

        return new TransientKey(encryptedHashSecret[0], encryptedHashSecret[1], mAndroidKeys, storage);
    }

    @NonNull
    private RemoteEncryptedContent getRemoteEncryptedContent(String content) {

        byte[] contented = content.getBytes();
        byte[] nonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);

        //first encrypt using device encryption pub key
        byte[] seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);

        //only need public key to generate secret
        byte[] remotePubKey = new KeyPair(seed).getPublicKey().toBytes();

        //only need private key for secret generation, but it is private so make sure it's encrypted
        LocalEncryptedContent encryptedPrivateKey  = getLocalEncryptedContent(new KeyPair(seed).getPrivateKey().toBytes());

        Box crypto = new Box(remotePubKey, decryptLocalBytes(encryptedPrivateKey));

        byte[] ciphertext = crypto.encrypt(nonce,contented);

        String tag = Base64.encodeToString(new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES), Themis.BASE64_FLAGS);

        ThemisKey keyed = new ThemisKey(tag,encryptedPrivateKey);
        keyed.save();

        return new RemoteEncryptedContent(nonce, ciphertext, contented.length, remotePubKey, tag);
    }

    @NonNull
    private LocalEncryptedContent getLocalEncryptedContent(String content) {
        return getLocalEncryptedContent(content.getBytes());
    }

    private LocalEncryptedContent getLocalEncryptedContent(byte[] contented) {
        byte[] nonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);

        //first encrypt using device encryption key
        SecretBox crypto = new SecretBox(deviceEncryption.retrievePrivKeyBytes());
        byte[] ciphertext = crypto.encrypt(nonce, contented);


        byte[] pwnonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);
        //next encrypt using password encryption key
        SecretBox pwcrypto = new SecretBox(passEncryption.retrievePrivKeyBytes());
        byte[] pwciphertext = pwcrypto.encrypt(pwnonce, ciphertext);

        //finally encrypt using keystore
        byte[][] encryptedSign = androidEncrypt(pwciphertext, mAndroidKeys);

        assert encryptedSign != null;

        return new LocalEncryptedContent(nonce, pwnonce, encryptedSign[0], encryptedSign[1]);
    }

    private LocalEncryptedContent updateLocalEncryptedContent(TransientKey newPass, byte[] contented) {
        byte[] nonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);

        //first encrypt using device encryption key
        SecretBox crypto = new SecretBox(deviceEncryption.retrievePrivKeyBytes());
        byte[] ciphertext = crypto.encrypt(nonce, contented);


        byte[] pwnonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);
        //next encrypt using password encryption key
        SecretBox pwcrypto = new SecretBox(newPass.retrievePrivKeyBytes());
        byte[] pwciphertext = pwcrypto.encrypt(pwnonce, ciphertext);

        //finally encrypt using keystore
        byte[][] encryptedSign = androidEncrypt(pwciphertext, mAndroidKeys);

        assert encryptedSign != null;

        return new LocalEncryptedContent(nonce, pwnonce, encryptedSign[0], encryptedSign[1]);
    }

    public LocalEncryptedContent encryptLocal(String toEnc) {
        return getLocalEncryptedContent(toEnc);
    }

    public String decryptLocal(LocalEncryptedContent toDec) {
        String result = null;
        try {
            result = new String(decryptLocalBytes(toDec), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] decryptLocalBytes(LocalEncryptedContent toDec) {
        return decrypt(
                toDec.content,
                toDec.nonce,
                toDec.pwnonce,
                toDec.iv,
                deviceEncryption,
                passEncryption,
                mAndroidKeys);
    }

    public RemoteEncryptedContent encryptRemote(String toEnc) {
        return getRemoteEncryptedContent(toEnc);
    }

    public String decryptRemote(RemoteEncryptedContent toDec) {
        byte[] decrypted = decryptRemoteContent(toDec,mAndroidKeys,this);
        String result = null;
        try {
            result = new String(decrypted, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ;
        return result;
    }

    public void updatePassword(byte[] newPassword, List<ThemisItem> themisItems) {
        TransientKey newPassKey = generateTransientEncryption(newPassword);
        for (ThemisItem ti: themisItems) {
            updatePassEncryption(newPassKey, ti);
        }
        List<ThemisKey> keys = ThemisKey.listAll(ThemisKey.class);
        for (ThemisKey tk: keys) {
            updatePassEncryption(newPassKey, tk);
        }
        passEncryption = newPassKey;
    }
    private void updatePassEncryption(TransientKey newPassKey, ThemisAPI ta) {
        LocalEncryptedContent newContent = updateLocalEncryptedContent(newPassKey,ta.getBytes(this));
        ta.replaceContent(newContent);
    }

    public String randomSeedString() {
        return Base64.encodeToString(new Random().randomBytes(32),Themis.BASE64_FLAGS);
    }

    public AuthKey getAuthKey() {
        List<AuthKey> aks = AuthKey.listAll(AuthKey.class);
        if (aks.size() > 0) {
            return aks.get(0);
        } else {
            return createDeviceKey(randomSeedString());
        }
    }
    private AuthKey createDeviceKey(String seed) {
        LocalEncryptedContent encrypted = encryptLocal(new Hash().pwhash_scryptsalsa208sha256(
                seed,
                new Hex(),
                Base64.decode(randomSeedString(),Themis.BASE64_FLAGS),
                32768,
                32768));
        AuthKey ak = new AuthKey(encrypted);
        ak.save();
        return ak;
    }

    public String getStaticHash(String seed) {
        return new Hash().pwhash_scryptsalsa208sha256(
                seed,
                new Hex(),
                Base64.decode(storage.getString("hashSalt",""), Themis.BASE64_FLAGS),
                32768,
                32768);

    }
}
