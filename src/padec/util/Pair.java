package padec.util;

import padec.rule.operator.RangeOperator;

import java.io.Serializable;

public class Pair<T, U> implements Comparable, Serializable {

    private static final long serialVersionUID = 7659291834859850839L;
    private static final double EPSILON = 50;
    private T a;
    private U b;

    public Pair() {
        this.a = null;
        this.b = null;
    }

    public Pair(T a, U b) {
        this.a = a;
        this.b = b;
    }

    public U getB() {
        return b;
    }

    public void setB(U b) {
        this.b = b;
    }

    public T getA() {
        return a;
    }

    public void setA(T a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "<" + a.toString() +", " + b.toString()+">";
    }

    @Override
    public int compareTo(Object o) {
        int result = 0;
        if (o instanceof Pair){
            Pair p = (Pair) o;
            if ((a instanceof Comparable) && (b instanceof Comparable) && (p.getA() instanceof Comparable) && p.getB() instanceof Comparable) {
                Comparable cA = (Comparable) a;
                Comparable cB = (Comparable) b;
                if (cA.compareTo(p.getA()) < 0 && cB.compareTo(p.getB()) < 0){
                    result = -1;
                }
                else{
                    if(cA.compareTo(p.getA()) > 0 && cB.compareTo(p.getB()) > 0){
                        result = 1;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        boolean isEqual = false;
        if (o instanceof Pair) {
            Pair oPair = (Pair) o;
            Double combinedRes = (Double) new RangeOperator().combine(this, o);
            if (combinedRes == null) {
                isEqual = (a != null && a.equals(oPair.getA())) && (b != null && b.equals(oPair.getB()));
            } else {
                isEqual = combinedRes <= EPSILON;
            }
        }
        return isEqual;
    }
}
