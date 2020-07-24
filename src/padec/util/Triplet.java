package padec.util;

import java.io.Serializable;

public class Triplet<T, U, V> extends Pair<T, U> implements Comparable, Serializable {
    private static final long serialVersionUID = 1804351248655631403L;
    private V c;

    public Triplet() {
        super();
        this.c = null;
    }

    public Triplet(T a, U b, V c) {
        super(a, b);
        this.c = c;
    }

    public V getC() {
        return c;
    }

    public void setC(V c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "<" + this.getA().toString() + ", " + this.getB().toString() + ", " + c.toString() + ">";
    }

    @Override
    public int compareTo(Object o) {
        int result = 0;
        if (o instanceof Triplet) {
            Triplet t = (Triplet) o;
            if ((this.getA() instanceof Comparable) && (this.getB() instanceof Comparable) && (c instanceof Comparable)
                    && (t.getA() instanceof Comparable) && (t.getB() instanceof Comparable) && (t.getC() instanceof Comparable)
            ) {
                Comparable cA = (Comparable) this.getA();
                Comparable cB = (Comparable) this.getB();
                Comparable cC = (Comparable) c;
                if (cA.compareTo(t.getA()) < 0 && cB.compareTo(t.getB()) < 0 && cC.compareTo(t.getC()) < 0) {
                    result = -1;
                } else {
                    if (cA.compareTo(t.getA()) > 0 && cB.compareTo(t.getB()) > 0 && cC.compareTo(t.getC()) > 0) {
                        result = 1;
                    }
                }
            }
        }
        return result;
    }

}
