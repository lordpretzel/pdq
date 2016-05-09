package uk.ac.ox.cs.pdq.fol;

import java.util.Map;
import java.util.Objects;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The negation of a formula.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public final class Negation<T extends Formula> extends UnaryFormula<T> {

	/** The hash. */
	private Integer hash;

	/**
	 * Constructor for Negation.
	 * @param sf T
	 */
	private Negation(T sf) {
		super(LogicalSymbols.NEGATION, sf);
	}

	/**
	 * Convenience constructor for Negation.
	 *
	 * @param <T> the generic type
	 * @param f T
	 * @return Negation<T>
	 */
	public static <T extends Formula> Negation<T> of(T f) {
		Preconditions.checkArgument(f != null);
		return new Negation<>(f);
	}

	@Override
	public Formula ground(Map<Variable, Constant> mapping) {
		return Negation.of(this.child.ground(mapping));
	}

	/**
	 * Two negations are equal if their subformulas are equal (using equals()).
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.child.equals(((Negation) o).child);
	}


	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.operator, this.child);
		}
		return this.hash;
	}
}