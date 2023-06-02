package ex12;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

public class MusicPlayer extends JFrame {

	//プレイヤー(BasicPlayerライブラリー)
	BasicPlayer player;

	//曲情報
	Map audioInfo;

	//ボタンテキスト
	// 停止
	final static String STOP = "■";
	//再生
	final static String PLAY = "▶";
	//一時停止
	final static String PAUSE = "I I";

	// 再生ボタン
	JButton basicPlay;
	// 曲名ラベル
	JLabel label;
	// シークバー
	JScrollBar seekBar;
	// 時間ラベル
	JLabel timeLabel;
	// 音量調整スライダー
	JSlider volumeSlider;
	// 音量ラベル
	JLabel volumeLabel;

	public static void main(String[] args) {
		new MusicPlayer();
	}

	public MusicPlayer() {
		setTitle("MP3プレイヤー");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100, 100, 300, 170);
		setLayout(new FlowLayout());
		setResizable(false);

		JMenuBar menuBar = new JMenuBar(); // メニューバーの生成
		setJMenuBar(menuBar); // メニューバーの設定

		JMenu file = new JMenu("ファイル"); // メニューの生成
		menuBar.add(file); // メニューバーにメニューを追加

		JMenuItem item;
		item = new JMenuItem(new OpenAction()); // メニュー項目の生成
		file.add(item); // メニューにメニュー項目を追加
		file.addSeparator(); // メニューにセパレータを追加
		item = new JMenuItem(new ExitAction()); // メニュー項目の生成
		file.add(item); // メニューにメニュー項目を追加

		// ラベル設置
		label = new JLabel("ファイルからmp3を読み込んでください。");
		add(label);

