package it.treviso.provincia.keygenerator;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Gui extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3246052309602286748L;
	private JPanel contentPane;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui frame = new Gui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Gui() {
		setTitle("Genera Chiavi OpenPGP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 451, 230);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JButton button = new JButton("");
		button.setToolTipText("Seleziona Directory dove generare le chiavi");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        
			    JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Seleziona Directory dove salvare le chiavi generate");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			         textField.setText(chooser.getSelectedFile().toString());
			      }
			    else {
			      System.out.println("No Selection ");
			      }
			}
		});
		button.setIcon(new ImageIcon(Gui.class.getResource("/com/sun/java/swing/plaf/windows/icons/Directory.gif")));
		button.setBounds(368, 55, 50, 25);
		panel.add(button);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(155, 55, 201, 25);
		panel.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Seleziona Directory");
		lblNewLabel.setBounds(12, 60, 147, 15);
		panel.add(lblNewLabel);
		
		JButton btnGeneraChiavi = new JButton("Genera Chiavi");
		btnGeneraChiavi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = new File(textField.getText());
				if (!f.exists()) {
					JOptionPane.showMessageDialog(null, "La directory" + textField.getText()+" non esiste", "Directory non valida",JOptionPane.ERROR_MESSAGE);
					return;
				}
				f = new File(textField.getText()+"/privata.asc");
				if (f.exists()) {
					JOptionPane.showMessageDialog(null, "Nella directory" + textField.getText()+" esiste già una chiave privata.\nSelezionare un altra directory o spostare le chiavi attualmente esistenti", "Coppia di chiavi già esistente",JOptionPane.ERROR_MESSAGE);
					return;
				}
				JPasswordField pwd = new JPasswordField(10); 
				int ch = JOptionPane.showConfirmDialog(null, pwd,"Inserire password",JOptionPane.OK_CANCEL_OPTION);
				JPasswordField pwd1 = new JPasswordField(10); 
				int ch1 = JOptionPane.showConfirmDialog(null, pwd1,"Confermare password",JOptionPane.OK_CANCEL_OPTION);
				if (ch < 0 || ch == 2 || ch1 < 0 || ch1 == 2)  JOptionPane.showMessageDialog(null, "Generazione Chiave annullata", "Generazione annullata", JOptionPane.WARNING_MESSAGE);
				else {
					String p1 = new String(pwd.getPassword());
					String p2 = new String(pwd1.getPassword());
					if (!(p1.equals(p2))) {
						JOptionPane.showMessageDialog(null, "Password non coincidente", "Generazione annullata", JOptionPane.ERROR_MESSAGE); 
						return;
					}
					KeyGenerator kg = new KeyGenerator(textField.getText(),pwd.getPassword());
					JOptionPane.showMessageDialog(null, "La generazione della chiavi è in corso, attendere", "Generazione chiavi in corso", JOptionPane.INFORMATION_MESSAGE);
					
					switch (kg.buildKey()) {
					case 1: JOptionPane.showMessageDialog(null, "Errori nella generazione della chiave", "NoSuchAlgorithmException", JOptionPane.ERROR_MESSAGE);break;
					case 2: JOptionPane.showMessageDialog(null, "Errore nel caricare le librerie BC", "NoSuchProviderException", JOptionPane.ERROR_MESSAGE);break;
					case 3: JOptionPane.showMessageDialog(null, "Errore nell'aprire i file di output", "FileNotFoundException", JOptionPane.ERROR_MESSAGE);break;
					case 4: JOptionPane.showMessageDialog(null, "Chiave generata non valida", "InvalidKeyException", JOptionPane.ERROR_MESSAGE);break;
					case 5: JOptionPane.showMessageDialog(null, "Problemi nella firma della chiave", "SignatureException", JOptionPane.ERROR_MESSAGE);break;
					case 6: JOptionPane.showMessageDialog(null, "Errore di I/O nei file", "IOException", JOptionPane.ERROR_MESSAGE);break;
					case 7: JOptionPane.showMessageDialog(null, "Errori nell'algoritmo PGP", "PGPException", JOptionPane.ERROR_MESSAGE);break;
					default: JOptionPane.showMessageDialog(null, "La generazione della chiavi è avvenuta con successo", "Chiavi generate", JOptionPane.INFORMATION_MESSAGE);

					}
				}
			}
		});
		btnGeneraChiavi.setToolTipText("Genera Chiavi nella Cartella Specificata");
		btnGeneraChiavi.setBounds(155, 115, 176, 25);
		panel.add(btnGeneraChiavi);
		
		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
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
				JOptionPane.showMessageDialog(null, "DocumentEncryptor versione "+versione+"\nProvincia di Treviso, Settore Sistemi Informatici");
			}
		});
		mnAiuto.add(mntmInformazioni);
	}
}