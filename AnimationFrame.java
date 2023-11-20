import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * <p>AnimationFrameはアニメーション描画機能を実装したウィンドウフレームです。
 * 内部ではイメージバッファを使用しており，繰り返し文を使った連続描画によるアニメーションプログラム作成を想定したコードになっています。</p>
 * <p>AnimationFrameを使って作成したウィンドウフレームには，再生ボタン[>]とコマ送りボタン[|>]が上部に表示されます。
 * 再生ボタンを押すとプログラム処理を連続実行します。続けて一時停止ボタン[||]をクリックすると直後のprintFrame()メソッド呼び出し部分でプログラムを停止します。
 * また，コマ送りボタンは次のprintFrame()メソッド呼び出し部分までプログラム処理を実行します。</p>
 * <p>AnimationFrameを使って作成したウィンドウフレームは，描画崩れを防ぐためにアプリケーションユーザーによるウィンドウサイズの変更はできないようになっています。
 * プログラマーがsetSize(int width, int height)メソッドを記述して描画領域のサイズを設定し、それに対応したサイズのウィンドウを作成することは可能です。</p>
 * @author 比嘉 築 @ E Cube Factory
 * @version 1.2
 */

public class AnimationFrame extends JFrame {

	private BufferedImage offscreen = null;
	private Graphics2D osg = null;
	private Color color = Color.BLACK;
	private HashMap<String, BufferedImage> imageMap = new HashMap<String, BufferedImage>();
	private boolean paintFrameFlg = false;
	private boolean playFlg = false;
	private boolean frameAdvanceFlg = false;
	private GraphPanel panel = new GraphPanel();
	private PlayButton playBtn = new PlayButton();
	private FrameAdvanceButton frameAdvanceBtn = new FrameAdvanceButton();

