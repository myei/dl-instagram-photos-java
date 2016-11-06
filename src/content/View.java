package content;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class View extends JPanel {
	private JTextField urlRequest = new JTextField(30);
	private JLabel imgContainer = new JLabel();
	private JLabel errorMsg = new JLabel();
	private JButton buscar = new JButton();
	private String urlImg;
	private Image imgResult; 
	
	public View() {
		setLayout(null);
		setBackground(Color.WHITE);
		
		setHeader();
		setBody();
	}
	
	private void setHeader() {
		JLabel logo = new JLabel();
		logo.setBounds(400 - 62, 20, 124, 51);
		
		try {
			Image logo_img = ImageIO.read(getClass().getResource("/img/logo.png")).getScaledInstance(124, 51, Image.SCALE_SMOOTH);
			logo.setIcon(new ImageIcon(logo_img));
		} catch (IOException e) { JOptionPane.showMessageDialog(this, "Error con el logotipo"); }
		
		add(logo);
	}
	
	private void setBody() {
		/*
		 * El listener es la clase interna ubicada mas adelante en la clase
		 */
		buscar.addActionListener(new ImageListener());
		
		/*
		 * Listener al hacer click en la imagen para descargarla
		 */
		imgContainer.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				/*
				 * Hilo de descarga de la imagen al equipo
				 */
				Thread hiloDlImage = new Thread(new Runnable() {
					public void run() {
						try {
							JFileChooser fc = new JFileChooser();
							fc.showSaveDialog(View.this);
							
							if (fc.getSelectedFile() != null) {
								showError("Descargando la imagen...");
								BufferedImage img = ImageIO.read(new URL(urlImg));
								ImageIO.write(img, "png", fc.getSelectedFile());
								
								JOptionPane.showMessageDialog(View.this, "Imagen descargada exitosamente");
								showError("");
							}
							
						} catch (IOException e1) { JOptionPane.showMessageDialog(View.this, "Fallo al descargar la imagen"); }						
					}
				});
				
				hiloDlImage.start();
			}
			
		});
		
		/*
		 * Listener al pulsar enter
		 */
		urlRequest.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					ImageListener l = new ImageListener();
					l.actionPerformed(new ActionEvent(urlRequest, 0, "" + e.getKeyChar()));
				}
			}
		});
		
		try {
			/*
			 * Añadiendo el icono para el boton de buscar la imagen
			 */
			Image search = ImageIO.read(getClass().getResource("/img/look.png"));
			buscar.setIcon(new ImageIcon(search.getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
		} catch (IOException e) { showError("Error con la imagen de buscar"); }
		
		urlRequest.setBounds(100, 75, 560, 30);
		buscar.setBounds(659, 75, 40, 30);
		imgContainer.setBounds(100, 125, 600, 600);
		errorMsg.setBounds(100, 125, 200, 30);
		errorMsg.setVisible(false);
		imgContainer.setVisible(false);
		
		add(urlRequest);
		add(buscar);
		add(imgContainer);
		add(errorMsg);
	}
	
	private void showError(String e) {
		imgContainer.setVisible(false);
		errorMsg.setText(e);
		errorMsg.setVisible(true);
	}
	
	private void showImage(Image i) {
		errorMsg.setVisible(false);
		imgContainer.setIcon(new ImageIcon(i));
		imgContainer.setVisible(true);
	}
	
	/*******************************************************************************************
	 * 									L I S T E N E R
	 *******************************************************************************************/
	private class ImageListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			showError("Buscando la imagen...");
			/*
			 * Hilo de verificacion de conexion a internet
			 * verificacion de contenido de url solicitada
			 */
			Thread hiloShowImage = new Thread(new Runnable() {
				public void run() {
					if (testConnection()) {
						try {
							String[] data = getContenidoHTML(urlRequest.getText());
							setImage(data[175]);
							
							System.out.println(data[161].substring(data[161].indexOf('@')));
						} catch (IOException E) {
							showError("Introduzca una url correcta");
						}
					} else {
						showError("Necesita conexion a internet...");
					}
				}
			});
			
			hiloShowImage.start();
		}
		
		/*
		 * Asignacion de imagen obtenida de la verificacion de url
		 * La guardo para ser usada al momento de descargar la imagen
		 */
		private void setImage(String img) {
			try {
				urlImg = img;
				// Redimensiono la imagen que recibo por parametro
				imgResult = ImageIO.read(new URL(img)).getScaledInstance(600, 600, Image.SCALE_SMOOTH);
				showImage(imgResult);
			} catch (IOException E) { showError("Ha ocurrido un error tratanto la imagen..."); }
		}
		
		/*
		 * Verificacion de url y obtencion de url de la imagen solicitada
		 * y el nombre de usuario dueño de la foto
		 * el contenido de la pagina se separa por comillas dobles (")
		 * y eliminando los espacios en blanco para reducir el uso de memoria
		 */
		private String[] getContenidoHTML(String urlReq) throws IOException {
			String line;
		    String content = "";
			
		    URL url = new URL(urlReq);
		    URLConnection uc = url.openConnection();
		    uc.connect();
		    
		    //Creamos el objeto con el que vamos a leer
		    BufferedReader buffer = new BufferedReader(new InputStreamReader(uc.getInputStream()));

		    while ((line = buffer.readLine()) != null)
		    	content += line;
		   
		    buffer.close();
		    return content.trim().split("\"");
		}
		
		/*
		 * Metodo para probar la conexion a internet de la aplicacion
		 * destinada a ser ejecutada antes de buscar la imagen
		 * y cuando sea necesario
		 */
		private boolean testConnection() {
			try {
				Socket s = new Socket("www.google.co.ve", 80);
				boolean c = s.isConnected(); 
				s.close();
				
				return c;
			} catch (Exception e) {
				showError("Necesita conexion a internet...");
				return false;
			}

		}
		
	}

} 