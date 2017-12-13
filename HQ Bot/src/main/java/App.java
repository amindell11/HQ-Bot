import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This program demonstrates how to capture screenshot of a portion of screen.
 * 
 */
public class App extends JFrame {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		App p = new App();
		p.loadBounding();
		p.setUndecorated(true);
		p.drawBoundScene();
		if (p.bounds == null || JOptionPane.showOptionDialog(p, "adjust bounds?", "Bounds", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.YES_OPTION) {
			p.setBounds();
		}
		p.setBackground(new Color(0, 0, 255, 0));
		p.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		p.setVisible(true);
		Main.getInstance().main(p);
	}

	Rectangle bounds;

	public void loadBounding() {
		File f = new File("bounds.cfg");
		if (f.exists())
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String s = br.readLine();
				br.close();
				String[] vals = s.split(",");
				int[] nums = (int[]) Arrays.stream(vals).mapToInt(Integer::parseInt).toArray();
				bounds = new Rectangle(nums[0], nums[1], nums[2], nums[3]);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void drawBoundScene() {
		setVisible(false);
		JComponent paintBox = new JComponent() {
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.GRAY);
				g2.setStroke(new BasicStroke(3));
				if (bounds != null) {
					g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			}
		};
		paintBox.setOpaque(true);
		getContentPane().add(paintBox);
		getContentPane().setBackground(new Color(0, 0, 255, 0));
		setBackground(new Color(0, 0, 255, 0));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}

	public void setBounds() {
		bounds = null;
		drawBoundScene();
		setBackground(new Color(.85f, .85f, .85f, .5f));
		List<Point> vertices = new ArrayList<>();
		Point p1, p2;
		addKeyListener(new Keystrokes(KeyEvent.VK_SPACE, () -> vertices.add(MouseInfo.getPointerInfo().getLocation())));
		while (vertices.size() == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		p1 = vertices.get(0);
		while (vertices.size() == 1) {
			p2 = MouseInfo.getPointerInfo().getLocation();
			bounds = new Line2D.Double(p1, p2).getBounds();
			repaint();
		}
		p2 = vertices.get(1);
		bounds = new Line2D.Double(p1, p2).getBounds();
		try {
			File f = new File("bounds.cfg");
			if (!f.exists())
				f.createNewFile();
			PrintWriter pw = new PrintWriter(f);
			pw.print(bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class Keystrokes implements KeyListener {
		int keyToWatch;
		Runnable onPress;

		public Keystrokes(int keyToWatch, Runnable onPress) {
			this.keyToWatch = keyToWatch;
			this.onPress = onPress;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == keyToWatch) {
				onPress.run();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}

	}

}