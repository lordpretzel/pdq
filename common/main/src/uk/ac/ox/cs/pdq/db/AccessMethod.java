package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * A AccessMethod method
 * Stores the positions of a relation's attributes whose values are required to access the relation.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class AccessMethod implements Serializable {

	/** Generated serial number */
	private static final long serialVersionUID = 1946416951995219490L;

	/** Types of access restrictions */
	public static enum Types {
		FREE, LIMITED, BOOLEAN
	}

	public static final String DEFAULT_PREFIX = "mt_";

	private static int globalCounter = 0;

	/** Input attribute positions */
	private final List<Integer> inputs;

	/** Access restriction */
	private final Types type;

	/** Name of the access restrictions */
	private final String name;

	/** String representation of the object */
	private String rep = null;

	/**
	 * Default constructor. Instantiates an inaccessible binding method
	 */
	public AccessMethod() {
		this(Types.FREE, new ArrayList<Integer>());
	}

	/**
	 * Copy constructor.
	 * @param binding AccessMethod
	 */
	public AccessMethod(AccessMethod binding) {
		this(binding.name, binding.type, binding.getInputs());
	}

	/**
	 * @param type
	 * @param bindingPositions
	 */
	public AccessMethod(Types type, List<Integer> bindingPositions) {
		this(DEFAULT_PREFIX + globalCounter++, type, bindingPositions);
	}

	/**
	 *
	 * @param name
	 * @param type
	 * @param bindingPositions
	 */
	public AccessMethod(String name, Types type, List<Integer> bindingPositions) {
		Preconditions.checkArgument(type == Types.FREE ? bindingPositions.isEmpty() : true);
		this.name = name;
		this.type = type;
		this.inputs = new ArrayList<>();
		if (bindingPositions != null) {
			for (Integer i : bindingPositions) {
				this.inputs.add(i);
			}
		}
	}

	/**
	 * @return the positions that are required inputs
	 */
	public List<Integer> getInputs() {
		return this.inputs;
	}

	/**
	 * @return the positions that are required inputs
	 */
	public List<Integer> getZeroBasedInputs() {
		List<Integer> zero = new ArrayList<>();
		for(Integer index:this.inputs) {
			zero.add(index-1);
		}
		return zero;
	}

	/**
	 * @return the type of the access restriction
	 */
	public Types getType() {
		return this.type;
	}

	/**
	 * @return the binding's name.
	 */
	public String getName() {
		return this.name;
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
				//				&& this.name.equals(((AccessMethod) o).name)
				&& this.type.equals(((AccessMethod) o).type)
				&& this.inputs.equals(((AccessMethod) o).inputs);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.type, this.inputs);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if (this.rep == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.name).append(':');
			result.append(this.type);
			if (this.type.equals(Types.LIMITED)) {
				char sep = '[';
				for (int i : this.inputs) {
					result.append(sep).append(i);
					sep = ',';
				}
				result.append(']');
			}
			this.rep = result.toString();
		}
		return this.rep;
	}
}
