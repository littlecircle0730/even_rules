package padec.crypto;

import padec.application.Endpoint;
import padec.attribute.Location;
import padec.attribute.PADECContext;
import padec.attribute.Pair;
import padec.filtering.FilteredData;
import padec.filtering.techniques.BasicFuzzy;
import padec.key.Key;
import padec.lock.Keyhole;
import padec.lock.Lock;
import padec.rule.ProducerRule;
import padec.rule.operator.EqualOperator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.util.Arrays;

/**
 * Utility class, manages cryptography tasks with a simple interface
 */
public class SimpleCrypto {
    private static final String KEYGEN_ALGORITHM = "RSA";
    private static final String ENCRYPT_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 512;
    private static final int ENCRYPT_BLOCKSIZE = 53;
    private static final int DECRYPT_BLOCKSIZE = 64;

    private static SimpleCrypto instance = null;

    private KeyPairGenerator generator;

    private SimpleCrypto(){
        try {
            generator = KeyPairGenerator.getInstance(KEYGEN_ALGORITHM);
            generator.initialize(KEY_SIZE);
        }
        catch (NoSuchAlgorithmException ex){
            System.err.println("Algorithm " + KEYGEN_ALGORITHM +" not found!");
            //ex.printStackTrace();
            throw new RuntimeException("Invalid keygen algorithm " + KEYGEN_ALGORITHM);
        }
    }

    /**
     * Get the SimpleCrypto singleton instance
     * @return SimpleCrypto instance.
     */
    public static SimpleCrypto getInstance(){
        if (instance == null){
            instance = new SimpleCrypto();
        }
        return instance;
    }

    private InputStream objectToStream(Object obj){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] objBytes = null;
        try{
            out = new ObjectOutputStream(baos);
            out.writeObject(obj);
            out.flush();
            objBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected error during Object-to-Stream I/O");
        }
        finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Unexpected error while closing Object-to-Stream intermediate I/O");
            }
        }
        return objBytes != null ? new ByteArrayInputStream(objBytes) : null;
    }

    private Object byteArrayToObject(byte[] byteArray){
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        ObjectInput in = null;
        Object res = null;
        try{
            in = new ObjectInputStream(bais);
            res = in.readObject();
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error during Byte-to-Object I/O");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found during Byte-to-Object I/O");
        }
        finally {
            try{
                if (in != null){
                    in.close();
                }
            }
            catch (IOException ex){
                System.err.println("Unexpected error while closing Byte-to-Object intermediate I/O");
            }
        }
        return res;
    }

    /**
     * Generate a Public-Private keypair
     * @return New encryption keypair.
     */
    public KeyPair generateKeys(){
        return generator.generateKeyPair();
    }

    /**
     * Encrypts an object to a byte array
     * @param toEncrypt Object to be encrypted
     * @param key Public encryption key of the receiver
     * @return Encrypted object as a byte array
     */
    public byte[] encrypt(Object toEncrypt, PublicKey key){
        try {
            Cipher c = Cipher.getInstance(ENCRYPT_ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] buffer = new byte[ENCRYPT_BLOCKSIZE];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            InputStream input = objectToStream(toEncrypt);
            if (input != null) {
                int loopCheck = input.read(buffer);
                while (loopCheck > 0){
                    byte[] actualBuffer;
                    if (loopCheck != ENCRYPT_BLOCKSIZE){
                        actualBuffer = new byte[loopCheck];
                        System.arraycopy(buffer, 0, actualBuffer, 0, loopCheck);
                    }
                    else{
                        actualBuffer = buffer;
                    }
                    byte[] encBuff = c.doFinal(actualBuffer);
                    output.write(encBuff);
                    loopCheck = input.read(buffer);
                }
                output.close();
            }
            return output.toByteArray();
        }
        catch (NoSuchAlgorithmException ex){
            System.err.println("Algorithm " + ENCRYPT_ALGORITHM +" not found!");
            //ex.printStackTrace();
            throw new RuntimeException("Invalid encryption algorithm " + ENCRYPT_ALGORITHM);
        } catch (NoSuchPaddingException ex) {
            System.err.println("Padding of algorithm " + ENCRYPT_ALGORITHM + " not found!");
            //ex.printStackTrace();
            throw new RuntimeException("Invalid padding " + ENCRYPT_ALGORITHM);
        } catch (InvalidKeyException ex) {
            System.err.println("Invalid key!");
            //ex.printStackTrace();
            throw new RuntimeException("Invalid key for encryption");
        } catch (IOException ex) {
            System.err.println("Exception in encryption I/O");
            //ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        } catch (BadPaddingException ex) {
            System.err.println("Bad padding!");
            //ex.printStackTrace();
            throw new RuntimeException("Bad padding in encryption I/O");
        } catch (IllegalBlockSizeException ex) {
            System.err.println("Illegal blocksize!");
            //ex.printStackTrace();
            throw new RuntimeException("Illegal blocksize in encryption I/O");
        }
    }

    /**
     * Decrypts a byte array into an object
     * @param encrypted Encrypted object byte array
     * @param key Private encryption key of the receiver
     * @return Decrypted object
     */
    public Object decrypt(byte[] encrypted, PrivateKey key){
        try {
            Cipher c = Cipher.getInstance(ENCRYPT_ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] buffer = new byte[DECRYPT_BLOCKSIZE];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            InputStream input = new ByteArrayInputStream(encrypted);
            int loopCheck = input.read(buffer);
            while (loopCheck > 0){
                byte[] actualBuffer;
                if (loopCheck != DECRYPT_BLOCKSIZE){
                    actualBuffer = new byte[loopCheck];
                    System.arraycopy(buffer, 0, actualBuffer, 0, loopCheck);
                }
                else{
                    actualBuffer = buffer;
                }
                byte[] encBuff = c.doFinal(actualBuffer);
                output.write(encBuff);
                loopCheck = input.read(buffer);
            }
            output.close();
            return byteArrayToObject(output.toByteArray());
        }
        catch (NoSuchAlgorithmException ex){
            System.err.println("Algorithm " + ENCRYPT_ALGORITHM +" not found!");
            //ex.printStackTrace();
            throw new RuntimeException("Invalid encryption algorithm " + ENCRYPT_ALGORITHM);
        } catch (NoSuchPaddingException ex) {
            System.err.println("Padding of algorithm " + ENCRYPT_ALGORITHM + " not found!");
            //ex.printStackTrace();
            throw new RuntimeException("Invalid padding " + ENCRYPT_ALGORITHM);
        } catch (InvalidKeyException ex) {
            System.err.println("Invalid key!");
            //ex.printStackTrace();
            throw new RuntimeException("Invalid key for encryption");
        } catch (IOException ex) {
            System.err.println("Exception in encryption I/O");
            //ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        } catch (BadPaddingException ex) {
            System.err.println("Bad padding!");
            //ex.printStackTrace();
            throw new RuntimeException("Bad padding in encryption I/O");
        } catch (IllegalBlockSizeException ex) {
            System.err.println("Illegal blocksize!");
            //ex.printStackTrace();
            throw new RuntimeException("Illegal blocksize in encryption I/O");
        }
    }

}
