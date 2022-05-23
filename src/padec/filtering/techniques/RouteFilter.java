package padec.filtering.techniques;

import padec.application.information.RunningRoute;
import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;
import padec.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RouteFilter implements FilterTechnique<List<RunningRoute>> {

    public static final String ROUTE_KIND_KEY = "kind";
    public static final String BEFORE_DATE_KEY = "before";
    public static final String AFTER_DATE_KEY = "after";
    public static final String BETWEEN_DATES_KEY = "between";

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
        if (parameters.containsKey(ROUTE_KIND_KEY)) {
            normalized.put(ROUTE_KIND_KEY, parameters.get(ROUTE_KIND_KEY));
        }
        return normalized;
    }

    private Double calcPrecision(Pair<Date, Date> between, Boolean typeFilter) {
        long diff = between.getB().getTime() - between.getA().getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        double precision = (double) daysDiff * (typeFilter ? 0.5 : 1);
        // The LOWER the precision, the MORE PRECISE (i.e. less fuzzy) data is!! Hence, multiplication.
        return precision * -1;
    }

    private Double getActualPrecision(List<RunningRoute> data, List<RunningRoute> filtered) {
        Date maxDate = data.stream().map(RunningRoute::getRouteDate).max(Comparator.comparing(Date::getDate)).get();
        Date minDate = data.stream().map(RunningRoute::getRouteDate).min(Comparator.comparing(Date::getDate)).get();
        List<Double> partialPrecisions = new ArrayList<>();
        LocalDate dt;
        for (dt = new java.sql.Date(minDate.getTime()).toLocalDate(); dt.isBefore(new java.sql.Date(maxDate.getTime()).toLocalDate()); dt = dt.plusDays(1)) {
            Date dtVal = java.sql.Date.valueOf(dt);
            List<RunningRoute> eventsInDate = data.stream().filter(h -> h.getRouteDate().equals(dtVal)).collect(Collectors.toList());
            List<RunningRoute> filteredEventsInDate = filtered.stream().filter(h -> h.getRouteDate().equals(dtVal)).collect(Collectors.toList());
            if (eventsInDate.size() > 0) {
                Double prec = (eventsInDate.size() - (eventsInDate.size() - filteredEventsInDate.size())) / ((double) eventsInDate.size());
                partialPrecisions.add(1 - prec);
            } else {
                partialPrecisions.add((double) 0);
            }
        }
        Double finalRes = partialPrecisions.stream().map((Double a) -> a * a).reduce(Double::sum).get();
        return Math.pow(finalRes, 1.0 / partialPrecisions.size());
    }

    @Override
    public FilteredData<List<RunningRoute>> filter(List<RunningRoute> data, Map<String, Object> parameters) {
        FilteredData<List<RunningRoute>> result = null;
        if (checkCorrectParams(parameters)) {
            parameters = normalizeParams(parameters);
            Pair<Date, Date> dates = (Pair<Date, Date>) parameters.get(BETWEEN_DATES_KEY);
            List<RunningRoute> between = data.stream()
                    .filter(h -> h.getRouteDate().compareTo(dates.getA()) > 0) // Keep those that happened after that date
                    .collect(Collectors.toList());
            between.removeIf(h -> h.getRouteDate().compareTo(dates.getB()) > 0); // Remove those that happened after that date
            if (parameters.containsKey(ROUTE_KIND_KEY)) {
                Integer routeKind = (Integer) parameters.get(ROUTE_KIND_KEY);
                List<RunningRoute> validKind = between.stream().filter(h -> h.getRouteType() == routeKind).collect(Collectors.toList());
                between.removeIf(h -> !validKind.contains(h));
            }
            Double actualPrec;
            try {
                actualPrec = getActualPrecision(data, between);
            } catch (NoSuchElementException ex) {
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

        return calcPrecision(dates, normalized.containsKey(ROUTE_KIND_KEY));
    }
}
