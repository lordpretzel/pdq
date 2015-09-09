package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;


/**
 * A binary formula
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public abstract class BinaryFormula<S extends Formula, T extends Formula> extends AbstractFormula {

	/** The binary operator*/
	protected final LogicalSymbols operator;

	/** The left operand*/
	protected final S left;

	/** The right operand*/
	protected final T right;

	protected final Collection<Formula> children;

	/**
	 * Constructor for BinaryFormula.
	 * @param operator LogicalSymbols
	 * @param left S
	 * @param right T
	 */
	public BinaryFormula(LogicalSymbols operator, S left, T right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
		this.children = ImmutableList.<Formula>of(this.left, this.right);
	}

	/**
	 * Constructor for BinaryFormula.
	 * @param operator LogicalSymbols
	 * @param pair Pair<S,T>
	 */
	public BinaryFormula(LogicalSymbols operator, Pair<S, T> pair) {
		this(operator, pair.getLeft(), pair.getRight());
	}

	/**
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
				&& this.operator.equals(((BinaryFormula<?, ?>) o).operator)
				&& this.left.equals(((BinaryFormula<?, ?>) o).left)
				&& this.right.equals(((BinaryFormula<?, ?>) o).right);
	}


	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.operator, this.left, this.right);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return "(" + this.left + " " + this.operator + " " + this.right + ")";
	}


	/**
	 * @return LogicalSymbols
	 */
	public LogicalSymbols getOperator() {
		return this.operator;
	}


	/**
	 * @return S
	 */
	public S getLeft() {
		return this.left;
	}


	/**
	 * @return T
	 */
	public T getRight() {
		return this.right;
	}

	/**
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getPredicates()
	 */
	@Override
	public List<Predicate> getPredicates() {
		List<Predicate> result = new ArrayList<>();
		result.addAll(this.left.getPredicates());
		result.addAll(this.right.getPredicates());
		return result;
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		List<Term> terms = new ArrayList<>();
		terms.addAll(this.left.getTerms());
		terms.addAll(this.right.getTerms());
		return terms;
	}

	/**
	 * @return boolean
	 */
	public boolean isConjunction() {
		return this.operator.equals(LogicalSymbols.AND);
	}

	/**
	 * @return boolean
	 */
	public boolean isEquivalence() {
		return this.operator.equals(LogicalSymbols.EQUIVALENCE);
	}

	/**
	 * @return boolean
	 */
	public boolean isImplication() {
		return this.operator.equals(LogicalSymbols.IMPLIES);
	}

	/**
	 * @return boolean
	 */
	public boolean isDisjunction() {
		return this.operator.equals(LogicalSymbols.OR);
	}

	/**
	 * @return LogicalSymbols
	 */
	public LogicalSymbols getSymbol() {
		return this.operator;
	}

	/**
	 * @return Collection<Formula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public Collection<Formula> getChildren() {
		return this.children;
	}
}