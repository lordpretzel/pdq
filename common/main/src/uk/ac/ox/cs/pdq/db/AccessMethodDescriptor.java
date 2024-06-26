// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.AccessMethodAdapter;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * An access method defines the positions of a relation's attributes whose
 * values are required to access the relation. An AccessMethodDescriptor cannot
 * be used for actual data access. It is a place holder that can be used for
 * reasoning/planing. In runtime these access method descriptors have to be
 * replaced by the executable access method.
 * 
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author Gabor Gyorkei
 * @author Stefano
 */
@XmlJavaTypeAdapter(AccessMethodAdapter.class)
public class AccessMethodDescriptor implements Serializable {

	protected static final long serialVersionUID = -5821292665848480210L;

	/** A Constant DEFAULT_PREFIX for all automatically generated access methods names */
	public static final String DEFAULT_PREFIX = "mt_";

	/**  Input attribute positions. */
	protected final Integer[] inputs;

	/**  Name of the access method. */
	protected final String name;

	/**  String representation of the object. */
	protected String toString = null;
	
	public AccessMethodDescriptor(Integer[] inputs) {
		this(DEFAULT_PREFIX + GlobalCounterProvider.getNext("AccessMethodName"), inputs);
	}

	protected AccessMethodDescriptor(String name, Integer[] inputs) {
		this.name = name;
		this.inputs = inputs.clone();
	}

	public Integer[] getInputs() {
		return this.inputs.clone();
	}
	
	public int getNumberOfInputs() {
		return this.inputs.length;
	}
	
	public int getInputPosition(int index) {
		assert (index >=0 && index < this.inputs.length);
		return this.inputs[index];
	}
	
	public String getName() {
		return this.name;
	}

	public static AccessMethodDescriptor create(String name, Integer[] inputs) {
		return Cache.accessMethod.retrieve(new AccessMethodDescriptor(name, inputs));
	}
	
	public static AccessMethodDescriptor create(Integer[] inputs) {
		return Cache.accessMethod.retrieve(new AccessMethodDescriptor(inputs));
	}

	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.name);
			if(this.inputs.length > 0) {
				result.append(':');
				char sep = '[';
				for (int i:this.inputs) {
					result.append(sep).append(i);
					sep = ',';
				}
				result.append(']');
			}
			this.toString = result.toString();
		}
		return this.toString;
	}

	/**
	 * Looks for constants in the input positions
	 * @param terms constants or variables
	 * @return the found position index and input constant pairs
	 */
	public Map<Integer, TypedConstant> computeInputConstants(Term[] terms) {
		Map<Integer, TypedConstant> ret = new HashMap<>();
		for (Integer i : this.getInputs()) {
			if (terms[i] instanceof TypedConstant)
				ret.put(i, (TypedConstant) terms[i]);
		}
		return ret;
	}
}
