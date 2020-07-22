package padec.perception;

import org.yaml.snakeyaml.Yaml;
import padec.attribute.Attribute;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PrivacyPerception {

    private static final String PERCEPTIONS_KEY = "perceptions";
    private static final String ATTRIBUTE_KEY = "attribute";
    private static final String CATEGORY_KEY = "category";

    private Map<Integer, Set<Class<? extends Attribute>>> innerMap;
    private Map<Class<? extends Attribute>, Integer> lookup;
    private int defaultCategory;

    public PrivacyPerception(){
        innerMap = new LinkedHashMap<>();
        lookup = new LinkedHashMap<>();
        defaultCategory = 0;
    }

    public PrivacyPerception(int defaultCategory) {
        innerMap = new LinkedHashMap<>();
        lookup = new LinkedHashMap<>();
        this.defaultCategory = defaultCategory;
    }

    public boolean loadFromYamlFile(String file){
        boolean success = false;
        try {
            InputStream iStream = new FileInputStream(file);
            Yaml yaml = new Yaml();
            Map<String, Object> yamlObj = yaml.load(iStream);
            innerMap.clear();
            List<Map<String, Object>> perceptions = (List<Map<String, Object>>) yamlObj.get(PERCEPTIONS_KEY);
            for (Map<String, Object> per : perceptions){
                String attrName = (String) per.get(ATTRIBUTE_KEY);
                Integer category = (Integer) per.get(CATEGORY_KEY);
                if (!innerMap.containsKey(category)){
                    innerMap.put(category, new LinkedHashSet<>());
                }
                Class<? extends Attribute> attr = (Class <? extends Attribute>) Class.forName(attrName);
                innerMap.get(category).add(attr);
                lookup.put(attr, category);
            }
            iStream.close();
            success = true;
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        }
        return success;
    }

    public boolean isCategory(int category){
        return innerMap.containsKey(category);
    }

    public Set<Class<? extends Attribute>> getAttributesFromCategory(int category){
        return innerMap.getOrDefault(category, new LinkedHashSet<>());
    }

    public List<Class<? extends Attribute>> getAttributeListFromCategory(int category){
        return new ArrayList<>(innerMap.getOrDefault(category, new LinkedHashSet<>()));
    }

    public int getCategoryFromAttribute(Class<? extends Attribute> attr){
        return lookup.getOrDefault(attr, defaultCategory);
    }

    public List<Integer> getCategories(){
        return new ArrayList<>(innerMap.keySet());
    }

    public void addPerception(Class<? extends Attribute> attr, int category){
        if(!innerMap.containsKey(category)){
            innerMap.put(category, new LinkedHashSet<>());
        }
        innerMap.get(category).add(attr);
        lookup.put(attr, category);
    }

}
