package content;

import java.awt.Toolkit;

import javax.swing.JFrame;

public class Window extends JFrame {
	
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Descargar Fotos de Instagram");
		setSize(800,700);
		setLocationRelativeTo(null);
        setIconImage(Toolkit.getDefaultToolkit().getImage("/img/favicon.ico"));
		
		add(new View());
		
		setVisible(true);
	}
	
}
