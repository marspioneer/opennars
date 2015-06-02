package nars.nal.term;

import nars.util.data.id.Identifier;
import nars.util.data.id.UTF8Identifier;
import nars.util.utf8.FastByteComparisons;

import java.util.Arrays;

/** implementation of a compound which generates and stores its name as a CharSequence, which is used for equality and hash*/
abstract public class DefaultCompound extends Compound {

    public DefaultCompound(Term... components) {
        super(components);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;

        if (that == null) return false;

        //if the other class is not a DefaultCompound it is considered non-equal, so the use of implementations must be consistent
        //if (!(that instanceof nars.nal.term.DefaultCompound)) return false;

        if (getClass() != that.getClass()) return false;


        final nars.nal.term.DefaultCompound t = (nars.nal.term.DefaultCompound)that;
//        if ((name == null) || (t.name == null)) {
//            //check operate first because name() may to avoid potential construction of name()
//            if (/*operator()!=t.operator()|| */ getComplexity() != t.getComplexity() )
//                return false;
//        }

        return equalID(t);
//        if (equalID(t)) {
//            share(t);
//            return true;
//        }
//        return false;
    }

    @Override
    public int compareSubterms(final Compound otherCompoundOfEqualType) {
        DefaultCompound o = ((DefaultCompound) otherCompoundOfEqualType);
        //int h = Integer.compare(hashCode(), o.hashCode());
        //if (h == 0) {
            //byte[] n1 = name();
            //byte[] n2 = o.name();
            int c = name().compareTo(o.name()); //FastByteComparisons.compare(n1, n2);
            /*if ((c == 0) && (n1!=n2)) {
                //equal string, ensure that the same byte[] instance is shared to accelerate equality comparison
                share(o);
            }*/
            return c;
        //}
        //return h;
    }

    protected void share(DefaultCompound equivalent) {
        super.share(equivalent);
        if (!hasVar()) {
            //equivalent.name = this.name; //also share name key
        }
    }

    @Override
    public void invalidate() {
        if (hasVar()) {
            super.invalidate(); //invalidate name so it will be (re-)created lazily

            for (final Term t : term) {
                if (t instanceof Compound)
                    ((Compound) t).invalidate();
            }
        }
        else {
            setNormalized();
        }
    }




}