	/**
	 * アニメーション用のコントロールボタンと，幅500ピクセル，高さ500ピクセルの描画領域を持つウィンドウフレームを生成します。
	 * フレームの可視化も自動で行います。
	 */
	public AnimationFrame() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				/* バッファ領域の作成 */
				if (osg == null && panel.getWidth() > 0 && panel.getHeight() > 0) {
					offscreen = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
					osg = (Graphics2D) offscreen.getGraphics();
					osg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					osg.setPaint(Color.WHITE);
					osg.fill(new Rectangle2D.Double(0, 0, panel.getWidth(), panel.getHeight()));
					osg.setPaint(color);
				}
			}
		});
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.add(panel, BorderLayout.CENTER);
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		contentPane.add(toolbar, BorderLayout.NORTH);
		toolbar.add(playBtn);
		toolbar.add(frameAdvanceBtn);
		setSize(500, 500);
		setVisible(true);
		setTitle("AnimationFrame");

		sleep(500);
	}

	/**
	 * 描画用のペンの色を設定します。描画系メソッドを呼び出す前にペンの色を設定しておくと，その色で描画が行われます。
	 * @param c Colorクラスのオブジェクト
	 */
	public void setColor(Color c) {
		color = c;
		if (osg != null)
			osg.setPaint(color);
	}

	/**
	 * 始点(x1,y1)から終点(x2,y2)まで直線を引きます。
	 * @param x1 始点のx座標
	 * @param y1 始点のy座標
	 * @param x2 終点のx座標
	 * @param y2 終点のy座標
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {
		if (paintFrameFlg) {
			clearOffscreen();
			paintFrameFlg = false;
		}
		if (osg != null) {
			osg.draw(new Line2D.Double(x1, y1, x2, y2));
		}
	}

	/**
	 * 指定された塗りつぶされた長方形を描きます。
	 * @param x 左上の頂点のx座標
	 * @param y 左上の頂点のy座標
	 * @param width 長方形の幅
	 * @param height 長方形の高さ
	 */
	public void drawRect(int x, int y, int width, int height) {
		if (paintFrameFlg) {
			clearOffscreen();
			paintFrameFlg = false;
		}
		if (osg != null) {
			drawRect(x, y, width, height, true);
		}
	}

	/**
	 * 指定された長方形を描きます。
	 * @param x 左上の頂点のx座標
	 * @param y 左上の頂点のy座標
	 * @param width 長方形の幅
	 * @param height 長方形の高さ
	 * @param fill 長方形を塗りつぶすならtrue，枠だけならfalse
	 */
	public void drawRect(int x, int y, int width, int height, boolean fill) {
		if (paintFrameFlg) {
			clearOffscreen();
			paintFrameFlg = false;
		}
		if (osg != null) {
			if (fill) {
				osg.fill(new Rectangle2D.Double(x, y, width, height));
			} else {
				osg.draw(new Rectangle2D.Double(x, y, width, height));
			}
		}
	}

	/**
	 * 指定座標を中心とする塗りつぶされた円を描画します。
	 * @param x 中心点のx座標
	 * @param y 中心点のy座標
	 * @param r 円の直径
	 */
	public void drawCircle(int x, int y, int r) {
		if (paintFrameFlg) {
			clearOffscreen();
			paintFrameFlg = false;
		}
		if (osg != null) {
			drawCircle(x, y, r, true);
		}
	}

	/**
	 * 指定座標を中心とする円を描画します。
	 * @param x 中心点のx座標
	 * @param y 中心点のy座標
	 * @param r 円の直径
	 * @param fill 円を塗りつぶすならtrue，枠だけならfalse
	 */
	public void drawCircle(int x, int y, int r, boolean fill) {
		if (paintFrameFlg) {
			clearOffscreen();
			paintFrameFlg = false;
		}
		if (osg != null) {
			if (fill) {
				osg.fill(new Ellipse2D.Double(x - r / 2, y - r / 2, r, r));
			} else {
				osg.draw(new Ellipse2D.Double(x - r / 2, y - r / 2, r, r));
			}
		}
	}

	/**
	 * 指定座標を中心とする塗りつぶされた正方形を描画します。
	 * @param x 中心点のx座標
	 * @param y 中心点のy座標
	 * @param s 正方形の1辺の長さ
	 */
	public void drawSquare(int x, int y, int s) {
		drawRect(x - s / 2, y - s / 2, s, s, true);
	}

	/**
	 * 指定座標を開始点とした文字列を描画します。開始点はベースライン(文字の下側)が基準となっています。
	 * @param str 表示する文字列
	 * @param x 描画開始点のx座標
	 * @param y 描画開始点のy座標
	 * @param s 文字のサイズ
	 */
	public void drawString(String str, int x, int y, float s) {
		if (paintFrameFlg) {
			clearOffscreen();
			paintFrameFlg = false;
		}
		if (osg != null) {
			osg.setFont(getFont().deriveFont(s));
			osg.drawString(str, x, y);
		}
	}

	/**
	 * 指定座標を中心とする画像を描画します。このメソッドを呼び出す前にsetImage()メソッドで画像を登録しておく必要があります。
	 * @param name 登録済みの画像ファイル名(png, jpg, gif形式)
	 * @param x 中心点のx座標
	 * @param y 中心点のy座標
	 * @param m 倍率(%)
	 */
	public void drawImage(String name, int x, int y, int m) {
		if (paintFrameFlg) {
			clearOffscreen();
			paintFrameFlg = false;
		}
		if (imageMap.containsKey(name)) {
			if (osg != null) {
				BufferedImage img = imageMap.get(name);
				int imgW = (int) (img.getWidth(this) * m / 100.0);
				int imgH = (int) (img.getHeight(this) * m / 100.0);
				osg.drawImage(img, x - imgW / 2, y - imgH / 2, imgW, imgH, this);
			}
		}
	}

	/**
	 * 表示する画像を登録します。画像ファイルはクラスファイルと同じフォルダに入れておいておきます。
	 * @param name 画像ファイル名(png, jpg, gif形式)
	 */
	public void setImage(String name) {
		try {
			BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(name));
			imageMap.put(name, img);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * プログラムを指定時間だけ停止させます。アニメーション表示をゆっくりにするために描画と描画の間に呼び出します。
	 * パラメータのtimeはミリ秒単位で指定しますが，実時間を保証するものではありません。時計プログラムのように，正確な時間計測が必要な場合は別の方法を使うようにしてください。
	 * @param time 停止時間(ミリ秒)
	 */
	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * 描画領域のサイズを幅widthピクセル，高さheightピクセルに変更します。
	 * @param width 描画領域の幅
	 * @param height 描画領域の高さ
	 */
	public void setSize(int width, int height) {
		int w = panel.getWidth();
		int h = panel.getHeight();
		if (w == width && h == height) {
			// 現在の設定値と同じなら何もしない
			return;
		}
		osg = null;
		panel.setPreferredSize(new Dimension(width, height));
		pack();
		while (osg == null) {
			sleep(10);
		}
	}

	/**
	 * それまで記述した描画のプログラムをウィンドウフレームに反映させます。
	 * 同時に，再生状態以外のときにはこのメソッドの呼び出しでプログラムを停止させ，ソフト利用者の「再生ボタンクリック」または「コマ送りボタンクリック」操作を待ちます。
	 */
	public void paintFrame() {
		panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
		paintFrameFlg = true;
		/* 再生操作待ち */
		while (true) {
			if (playFlg)
				break;
			sleep(100);
		}
		/* コマ送りの処理 */
		if (frameAdvanceFlg) {
			playFlg = false;
			frameAdvanceFlg = false;
		}

		sleep(80);
	}

	/**
	 * バッファをクリアするprivateメソッドです。
	 */
	private void clearOffscreen() {
		if (osg != null) {
			osg.setPaint(Color.WHITE);
			osg.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
			osg.setPaint(color);
		}
	}

	private class GraphPanel extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			/* バッファの描画 */
			g.drawImage(offscreen, 0, 0, this);
		}
	}

	private class PlayButton extends JButton implements ActionListener {

		private ImageIcon playIcon;
		private ImageIcon pauseIcon;

		PlayButton() {
			BufferedImage playImg = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) playImg.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setPaint(Color.BLUE);
			int xpoints[] = { 2, 18, 2 };
			int ypoints[] = { 2, 10, 18 };
			g.fill(new Polygon(xpoints, ypoints, xpoints.length));
			playIcon = new ImageIcon(playImg);

			BufferedImage pauseImg = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) pauseImg.getGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setPaint(Color.BLUE);
			g2.fill(new Rectangle2D.Double(3, 2, 6, 16));
			g2.fill(new Rectangle2D.Double(11, 2, 6, 16));
			pauseIcon = new ImageIcon(pauseImg);

			setIcon(playIcon);

			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			playFlg = !playFlg;
			if (getIcon() == pauseIcon) {
				setIcon(playIcon);
				frameAdvanceBtn.setEnabled(true);
			} else {
				setIcon(pauseIcon);
				frameAdvanceBtn.setEnabled(false);
			}
		}
	}

	private class FrameAdvanceButton extends JButton implements ActionListener {

		FrameAdvanceButton() {
			BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) img.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setPaint(Color.BLUE);
			int xpoints[] = { 8, 18, 8 };
			int ypoints[] = { 2, 10, 18 };
			g.fill(new Polygon(xpoints, ypoints, xpoints.length));
			g.fill(new Rectangle2D.Double(2, 2, 4, 16));
			Icon icon = new ImageIcon(img);

			setIcon(icon);

			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			if (!playFlg) {
				playFlg = true;
				frameAdvanceFlg = true;
			}
		}
	}
}
