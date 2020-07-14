package padec.parser;

import org.yaml.snakeyaml.Yaml;
import padec.application.Endpoint;
import padec.parser.AccessLevelParser.accesslevel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class LockParser {
    public class lock {
        private Endpoint endpoint;
        private List<accesslevel> accessLevelList;
    }

    public static lock main(String[] args) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream =
                new FileInputStream("../lockFormat.yml");

        lock locks = yaml.loadAs(inputStream, lock.class);
        return locks;
    }
}