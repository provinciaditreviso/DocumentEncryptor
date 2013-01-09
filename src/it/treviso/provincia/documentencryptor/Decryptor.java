package it.treviso.provincia.documentencryptor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
 
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.RSAUtil;
import org.bouncycastle.openssl.PEMReader;
 
import sun.misc.BASE64Decoder;
 
public class Decryptor {
 
    public static void main(String[] args)
    {
 
        String privateKeyFilename = null;
        String encryptedFilename = null;
        String outputFilename = null;
 
        Decryptor rsaDecryptFile = new Decryptor();
 
        if (args.length < 3)
        {
            System.err.println("Usage: java "+ rsaDecryptFile.getClass().getName()+
            " Private_Key_Filename Encrypted_Filename Output_Filename");
            System.exit(1);
        }
 
        privateKeyFilename = args[0].trim();
        encryptedFilename = args[1].trim();
        outputFilename = args[2].trim();
        rsaDecryptFile.decrypt(privateKeyFilename, encryptedFilename, outputFilename);
 
    }
 
    private void decrypt (String privateKeyFilename, String encryptedFilename, String outputFilename){
 
        try {
 
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
 
            String value = "";
            AsymmetricKeyParameter privateKey;
    		
    		Reader reader = new FileReader(privateKeyFilename);
    		PEMReader pemReader = new PEMReader(reader, null);
    		//privateKey = PrivateKeyFactory.createKey(pemReader.readPemObject().getContent());
    		privateKey = PrivateKeyFactory.createKey(new PrivateKeyInfo(new
    				AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, new DERNull()),
    				RSAPrivateKey.getInstance(pemReader.readPemObject().getContent())));
    		
            AsymmetricBlockCipher e = new RSAEngine();
            e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
            e.init(false, privateKey);
 
            String inputdata = readFileAsString(encryptedFilename);
            byte[] messageBytes = hexStringToByteArray(inputdata);
 
            int i = 0;
            int len = e.getInputBlockSize();
            while (i < messageBytes.length)
            {
                if (i + len > messageBytes.length)
                    len = messageBytes.length - i;
 
                byte[] hexEncodedCipher = e.processBlock(messageBytes, i, len);
                value = value + new String(hexEncodedCipher);
                i += e.getInputBlockSize();
            }
 
 
            System.out.println(value);
 
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));
            out.write(value);
            out.close();
 
 
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
 
    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
 
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
 
    private static String readFileAsString(String filePath)
    throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        //System.out.println(fileData.toString());
        return fileData.toString();
    }
 
}