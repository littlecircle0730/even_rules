package padec.attribute;

public class SoundLevel extends Attribute<Double> {

    private static final String SOUND_LEVEL_NAME = "soundLevel";

    public SoundLevel(){
        super(Double.class, SOUND_LEVEL_NAME);
    }
}
