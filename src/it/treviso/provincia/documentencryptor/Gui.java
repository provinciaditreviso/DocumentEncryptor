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
import java.io.FileOutputStream;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
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
		chooseKey.setToolTipText("Seleziona file key contenente la chiave pubblica");
		chooseKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
			      chooser.setCurrentDirectory(new File("."));
			      chooser.setFileFilter(new FileFilter() {
			        public boolean accept(File f) {
			          return f.getName().toLowerCase().endsWith(".key")
			              || f.isDirectory();
			        }

			        public String getDescription() {
			          return "Key Files (*.key)";
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
				case 3: JOptionPane.showMessageDialog(frame, "Chiave di cifratura non valida","Chiave Invalida", JOptionPane.ERROR_MESSAGE); break;
				case 4: JOptionPane.showMessageDialog(frame, "Problemi in fase di cifratura","Errore", JOptionPane.ERROR_MESSAGE); break;
				case 5: JOptionPane.showMessageDialog(frame, "Errore di I/O sul file, controllare di avere i permessi necessari sul file","Errore I/O", JOptionPane.ERROR_MESSAGE); break;
				case 6: JOptionPane.showMessageDialog(frame, "Errore nel salvare il file cifrato, controllare di avere i permessi di scrittura sul file","Specificare chiave", JOptionPane.ERROR_MESSAGE); break;
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
			if(file=="") return 1;
			if(key=="") return 2;
	        Security.addProvider(new BouncyCastleProvider());
	        PGPProcessor p = new PGPProcessor();

	        byte[] original;
			try {
				original = p.getBytesFromFile(new File(file));
			} catch (IOException e) {
				return 5;
			}

	        FileInputStream pubKey;
			try {
				pubKey = new FileInputStream(key);
			} catch (FileNotFoundException e) {
				return 3;
			}
	        byte[] encrypted;
			try {
				encrypted = p.encrypt(original, p.readPublicKey(pubKey), null,
				        true, true);
			} catch (NoSuchProviderException e) {
				System.out.println("Error: NoSuchProviderException");
				return 4;
			} catch (IOException e) {
				System.out.println("Error: IOException");
				return 4;
			} catch (PGPException e) {
				System.out.println("Error: PGPException");
				return 4;
			}

	        FileOutputStream dfis;
			try {
				dfis = new FileOutputStream(file+".enc");
				dfis.write(encrypted);
				dfis.close();
			} catch (FileNotFoundException e) {
				System.out.println("Error: FileNotFoundException");
				return 6;
			}
	        catch (IOException e) {
	        	System.out.println("Error: IOException");
				return 6;
			}
				


		return 0;
	}
	

}
