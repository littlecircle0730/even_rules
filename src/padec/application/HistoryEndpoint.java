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

    public HistoryEndpoint(String histoFile) {
        Gson gson = new Gson();
        history = new ArrayList<>();
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
                nTriple.setC((Double) m.getOrDefault("rating", -1.0));
                history.add(nTriple);
            }
            sort();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void sort() {
        history.sort(Comparator.comparing(Pair::getB));
    }

    @Override
    public List<Pair<String, Date>> execute(Map<String, Object> parameters) {
        return history;
    }
}
