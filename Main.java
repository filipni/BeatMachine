import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

class BeatMachine extends JFrame {
    private static final int NUMBER_OF_INSTRUMENTS = 7;
    private static final int NUMBER_OF_BEATS = 16;

    private HashMap<String, Integer> instrumentNameToId = new HashMap<>();

    private JButton playButton;
    private JButton stopButton;
    private JButton tempoUpButton;
    private JButton tempoDownButton;
    private JPanel instrumentPanel;
    private JPanel beatGridPanel;
    private JPanel buttonPanel;

    private JComboBox[] instrumentSelectors = new JComboBox[NUMBER_OF_INSTRUMENTS];
    private JSpinner[] noteSelectors = new JSpinner[NUMBER_OF_INSTRUMENTS];
    private JCheckBox[][] beatBoxes = new JCheckBox[NUMBER_OF_INSTRUMENTS][NUMBER_OF_BEATS];

    private BeatGenerator generator;

    private ArrayList<Integer> getSelectedInstruments() {
        ArrayList<Integer> selectedInstruments = new ArrayList<>();
        for (JComboBox c : instrumentSelectors) {
            int instrumentId = instrumentNameToId.get(c.getSelectedItem());
            selectedInstruments.add(instrumentId);
        }
        return selectedInstruments;
    }

    private void transferBeatGrid() {
        for (int i = 0; i < NUMBER_OF_INSTRUMENTS; i++) {
            for (int j = 0; j < NUMBER_OF_BEATS; j++) {
               if (beatBoxes[i][j].isSelected()) {
                   int note = (int) noteSelectors[i].getValue();
                   generator.updateChannelBeat(i, j, note);
               }
               else
                   generator.updateChannelBeat(i, j, 0);
            }
        }
    }

    private void createPlayButton() {
        playButton = new JButton("Play");
        playButton.addActionListener(e -> {
            try {
                generator.setInstruments(getSelectedInstruments());
                transferBeatGrid();
                generator.play();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void createStopButton() {
       stopButton = new JButton("Stop");
       stopButton.addActionListener(e -> generator.stop());
    }

    private void createTempoUpButton() {
        tempoUpButton = new JButton("Tempo up");
        tempoUpButton.addActionListener(e -> {
            generator.tempoUp();
        });
    }

    private void createTempoDownButton() {
        tempoDownButton = new JButton("Tempo down");
        tempoDownButton.addActionListener(e -> {
            generator.tempoDown();
        });
    }

    private void createBeatGrid() {
        beatGridPanel = new JPanel(new GridLayout(NUMBER_OF_INSTRUMENTS, NUMBER_OF_BEATS));
        Border etchedBorder = BorderFactory.createEtchedBorder();
        Border title = BorderFactory.createTitledBorder(etchedBorder, "Beats");
        beatGridPanel.setBorder(title);

        for (int i = 0; i < NUMBER_OF_INSTRUMENTS; i++) {
            for (int j = 0; j < NUMBER_OF_BEATS; j++) {
                JCheckBox box = new JCheckBox();
                beatBoxes[i][j] = box;
                beatGridPanel.add(box);
            }
        }
    }

    private void createInstrumentPanel() {
        instrumentPanel = new JPanel();
        instrumentPanel.setLayout(new BoxLayout(instrumentPanel, BoxLayout.Y_AXIS));
        Border etchedBorder = BorderFactory.createEtchedBorder();
        Border title = BorderFactory.createTitledBorder(etchedBorder, "Instruments & Notes");
        instrumentPanel.setBorder(title);

        Object[] availableInstruments = instrumentNameToId.keySet().toArray();

        for (int i = 0; i < NUMBER_OF_INSTRUMENTS; i++) {
            JPanel instrumentInfo = new JPanel();

            JComboBox<String> instrumentDropDown = new JComboBox(availableInstruments);
            instrumentSelectors[i] = instrumentDropDown;
            instrumentInfo.add(instrumentDropDown);

            SpinnerModel model = new SpinnerNumberModel(50, 0, 100, 1);
            JSpinner noteSelect = new JSpinner(model);
            noteSelectors[i] = noteSelect;
            instrumentInfo.add(noteSelect);
            instrumentPanel.add(instrumentInfo);
        }
    }

    private void createButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        Border etchedBorder = BorderFactory.createEtchedBorder();
        Border title = BorderFactory.createTitledBorder(etchedBorder, "Controls");
        buttonPanel.setBorder(title);

        createPlayButton();
        createStopButton();
        createTempoUpButton();
        createTempoDownButton();

        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(tempoUpButton);
        buttonPanel.add(tempoDownButton);
    }

    private void createInstrumentMap() throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        javax.sound.midi.Instrument[] orchestra = synth.getAvailableInstruments();
        for (javax.sound.midi.Instrument i : orchestra) {
            String[] stringParts = i.toString().replace('#', ' ').split(" ");
            int instrumentId = Integer.parseInt(stringParts[stringParts.length - 1]);
            instrumentNameToId.put(i.getName(), instrumentId);
        }
        synth.close();
    }

    BeatMachine() throws InvalidMidiDataException, MidiUnavailableException {
        createBeatGrid();
        createButtonPanel();
        createInstrumentMap();
        createInstrumentPanel();

        this.add(instrumentPanel, BorderLayout.WEST);
        this.add(beatGridPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.EAST);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("The Beat Machine");
        this.pack();
        this.setResizable(false);
        this.setVisible(true);

        generator = new BeatGenerator(NUMBER_OF_INSTRUMENTS, NUMBER_OF_BEATS);
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        new BeatMachine();
    }
}
