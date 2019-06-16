import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BeatGenerator {

    private final int numInstruments;
    private final int numBeats;
    private int[][] beatGrid;
    private List<Integer> instruments;
    private final float tempoAdjustFactor = 0.3f;

    private Sequencer player;
    private Sequence sequence;
    private Track track;

    public BeatGenerator(int numInstruments, int numBeats) throws MidiUnavailableException, InvalidMidiDataException {
        this.numInstruments = numInstruments;
        this.instruments = new ArrayList<>(Collections.nCopies(numInstruments,0));
        this.numBeats = numBeats;
        beatGrid = new int[numInstruments][numBeats];

        player = MidiSystem.getSequencer();
        player.setTempoInBPM(120);
        player.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        player.open();

        sequence = new Sequence(Sequence.PPQ, 4);
    }

    public void setInstruments(ArrayList<Integer> instrumentIds)
    {
        // Todo: Check length of parameter
        instruments = instrumentIds;
    }

    private void setupInstruments() throws InvalidMidiDataException {
        for (int i = 0; i < numInstruments; i++)
            setChannelInstrument(i, instruments.get(i));
    }

    private void setChannelInstrument(int channel, int instrument) throws InvalidMidiDataException {
        setChannelInstrument(channel, instrument, 0);
    }

    private void setChannelInstrument(int channel, int instrument, int beat) throws InvalidMidiDataException {
        final int MIDI_CMD_CHANGE_INSTRUMENT = 192;
        ShortMessage changeInstrumentMsg = new ShortMessage();
        changeInstrumentMsg.setMessage(MIDI_CMD_CHANGE_INSTRUMENT, channel, instrument, 0);
        MidiEvent changeInstrumentEvent = new MidiEvent(changeInstrumentMsg, beat);
        track.add(changeInstrumentEvent);
    }

    public void updateChannelBeat(int channel, int beat, int note) {
        beatGrid[channel][beat] = note;
    }

    public void tempoUp() {
        float tempoFactor = player.getTempoFactor();
        player.setTempoFactor(tempoFactor * (1 + tempoAdjustFactor));
    }

    public void tempoDown() {
        float tempoFactor = player.getTempoFactor();
        player.setTempoFactor(tempoFactor * (1 - tempoAdjustFactor));
    }

    private void addNote(int channel, int note, int noteStart) throws InvalidMidiDataException {
        addStartEvent(channel, note, noteStart);
        addStopEvent(channel, note, noteStart + 1);
    }

    private void addStartEvent(int channel, int note, int noteStart) throws InvalidMidiDataException {
        final int MIDI_CMD_START_NOTE = 144;
        ShortMessage startMsg = new ShortMessage();
        startMsg.setMessage(MIDI_CMD_START_NOTE, channel, note ,100);
        MidiEvent startEvent = new MidiEvent(startMsg, noteStart);
        track.add(startEvent);
    }

    private void addStopEvent(int channel, int note, int noteEnd) throws InvalidMidiDataException {
        final int MIDI_CMD_STOP_NOTE = 128;
        ShortMessage stopMsg = new ShortMessage();
        stopMsg.setMessage(MIDI_CMD_STOP_NOTE, channel, note, 100);
        MidiEvent stopEvent = new MidiEvent(stopMsg, noteEnd);
        track.add(stopEvent);
    }


    private void createBeat() throws InvalidMidiDataException {
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        setupInstruments();
        for (int i = 0; i < this.numInstruments; i++) {
            for (int j = 0; j < this.numBeats; j++) {
                int note = beatGrid[i][j];
                if (note != 0)
                    addNote(i, note, j);
            }
        }
        // Dummy event to make sure we wait the correct number of beats before restarting the loop
        setChannelInstrument(0, instruments.get(0), numBeats - 1);
    }

    public void play() throws InvalidMidiDataException {
        createBeat();
        player.setSequence(sequence);
        player.start();
    }

    public void stop() {
        player.stop();
    }
}
