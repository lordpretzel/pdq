// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.reasoning;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;

/**
 * A factory for creating Match objects.
 *
 * @author Efthymia Tsamoura
 */
public class MatchFactory {

	/**
	 *
	 * @param axiom the axiom
	 * @param fact the fact
	 * @return 		a match given the input accessibility axiom and the input fact
	 */
	public static Match createMatchForAccessibilityAxiom(AccessibilityAxiom axiom, Atom fact) {
		Map<Variable, Constant> map = new LinkedHashMap<>();
		for(int termIndex = 0; termIndex < axiom.getGuard().getNumberOfTerms(); ++termIndex) {
			Term term = axiom.getGuard().getTerm(termIndex);
			if(term instanceof Variable && fact.getTerm(termIndex) instanceof Constant) 
				map.put((Variable)term, (Constant) fact.getTerm(termIndex));
			else 
				throw new java.lang.RuntimeException("Cannot map constants to constants or variables to variables, but only variables to constants");
		}
		return Match.create(axiom, map);
	}
}
