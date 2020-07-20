package padec.filtering.techniques;

import padec.util.Pair;
import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HistoryFuzzy implements FilterTechnique<List<Pair<String, Date>>> {

    public static final String BETWEEN_DATES_KEY = "between";
    public static final String AT_LEAST_TIMES_KEY = "times";
    public static final String BEFORE_DATE_KEY = "before";
    public static final String AFTER_DATE_KEY = "after";

    private boolean checkCorrectParams(Map<String, Object> parameters) {
        boolean correct = true;
        if (parameters == null) {
            return true;
        }
        if ((parameters.containsKey(BETWEEN_DATES_KEY) && parameters.containsKey(BEFORE_DATE_KEY))
                || (parameters.containsKey(BETWEEN_DATES_KEY) && parameters.containsKey(AFTER_DATE_KEY))) {
            correct = false;
        }
        if (parameters.containsKey(BETWEEN_DATES_KEY)) {
            if (!(parameters.get(BETWEEN_DATES_KEY) instanceof Pair)) {
                correct = false;
            } else {
                Pair between = (Pair) parameters.get(BETWEEN_DATES_KEY);
                if ((!(between.getA() instanceof Date)) || (!(between.getB() instanceof Date))) {
                    correct = false;
                }
            }
        }
        if (parameters.containsKey(BEFORE_DATE_KEY) && !(parameters.get(BEFORE_DATE_KEY) instanceof Date || parameters.get(BEFORE_DATE_KEY) instanceof String)) {
            correct = false;
        }
        if (parameters.containsKey(AFTER_DATE_KEY) && !(parameters.get(AFTER_DATE_KEY) instanceof Date || parameters.get(AFTER_DATE_KEY) instanceof String)) {
            correct = false;
        }
        if (parameters.containsKey(AT_LEAST_TIMES_KEY) && !(parameters.get(AT_LEAST_TIMES_KEY) instanceof Integer)) {
            correct = false;
        }
        return correct;
    }

    private Map<String, Object> normalizeParams(Map<String, Object> parameters) {
        Map<String, Object> normalized = new HashMap<>();
        if (parameters == null) {
            parameters = new HashMap<>();
        }

        if (parameters.containsKey(BETWEEN_DATES_KEY)) {
            normalized.put(BETWEEN_DATES_KEY, parameters.get(BETWEEN_DATES_KEY));
        } else {
            if (parameters.containsKey(BEFORE_DATE_KEY) && parameters.get(BEFORE_DATE_KEY) instanceof String) {
                try {
                    parameters.put(BEFORE_DATE_KEY, new SimpleDateFormat("yyyy-MM-dd").parse((String) parameters.get(BEFORE_DATE_KEY)));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
            if (parameters.containsKey(AFTER_DATE_KEY) && parameters.get(AFTER_DATE_KEY) instanceof String) {
                try {
                    parameters.put(AFTER_DATE_KEY, new SimpleDateFormat("yyyy-MM-dd").parse((String) parameters.get(AFTER_DATE_KEY)));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
            if (parameters.containsKey(BEFORE_DATE_KEY) && parameters.containsKey(AFTER_DATE_KEY)) {
                Pair<Date, Date> bet = new Pair<>((Date) parameters.get(AFTER_DATE_KEY), (Date) parameters.get(BEFORE_DATE_KEY));
                normalized.put(BETWEEN_DATES_KEY, bet);
            } else {
                if (parameters.containsKey(BEFORE_DATE_KEY)) {
                    Pair<Date, Date> bet = new Pair<>(new Date(0), (Date) parameters.get(BEFORE_DATE_KEY));
                    normalized.put(BETWEEN_DATES_KEY, bet);
                } else {
                    if (parameters.containsKey(AFTER_DATE_KEY)) {
                        Pair<Date, Date> bet = new Pair<>((Date) parameters.get(AFTER_DATE_KEY), new Date());
                        normalized.put(BETWEEN_DATES_KEY, bet);
                    } else {
                        Pair<Date, Date> bet = new Pair<>(new Date(0), new Date());
                        normalized.put(BETWEEN_DATES_KEY, bet);
                    }
                }
            }
        }
        normalized.put(AT_LEAST_TIMES_KEY, parameters.getOrDefault(AT_LEAST_TIMES_KEY, 0));
        return normalized;
    }

    private static List<String> moreThanTimes(List<Pair<String, Date>> history, Integer times) {
        List<String> places = history.stream().map(Pair::getA).collect(Collectors.toList());
        Map<String, Long> reps = places.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return reps.keySet().stream().filter(place -> reps.get(place) >= times).collect(Collectors.toList());
    }

    private Double calcPrecision(Pair<Date, Date> between, Integer times) {
        long diff = between.getB().getTime() - between.getA().getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        double precision = (double) daysDiff / (times == 0 ? 1 : times);
        // The LOWER the precision, the MORE PRECISE (i.e. less fuzzy) data is!! Hence, multiplication.
        return precision * -1;
    }

    @Override
    public FilteredData<List<Pair<String, Date>>> filter(List<Pair<String, Date>> data, Map<String, Object> parameters) {
        FilteredData<List<Pair<String, Date>>> result = null;
        if (checkCorrectParams(parameters)) {
            parameters = normalizeParams(parameters);
            Pair<Date, Date> dates = (Pair<Date, Date>) parameters.get(BETWEEN_DATES_KEY);
            List<Pair<String, Date>> between = data.stream()
                    .filter(h -> h.getB().compareTo(dates.getA()) > 0) // Keep those that happened after that date
                    .collect(Collectors.toList());
            between.removeIf(h -> h.getB().compareTo(dates.getB()) > 0); // Remove those that happened after that date
            List<String> sPlaces = moreThanTimes(between, (Integer) parameters.get(AT_LEAST_TIMES_KEY));
            between.removeIf(h -> !sPlaces.contains(h.getA()));
            result = new FilteredData<>(between, calcPrecision(dates, (Integer) parameters.get(AT_LEAST_TIMES_KEY)));
        }
        return result;
    }

    @Override
    public Double getPrecision(Map<String, Object> parameters) {
        if (!checkCorrectParams(parameters)) {
            return Double.MAX_VALUE;
        }
        Map<String, Object> normalized = normalizeParams(parameters);
        Pair<Date, Date> dates = (Pair<Date, Date>) normalized.get(BETWEEN_DATES_KEY);

        return calcPrecision(dates, (Integer) normalized.get(AT_LEAST_TIMES_KEY));
    }

}
