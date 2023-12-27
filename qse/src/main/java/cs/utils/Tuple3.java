//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import java.util.Objects;

public class Tuple3<X, Y, Z> {
    public final X _1;
    public final Y _2;
    public final Z _3;

    public Tuple3(X _1, Y _2, Z _3) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }

    public X _1() {
        return this._1;
    }

    public Y _2() {
        return this._2;
    }

    public Z _3() {
        return this._3;
    }

    public String toString() {
        return "Tuple3{_1=" + this._1 + ", _2=" + this._2 + ", _3=" + this._3 + "}";
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this._1, this._2, this._3});
    }

    public boolean equals(Object o) {
        if (!(o instanceof Tuple3)) {
            return false;
        } else {
            Tuple3 other = (Tuple3)o;
            return Objects.equals(this._1, other._1) && Objects.equals(this._2, other._2) && Objects.equals(this._3, other._3);
        }
    }
}
