package uk.ac.ox.cs.pdq.planner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class PlannerUtility.
 *
 * @author Efthymia Tsamoura
 */
public class PlannerUtility {

	/**
	 * Gets the input constants.
	 *
	 * @param rule the rule
	 * @param facts the facts
	 * @return the constants of the input facts that correspond to the input positions of the rule
	 */
	public static Collection<Constant> getInputConstants(AccessibilityAxiom rule, Set<Atom> facts) {
		Collection<Constant> inputs = new LinkedHashSet<>();
		for(Atom fact:facts) {
			List<Constant> constants = Utility.getTypedAndUntypedConstants(fact,rule.getAccessMethod().getZeroBasedInputs());
			for(Constant constant:constants) {
				if(constant.isUntypedConstant()) {
					inputs.add(constant);
				}
			}
		}
		return inputs;
	}

	/**
	 * Gets the input constants.
	 *
	 * @param accessMethod the binding
	 * @param fact the fact
	 * @return the constants in the input positions of the given fact
	 */
	public static List<Constant> getInputConstants(AccessMethod accessMethod, Atom fact) {
		List<Constant> ret  = Utility.getTypedAndUntypedConstants(fact,accessMethod.getZeroBasedInputs());
		return Lists.newArrayList(uk.ac.ox.cs.pdq.util.Utility.removeDuplicates(ret));
	}

	/**
	 * Gets the constants lying at the input positions.
	 *
	 * @throws IllegalArgumentException if there is a non-constant at one of the input positions
	 * @param positions List<Integer>
	 * @return the List<Constant> at the given positions.
	 */
	public static List<Constant> getTypedAndUntypedConstants(Atom atom, Integer[] positions) {
		List<Constant> result = new ArrayList<>();
		for(Integer i: positions) {
			if(i < atom.getTerms().length && !atom.getTerm(i).isVariable()) 
				result.add((Constant) atom.getTerm(i));
			else 
				throw new java.lang.IllegalArgumentException();
		}
		return result;
	}

	/**
	 * Accessible.
	 *
	 * @param <Q> the generic type
	 * @param query the query
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#createAccessibleQuery(AccessibleSchema)
	 */
	public static ConjunctiveQuery createAccessibleQuery(ConjunctiveQuery query) {
		Atom[] atoms = new Atom[query.getNumberOfAtoms()];
		for (int atomIndex = 0; atomIndex < query.getNumberOfAtoms(); ++atomIndex) {
			Atom queryAtom = query.getAtom(atomIndex);
			atoms[atomIndex] = Atom.create(Predicate.create(AccessibleSchema.inferredAccessiblePrefix + queryAtom.getPredicate().getName(), queryAtom.getNumberOfTerms()), queryAtom.getTerms());
		}
		if(atoms.length == 1) 
			return ConjunctiveQuery.create(query.getFreeVariables(), atoms[0]);
		else 
			return ConjunctiveQuery.create(query.getFreeVariables(), (Conjunction) Conjunction.of(atoms));
	}

	/**
	 * Accessible.
	 *
	 * @param <Q> the generic type
	 * @param query the query
	 * @param canonicalMapping the canonical mapping
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#createAccessibleQuery(AccessibleSchema)
	 */
	public static ConjunctiveQuery createAccessibleQuery(ConjunctiveQuery query, Map<Variable, Constant> canonicalMapping) {
		Atom[] atoms = new Atom[query.getNumberOfAtoms()];
		for (int atomIndex = 0; atomIndex < query.getNumberOfAtoms(); ++atomIndex) {
			Atom queryAtom = query.getAtom(atomIndex);
			atoms[atomIndex] = Atom.create(Predicate.create(AccessibleSchema.inferredAccessiblePrefix + queryAtom.getPredicate().getName(), queryAtom.getNumberOfTerms()), queryAtom.getTerms());
		}
		if(atoms.length == 1) 
			return ConjunctiveQuery.create(query.getFreeVariables(), atoms[0], canonicalMapping);
		else 
			return ConjunctiveQuery.create(query.getFreeVariables(), (Conjunction) Conjunction.of(atoms), canonicalMapping);
	}
}
