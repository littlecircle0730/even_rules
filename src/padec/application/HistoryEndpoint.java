package padec.application;

import com.google.gson.Gson;
import padec.util.Pair;
import padec.util.Triplet;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryEndpoint implements Endpoint<List<Pair<String, Date>>> {

    /**
     * History, loaded
     */
    private List<Pair<String, Date>> history;

    /**
     * Flag to check load
     */
    private boolean loaded;

    public HistoryEndpoint() {
        loaded = false;
        history = new ArrayList<>();
    }

    public void load(String histoFile) {
        Gson gson = new Gson();
        try {
            FileReader fr = new FileReader(histoFile);
            List<Map> mList = gson.fromJson(fr, List.class);
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            fr.close();
            for (Map m : mList) {
                //Pair<String, Date> nPair = new Pair<>();
                Triplet<String, Date, Double> nTriple = new Triplet<>();
                nTriple.setA((String) m.get("place"));
                nTriple.setB(parser.parse((String) m.get("time")));
                nTriple.setC((Double) m.getOrDefault("rating", ((String) m.get("place")).length() % 5));
                history.add(nTriple);
            }
            loaded = true;
            sort();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public HistoryEndpoint(String histoFile) {
        loaded = false;
        history = new ArrayList<>();
        load(histoFile);
    }

    private void sort() {
        history.sort(Comparator.comparing(Pair::getB));
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public List<Pair<String, Date>> execute(Map<String, Object> parameters) {
        return history;
    }
}
