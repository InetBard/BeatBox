import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BeatBox {
	JPanel mainPanel;
	ArrayList<JCheckBox> checkboxList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;

	String[] instrumentsNames = { "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
			"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
			"Low Mid Tom", "High Agogo", "Open High Conga" };
	int[] instruments = { 35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63 };

	public static void main(String[] args) {
		new BeatBox().buildGUI();
	}

	private void buildGUI() {
		theFrame = new JFrame("БИТБОКС МАШИНА");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		checkboxList = new ArrayList<JCheckBox>();
		Box buttonBox = new Box(BoxLayout.Y_AXIS);

		JButton start = new JButton("Старт");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);

		JButton stop = new JButton("Стоп");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);

		JButton upTempo = new JButton("Темп +");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);

		JButton downTempo = new JButton("Темп -");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);

		JButton serializeit = new JButton("Сериализовать");
		serializeit.addActionListener(new MySendListener());
		buttonBox.add(serializeit);

		JButton deSerializeit = new JButton("Десериализовать");
		deSerializeit.addActionListener(new MyReadListener());
		buttonBox.add(deSerializeit);

		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for (int i = 0; i < 16; i++) {
			nameBox.add(new Label(instrumentsNames[i]));
		}

		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);

		theFrame.getContentPane().add(background);

		GridLayout grid = new GridLayout(16, 16);
		grid.setVgap(1);
		grid.setHgap(1);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);

		for (int i = 0; i < 256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}

		setUpMidi();
		theFrame.setBounds(50, 50, 300, 300);
		theFrame.pack();
		theFrame.setVisible(true);
	}

	public void setUpMidi() {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void builTrackAndStart() {
		int[] trackList = null;
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		for (int i = 0; i < 16; i++) {
			trackList = new int[16];
			int key = instruments[i];
			for (int j = 0; j < 16; j++) {
				JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
				if (jc.isSelected()) {
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			makeTracks(trackList);
			track.add(makeEvent(176, 1, 127, 0, 16));
		}
		track.add(makeEvent(192, 9, 1, 0, 15));
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public class MyReadListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean[] checkboxState = null;
			try {
				FileInputStream fileIn = new FileInputStream(new File("C:\\123\\Checkbox.ser"));
				ObjectInputStream is = new ObjectInputStream(fileIn);
				checkboxState = (boolean[]) is.readObject();
				is.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			for (int i = 0; i < 256; i++) {
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if (checkboxState[i]) {
					check.setSelected(true);
				} else {
					check.setSelected(false);
				}
			}
			sequencer.stop();
			builTrackAndStart();
		}

	}

	public class  MySendListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean[] checkboxState = new boolean[256];
			for (int i = 0; i < 256; i++) {
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if (check.isSelected()) {
					checkboxState[i] = true;
				}
			}
			try {
				FileOutputStream fileStream = new FileOutputStream(new File("C:\\123\\CheckBox.ser"));
				ObjectOutputStream os = new ObjectOutputStream(fileStream);
				os.writeObject(checkboxState);
				os.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	public class MyStartListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			builTrackAndStart();
		}
	}

	public class MyStopListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			sequencer.stop();
		}
	}

	public class MyUpTempoListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) ((tempoFactor) * 1.03));
		}
	}

	public class MyDownTempoListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) ((tempoFactor) * .97));
		}
	}

	public void makeTracks(int[] list) {
		for (int i = 0; i < 16; i++) {
			int key = list[i];
			if (key != 0) {
				track.add(makeEvent(144, 9, key, 100, i));
				track.add(makeEvent(128, 9, key, 100, i + 1));
			}
		}
	}

	public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a, tick);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return event;
	}
}
