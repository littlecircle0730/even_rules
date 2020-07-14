package padec.parser;

import org.yaml.snakeyaml.Yaml;
import padec.parser.RuleParser.rule;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AccessLevelParser {
    public class accesslevel {
        private int position;
        private filter filters;
        private rule rules;
    }

    public class filter {
        private String technique;
        private params param;
    }

    public class params {
        private int precision;
    }

    public static accesslevel main() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream =
                new FileInputStream("../rule.yml");

        accesslevel accesslevels = yaml.loadAs(inputStream, accesslevel.class);
        return accesslevels;
    }
}