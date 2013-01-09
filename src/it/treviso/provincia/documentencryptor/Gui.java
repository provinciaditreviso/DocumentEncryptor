package it.treviso.provincia.documentencryptor;

import java.awt.EventQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;

import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.openssl.PEMReader;

import sun.misc.BASE64Decoder;

public class Gui {

	private JFrame frame;
	private JTextField textFile;
	private JTextField textKey;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		final JPanel panel = new JPanel();
		panel.setBounds(12, 26, 428, 263);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblFileDaCifrare = new JLabel("File da cifrare");
		lblFileDaCifrare.setBounds(0, 32, 97, 15);
		panel.add(lblFileDaCifrare);
		
		textFile = new JTextField();
		textFile.setEditable(false);
		textFile.setBounds(127, 30, 227, 25);
		panel.add(textFile);
		textFile.setColumns(10);
		
		JLabel lblPercorsoChiave = new JLabel("Percorso chiave");
		lblPercorsoChiave.setBounds(0, 110, 112, 15);
		panel.add(lblPercorsoChiave);
		
		textKey = new JTextField();
		textKey.setEditable(false);
		textKey.setColumns(10);
		textKey.setBounds(127, 108, 227, 25);
		panel.add(textKey);
		
		JButton chooseFile = new JButton("");
		chooseFile.setToolTipText("Seleziona file da firmare");
		chooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			      JFileChooser chooser = new JFileChooser();
			      chooser.setCurrentDirectory(new File("."));
			      int r = chooser.showOpenDialog(panel);
			      if (r == JFileChooser.APPROVE_OPTION) {
			        String filename = chooser.getSelectedFile().getPath();
			        textFile.setText(filename);
			      }
			    }
			}
		);
		chooseFile.setIcon(new ImageIcon(Gui.class.getResource("/com/sun/java/swing/plaf/windows/icons/NewFolder.gif")));
		chooseFile.setBounds(374, 30, 42, 25);
		panel.add(chooseFile);
		
		JButton chooseKey = new JButton("");
		chooseKey.setToolTipText("Seleziona file pem contenente la chiave di cifratura");
		chooseKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
			      chooser.setCurrentDirectory(new File("."));
			      chooser.setFileFilter(new FileFilter() {
			        public boolean accept(File f) {
			          return f.getName().toLowerCase().endsWith(".pem")
			              || f.isDirectory();
			        }

			        public String getDescription() {
			          return "Key Files (*.pem)";
			        }
			      });
			      int r = chooser.showOpenDialog(panel);
			      if (r == JFileChooser.APPROVE_OPTION) {
			        String keyname = chooser.getSelectedFile().getPath();
			        textKey.setText(keyname);
			      }
			}
		});
		chooseKey.setIcon(new ImageIcon(Gui.class.getResource("/com/sun/java/swing/plaf/windows/icons/NewFolder.gif")));
		chooseKey.setBounds(374, 108, 42, 25);
		panel.add(chooseKey);
		
		JButton btnCifraFile = new JButton("Cifra File");
		btnCifraFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result;
				result = encryptFile(textFile.getText(),textKey.getText());
				switch(result) {
				case 0:	JOptionPane.showMessageDialog(frame, "File cifrato correttamente in "+textFile.getText()+".enc"); break;
				case 1: JOptionPane.showMessageDialog(frame, "File da cifrare non specificato","Specificare file", JOptionPane.ERROR_MESSAGE); break;
				case 2: JOptionPane.showMessageDialog(frame, "Chiave di cifratura non specificata","Specificare chiave", JOptionPane.ERROR_MESSAGE); break;
				default: JOptionPane.showMessageDialog(frame, "Errore inatteso", "Errore", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnCifraFile.setBounds(154, 192, 117, 25);
		panel.add(btnCifraFile);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnAiuto = new JMenu("Aiuto");
		menuBar.add(mnAiuto);
		
		JMenuItem mntmInformazioni = new JMenuItem("Informazioni");
		mntmInformazioni.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String versione = "0";
				URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();
				try {
				  URL url = cl.findResource("META-INF/MANIFEST.MF");
				  Manifest manifest = new Manifest(url.openStream());
				  Attributes attr = manifest.getMainAttributes();
				  versione = attr.getValue("Versione");
				} catch (IOException E) {
				  E.printStackTrace();
				}
				JOptionPane.showMessageDialog(frame, "DocumentEncryptor versione "+versione+"\nProvincia di Treviso, Settore Sistemi Informatici");
			}
		});
		mnAiuto.add(mntmInformazioni);
	}

	protected int encryptFile(String file, String key) {
		if (file == "" ) return 1;
		if (key == "") return 2;
		
		File source = new File(file);
		String fileContent;
		try {
			fileContent = getBytesFromFile(source);
		} catch (IOException e1) {
			return 5;
		}
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String value = "";
       /* String fkey;
		try {
			fkey = getBytesFromFile(new File(key));
		} catch (IOException e3) {
			return 5;
		}
        BASE64Decoder b64 = new BASE64Decoder();*/
        AsymmetricKeyParameter publicKey;
		try {
			Reader reader = new FileReader(key);
			PEMReader pemReader = new PEMReader(reader, null);
			publicKey = PublicKeyFactory.createKey(pemReader.readPemObject().getContent());
		} catch (IOException e2) {
			return 3;
		}
        AsymmetricBlockCipher e = new RSAEngine();
        e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
        e.init(true, publicKey);
        
        byte[] messageBytes = fileContent.getBytes();
        int i = 0;
        int len = e.getInputBlockSize();
        while (i < messageBytes.length)
        {
            if (i + len > messageBytes.length)
                len = messageBytes.length - i;

            byte[] hexEncodedCipher;
			try {
				hexEncodedCipher = e.processBlock(messageBytes, i, len);
				value = value + getHexString(hexEncodedCipher);
			} catch (InvalidCipherTextException e1) {
				System.out.println("Error: InvalidCipherTextException");
				return 4;
			}
			catch (Exception e1) {
				System.out.println("Error: getHexString");
				return 4;
			}
            i += e.getInputBlockSize();
        }

        //System.out.println(value);
        BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(file+".enc"));
			out.write(value);
	        out.close();
		} catch (IOException e1) {
			return 6;
		}
		return 0;
	}
	
	public static String getBytesFromFile(File file) throws IOException {
	        StringBuffer fileData = new StringBuffer(1000);
	        BufferedReader reader = new BufferedReader(
	                new FileReader(file));
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1){
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	            buf = new char[1024];
	        }
	        reader.close();
	        return fileData.toString();
	}
	
	public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
}
