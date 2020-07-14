package padec.parser;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RuleParser {
    public class rule {
        private String type;
        private String attribute;
        private double values;
        private String contributor;
        private String operator;
    }

    public static rule main() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream =
                new FileInputStream("../rule.yml");

        rule rules = yaml.loadAs(inputStream, rule.class);
        return rules;
    }
}