package uk.ac.ox.cs.pdq.fol;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

/**
 * A logical implication.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <S> the generic type
 * @param <T> the generic type
 */
public class Implication<S extends Formula, T extends Formula>
		extends BinaryFormula<S, T>
		implements Rule<S, T> {

	/**
	 * Constructor for Implication.
	 * @param body S
	 * @param head T
	 */
	protected Implication(S body, T head) {
		super(LogicalSymbols.IMPLIES, body, head);
	}

	/**
	 * Constructor for Implication.
	 * @param pair Pair<S,T>
	 */
	protected Implication(Pair<S, T> pair) {
		super(LogicalSymbols.IMPLIES, pair);
	}

	/**
	 * Convenience constructor for Implication.
	 *
	 * @param <S> the generic type
	 * @param <T> the generic type
	 * @param body S
	 * @param head T
	 * @return Implication<S,T>
	 */
	public static <S extends Formula, T extends Formula> Implication<S, T> of(S body, T head) {
		return new Implication<>(body, head);
	}

	/**
	 * Convenience constructor for Implication.
	 *
	 * @param <S> the generic type
	 * @param <T> the generic type
	 * @param pair Pair<S,T>
	 * @return Implication<S,T>
	 */
	public static <S extends Formula, T extends Formula> Implication<S, T> of(Pair<S, T> pair) {
		return new Implication<>(pair);
	}

	@Override
	public Formula ground(Map<Variable, Constant> mapping) {
		S s = (S) this.left.ground(mapping);
		T t = (T) this.right.ground(mapping);
		return Implication.of(s, t);
	}

	/**
	 * Gets the implication's head, i.e., the consequent.
	 *
	 * @return T
	 * @see uk.ac.ox.cs.pdq.fol.Rule#getHead()
	 */
	@Override
	public T getHead() {
		return this.getRight();
	}

	/**
	 * Gets the implication's body.
	 *
	 * @return S
	 * @see uk.ac.ox.cs.pdq.fol.Rule#getBody()
	 */
	@Override
	public S getBody() {
		return this.getLeft();
	}
	
	
	@Override
	public boolean contains(Predicate s) {
		for (Atom atom: this.getAtoms()) {
			if (atom.getPredicate().equals(s)) {
				return true;
			}
		}
		return false;
	}
}