		// シークバー設置
		seekBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 0, 0, 1000);
		seekBar.setPreferredSize(new Dimension(200, seekBar.getPreferredSize().height));
		// シークバーの音量変更時に呼び出せる
		seekBar.addAdjustmentListener(volumeChangeAL);
		// シークバーの追加
		add(seekBar);

		// 時間ラベル
		timeLabel = new JLabel("00:00");
		//時間ラベルの追加
		add(timeLabel);

		// 音量調整
		volumeSlider = new JSlider(0, 100, 100);
		// 音量が変更したら
		volumeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					// 音量を変更する[setGain]
					player.setGain((double) volumeSlider.getValue() / 100);
					// ラベルの更新
					volumeLabel.setText("音量 : " + volumeSlider.getValue() + "%");
				} catch (BasicPlayerException e1) {
					e1.printStackTrace();
				}
			}
		});

		// 音量バーの追加
		add(volumeSlider);
		// 音量バーラベル
		volumeLabel = new JLabel("音量 : " + volumeSlider.getValue() + "%");
		//音量ラベルの追加
		add(volumeLabel);

		// 再生ボタンの設定
		basicPlay = new JButton(PLAY);
		basicPlay.addActionListener(new basicPlayAction());
		//停止ボタンの設定
		JButton bStop = new JButton(STOP);
		bStop.addActionListener(new basicStopAction());
		//再生・停止ボタンの追加
		add(basicPlay);
		add(bStop);

		player = new BasicPlayer();
		//曲情報の読み取り
		player.addBasicPlayerListener(bpl);

		setVisible(true);

	}

	AdjustmentListener volumeChangeAL = new AdjustmentListener() {
		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			if (!seekBar.getValueIsAdjusting()) {
				try {
					// 位置の計算
					long bytes = Long.parseLong(audioInfo.get("audio.length.bytes").toString());
					long seek = bytes * seekBar.getValue() / seekBar.getMaximum();

					// シークバー
					player.removeBasicPlayerListener(bpl);
					player.seek(seek);
					player.addBasicPlayerListener(bpl);
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				} catch (BasicPlayerException e1) {
					e1.printStackTrace();
				}
			}
		}
	};

	BasicPlayerListener bpl = new BasicPlayerListener() {
		@Override
		public void stateUpdated(BasicPlayerEvent event) {
		}

		@Override
		public void setController(BasicController controller) {
		}

		@Override
		//時間計算
		public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
			long total = Long.parseLong(audioInfo.get("audio.length.bytes").toString());
			int newValue = (int) ((double) bytesread / total * seekBar.getMaximum());
			if (newValue != seekBar.getValue() && !seekBar.getValueIsAdjusting()) {
				seekBar.removeAdjustmentListener(volumeChangeAL);

				// トータル秒数計算
				long bitrate = Long.parseLong(audioInfo.get("bitrate").toString());
				int seconds = (int) (total / (bitrate / 8));

				// 現在の秒数
				int nowSec = (int) (bytesread * seconds / total);

				// 時間の分
				int tm = seconds / 60;
				//時間の秒
				int ts = seconds % 60;
				//分
				int m = nowSec / 60;
				//秒
				int s = nowSec % 60;

				seekBar.setValue(newValue);
				timeLabel.setText(String.format("%1$02d:%2$02d / %3$02d:%4$02d", m, s, tm, ts));
				seekBar.addAdjustmentListener(volumeChangeAL);
			}
		}

		@Override
		public void opened(Object stream, Map properties) {
			audioInfo = properties;
		}
	};

	class OpenAction extends AbstractAction {
		OpenAction() {
			putValue(Action.NAME, "ファイルを開く");
			putValue(Action.SHORT_DESCRIPTION, "開く");
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("ファイルを開く");
			fileChooser.setFileFilter(new Mp3FileFilter());
			int returnFile = fileChooser.showOpenDialog(label);
			if (returnFile != JFileChooser.APPROVE_OPTION) {
				return;
			}

			try {
				File file = fileChooser.getSelectedFile();
				open(file);
			} catch (BasicPlayerException e1) {
				e1.printStackTrace();
			}

		}
	}

	class ExitAction extends AbstractAction {
		ExitAction() {
			putValue(Action.NAME, "終了");
			putValue(Action.SHORT_DESCRIPTION, "終了");
		}

		public void actionPerformed(ActionEvent e) {
			Object[] msg = { "アプリケーションを終了してよろしいですか？" };
			int ans = (int) JOptionPane.showConfirmDialog(label, msg, "確認", JOptionPane.YES_NO_OPTION);
			if (JOptionPane.YES_OPTION == ans) {
				System.exit(0);
			}
		}
	}

	// 再生ボタン
	class basicPlayAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int status = player.getStatus();
				if (status == BasicPlayer.PLAYING) { //再生中
					//一時停止
					pause();
				} else if (status == BasicPlayer.STOPPED) { //停止中
					//再生開始
					play();
				} else if (status == BasicPlayer.PAUSED) { //一時停止中
					//再生再開
					resume();
				}
			} catch (Exception ex) {

			}
		}
	}

	// 停止ボタン
	class basicStopAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				stop();
			} catch (Exception ex) {

			}
		}
	}

	// 再生
	private void play() throws BasicPlayerException {
		player.play();
		player.setGain((double) volumeSlider.getValue() / 100);
		basicPlay.setText(PAUSE);
	}

	// 停止
	private void stop() throws BasicPlayerException {
		player.stop();
		basicPlay.setText(PLAY);
		timeLabel.setText(String.format("停止"));
	}

	// 一時停止
	private void pause() throws BasicPlayerException {
		// playMode = PLAY_MODE_PAUSE;
		player.pause();
		basicPlay.setText(PLAY);
	}

	// 再生再開
	private void resume() throws BasicPlayerException {
		player.resume();
		basicPlay.setText(PAUSE);
	}

	// ファイルを開く
	private void open(File file) throws BasicPlayerException {
		label.setText(file.getName());
		label.setHorizontalAlignment(JLabel.CENTER);
		// プレイヤーにファイルを開かせる
		player.open(file);
		play(); // 再生
	}

	//mp3のみ
	class Mp3FileFilter extends FileFilter {
		String[] extensions = { "mp3" };
		String description = "MP3ファイル *.mp3";

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}
			String name = file.getName().toLowerCase();
			for (int i = 0; i < extensions.length; i++) {
				if (name.endsWith(extensions[i])) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			return this.description;
		}
	}

}
