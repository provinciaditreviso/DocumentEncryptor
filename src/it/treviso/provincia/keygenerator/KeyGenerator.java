package it.treviso.provincia.keygenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

/**
 * A simple utility class that generates a RSA PGPPublicKey/PGPSecretKey pair.
 * <p>
 * usage: RSAKeyPairGenerator [-a] identity passPhrase
 * <p>
 * Where identity is the name to be associated with the public key. The keys are placed 
 * in the files pub.[asc|bpg] and secret.[asc|bpg].
 */
public class KeyGenerator
{
    private static void exportKeyPair(
        OutputStream    secretOut,
        OutputStream    publicOut,
        PublicKey       publicKey,
        PrivateKey      privateKey,
        String          identity,
        char[]          passPhrase)
        throws IOException, InvalidKeyException, NoSuchProviderException, SignatureException, PGPException
    {    
            secretOut = new ArmoredOutputStream(secretOut);

        //PGPSecretKey    secretKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, PGPPublicKey.RSA_GENERAL, publicKey, privateKey, new Date(), identity, PGPEncryptedData.AES_256, passPhrase, null, null, new SecureRandom(), "BC");
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        PGPKeyPair          keyPair = new PGPKeyPair(PGPPublicKey.RSA_GENERAL, publicKey, privateKey, new Date());
        PGPSecretKey        secretKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, keyPair, identity, sha1Calc, null, null, new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1Calc).setProvider("BC").build(passPhrase));
        
        secretKey.encode(secretOut);
        
        secretOut.close();
        publicOut = new ArmoredOutputStream(publicOut);

        PGPPublicKey    key = secretKey.getPublicKey();
        
        key.encode(publicOut);
        
        publicOut.close();
    }


	private String path;
	private char[] passphrase;
    
    public KeyGenerator(String path, char[] passphrase) {
    	this.path = path;
    	this.passphrase = passphrase;
    	buildKey();
    }
    
    
    public int buildKey()
    {
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA", "BC");
		} catch (NoSuchAlgorithmException e) {
			return 1;
		} catch (NoSuchProviderException e) {
			return 2;
		}
        
        kpg.initialize(4096);
        
        KeyPair kp = kpg.generateKeyPair();
        FileOutputStream out1 = null;
        FileOutputStream out2 = null;
		try {
			out1 = new FileOutputStream(this.path+"/privata.asc");
			out2 = new FileOutputStream(this.path+"/pubblica.asc");
		} catch (FileNotFoundException e) {
			return 3;
			
		}
		
		try {
			exportKeyPair(out1, out2, kp.getPublic(), kp.getPrivate(), "protocollo.provincia.treviso@pecveneto.it", this.passphrase);
		} catch (InvalidKeyException e) {
			return 4;
		} catch (NoSuchProviderException e) {
			return 2;
		} catch (SignatureException e) {
			return 5;
		} catch (IOException e) {
			return 6;
		} catch (PGPException e) {
			e.printStackTrace();
			return 7;
		}
		
		return 0;
    }
}