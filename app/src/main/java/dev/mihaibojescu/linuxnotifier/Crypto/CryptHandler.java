package dev.mihaibojescu.linuxnotifier.Crypto;

import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by michael on 08.07.2017.
 *
 * CryptHandler singleton. Used for creating pins and public/private keys.
 */

public class CryptHandler
{

    private static CryptHandler instance = null;
    private static String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPQRSTUVWXYZ0123456789";
    private KeyPairGenerator kpg;
    private KeyPair keys;


    private CryptHandler()
    {
    }

    public static CryptHandler getInstance()
    {
        if (instance == null)
            instance = new CryptHandler();

        return instance;
    }

    public void createKeyPairGenerator(int size)
    {
        if (kpg == null)
            try
            {
                kpg = KeyPairGenerator.getInstance("RSA");
            }
            catch (NoSuchAlgorithmException e)
            {
                Log.e("Error", "Algorithm 'RSA' cannot be used!");
            }
    }

    public void createNewKeys(int size)
    {
        if (kpg == null)
            createKeyPairGenerator(size);

        keys = kpg.genKeyPair();
    }

    public PublicKey getPublicKey() throws Exception
    {
        if (keys == null)
            throw new Exception("Keys not available, need to be created first.");

        return keys.getPublic();
    }

    public byte[] getPrivateKey() throws Exception
    {
        if (keys == null)
            throw new Exception("Keys not available, need to be created first");

        return keys.getPrivate().getEncoded().toString().getBytes();
    }

    public String createPin(int size)
    {
        String newPin = "";
        SecureRandom randomService = new SecureRandom();
        for(int i = 0; i < size; i++)
            newPin += alphabet.charAt(randomService.nextInt(alphabet.length()));

        return newPin;
    }
}
