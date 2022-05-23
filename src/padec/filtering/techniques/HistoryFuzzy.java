package padec.filtering.techniques;

import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;
import padec.util.Pair;
import padec.util.Triplet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HistoryFuzzy implements FilterTechnique<List<Pair<String, Date>>> {

    public static final String BETWEEN_DATES_KEY = "between";
    public static final String AT_LEAST_TIMES_KEY = "times";
    public static final String BEFORE_DATE_KEY = "before";
    public static final String AFTER_DATE_KEY = "after";
    public static final String MIN_RATING_KEY = "min";
    public static final String MAX_RATING_KEY = "max";
    public static final String MIN_OR_RATING_KEY = "minor";
    public static final String MAX_OR_RATING_KEY = "maxor";

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
        normalized.put(MIN_RATING_KEY, parameters.getOrDefault(MIN_RATING_KEY, 0));
        normalized.put(MAX_RATING_KEY, parameters.getOrDefault(MAX_RATING_KEY, 5));
        normalized.put(MIN_OR_RATING_KEY, parameters.getOrDefault(MIN_OR_RATING_KEY, 5));
        normalized.put(MAX_OR_RATING_KEY, parameters.getOrDefault(MAX_OR_RATING_KEY, 0));
        return normalized;
    }

    private static List<String> moreThanTimes(List<Pair<String, Date>> history, Integer times) {
        List<String> places = history.stream().map(Pair::getA).collect(Collectors.toList());
        Map<String, Long> reps = places.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return reps.keySet().stream().filter(place -> reps.get(place) >= times).collect(Collectors.toList());
    }

    private static List<String> ratings(List<Pair<String, Date>> pairHistory, Double min, Double max) {
        List<Triplet<String, Date, Double>> history = new ArrayList<>();
        for (Pair p : pairHistory) {
            if (p instanceof Triplet) {
                history.add((Triplet) p);
            }
        }
        return history.stream().filter(h -> h.getC() >= min && h.getC() <= max).map(Triplet::getA).collect(Collectors.toList());
    }

    private static List<String> ratingsOr(List<Pair<String, Date>> pairHistory, Double minor, Double maxor) {
        List<Triplet<String, Date, Double>> history = new ArrayList<>();
        for (Pair p : pairHistory) {
            if (p instanceof Triplet) {
                history.add((Triplet) p);
            }
        }
        return history.stream().filter(h -> h.getC() <= minor || h.getC() >= maxor).map(Triplet::getA).collect(Collectors.toList());
    }

    private Double calcPrecision(Pair<Date, Date> between, Integer times) {
        long diff = between.getB().getTime() - between.getA().getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        double precision = (double) daysDiff / (times == 0 ? 1 : times);
        // The LOWER the precision, the MORE PRECISE (i.e. less fuzzy) data is!! Hence, multiplication.
        return precision * -1;
    }

    private Double getActualPrecision(List<Pair<String, Date>> data, List<Pair<String, Date>> filtered){
        Date maxDate = data.stream().map(Pair<String, Date>::getB).max(Comparator.comparing(Date::getDate)).get();
        Date minDate = data.stream().map(Pair<String, Date>::getB).min(Comparator.comparing(Date::getDate)).get();
        List<Double> partialPrecisions = new ArrayList<>();
        LocalDate dt;
        for (dt = new java.sql.Date(minDate.getTime()).toLocalDate(); dt.isBefore(new java.sql.Date(maxDate.getTime()).toLocalDate()); dt = dt.plusDays(1)){
            Date dtVal = java.sql.Date.valueOf(dt);
            List<Pair<String, Date>> eventsInDate = data.stream().filter(h -> h.getB().equals(dtVal)).collect(Collectors.toList());
            List<Pair<String, Date>> filteredEventsInDate = filtered.stream().filter(h -> h.getB().equals(dtVal)).collect(Collectors.toList());
            if (eventsInDate.size()>0){
                Double prec = (eventsInDate.size()-(eventsInDate.size() - filteredEventsInDate.size()))/((double) eventsInDate.size());
                partialPrecisions.add(1-prec);
            }
            else{
                partialPrecisions.add((double) 0);
            }
        }
        Double finalRes = partialPrecisions.stream().map((Double a) -> a*a).reduce(Double::sum).get();
        return Math.pow(finalRes, 1.0/partialPrecisions.size());
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
            List<String> rated = ratings(between, (Double) parameters.get(MIN_RATING_KEY), (Double) parameters.get(MAX_RATING_KEY));
            between.removeIf(h -> !rated.contains(h.getA()));
            //result = new FilteredData<>(between, calcPrecision(dates, (Integer) parameters.get(AT_LEAST_TIMES_KEY)));
            List<String> ratedOr = ratingsOr(between, (Double) parameters.get(MIN_OR_RATING_KEY), (Double) parameters.get(MAX_OR_RATING_KEY));
            between.removeIf(h -> !ratedOr.contains(h.getA()));
            Double actualPrec;
            try{
                actualPrec = getActualPrecision(data, between);
            }
            catch (NoSuchElementException ex){
                actualPrec = 1.0;
            }
            result = new FilteredData<>(between, actualPrec);
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
