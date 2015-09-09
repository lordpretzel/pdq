package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * A predicate's signature, associate a symbol with an arity.
 *
 * @author Julien Leblay
 */
public class Signature {

	/** Cached instance hash (only possible because signatures are immutable). */
	protected final int hash;

	/** Cached string representation of the signature */
	protected String rep;

	/** Signature name */
	protected final String name;

	/** Signature arity */
	protected final int arity;

	/** true, if this is the signature for an equality predicate */
	protected final boolean equality;

	/**
	 * Constructor for Signature.
	 * @param symbol String
	 * @param arity int
	 */
	public Signature(String symbol, int arity) {
		this(symbol, arity, false);
	}

	/**
	 * Constructor for Signature.
	 * @param symbol String
	 * @param arity int
	 * @param equality boolean
	 */
	public Signature(String symbol, int arity, boolean equality) {
		Preconditions.checkArgument(symbol != null);
		Preconditions.checkArgument(!symbol.isEmpty());
		Preconditions.checkArgument(arity >= 0);
		this.name = symbol;
		this.arity = arity;
		this.equality = equality;
		this.hash = Objects.hash(this.name, this.arity);
		this.rep = this.makeString();
	}

	/**
	 * @return the name of the predicate this signature underlies.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the arity of the predicate this signature underlies.
	 */
	public int getArity() {
		return this.arity;
	}

	/**
	 * @return true if the signature is of an equality predicate,
	 * false otherwise
	 */
	public boolean isEquality() {
		return this.equality;
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
				&& this.name.equals(((Signature) o).name)
				&& this.arity == ((Signature) o).arity;
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return this.hash;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.rep;
	}

	/**
	 * @return String
	 */
	private String makeString() {
		StringBuilder result = new StringBuilder();
		result.append(this.name).append('[').append(this.arity).append(']');
		return result.toString().intern();
	}
}
