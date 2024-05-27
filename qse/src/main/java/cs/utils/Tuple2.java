//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import com.google.common.base.Objects;

public class Tuple2<X, Y> {
    public final X _1;
    public final Y _2;

    public Tuple2(X _1, Y _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public X _1() {
        return this._1;
    }

    public Y _2() {
        return this._2;
    }

    public String toString() {
        return "Tuple2{_1=" + this._1 + ", _2=" + this._2 + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Tuple2<?, ?> tuple2 = (Tuple2)o;
            return Objects.equal(this._1, tuple2._1) && Objects.equal(this._2, tuple2._2);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this._1, this._2});
    }
}
