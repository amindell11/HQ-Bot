import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class Main {
	public static final boolean SAVE_DATA = true;
	private static final Main INSTANCE = new Main();

	public static Main getInstance() {
		return INSTANCE;
	}

	private String directory;
	private int fileIndex = 0;

	private Main() {
		if (SAVE_DATA) {
			directory = createSessionDirectory();
		}
	}

	public synchronized void main(App app) {
		Main main = this;
		app.addKeyListener(app.new Keystrokes(KeyEvent.VK_ENTER, () -> {
			synchronized (main) {
				fileIndex++;
				main.notifyAll();
				try {
					main.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}));
		new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}

			fileIndex++;
			synchronized (main) {
				main.notifyAll();
			}
		}).start();
		solveOnInput(app);
	}

	private synchronized void solveOnInput(App app) {
		while (true) {
			int oldFileIndex = fileIndex;
			while (oldFileIndex == fileIndex) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				String ans;
				ans = solveQuestion(app);
				showAns(app, ans);
				notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String solveQuestion(App app) throws Exception {
		long startTime = System.currentTimeMillis();

		Robot robot = new Robot();
		String fileName = getImageFile(fileIndex).getPath();
		Rectangle rectArea = app.bounds;
		BufferedImage screenFullImage = robot.createScreenCapture(rectArea);
		ImageIO.write(screenFullImage, "jpg", new File(fileName));
		Question q = ReadText.detectText(fileName, System.out);
		String ans = QuestionEval.getInstance().getAnswer(q);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("runtime: "+(double)totalTime/1000d);
		return ans;
	}

	private void showAns(App app, String ans) {
		final JOptionPane pane = new JOptionPane(ans, JOptionPane.INFORMATION_MESSAGE);
		final JDialog d = pane.createDialog("Result");
		d.setLocation(app.bounds.x - app.bounds.width / 2, (int) (app.bounds.y + app.bounds.getHeight()));
		d.setVisible(true);
	}

	private File getImageFile(int fileIndex) {
		try {
			if (SAVE_DATA) {
				File file = new File(directory + "/Question_" + fileIndex + ".jpg");
				if (!file.exists())
					file.createNewFile();
				return file;
			} else {
				return File.createTempFile("Question_" + fileIndex, ".jpg");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String createSessionDirectory() {
		File file = new File("HQ Sessions/" + new SimpleDateFormat("M-d ha").format(new Date()));
		file.mkdirs();
		return file.getPath();
	}
}