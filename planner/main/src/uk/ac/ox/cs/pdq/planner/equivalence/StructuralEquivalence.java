package uk.ac.ox.cs.pdq.planner.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

/**
 * Structural equivalence.
 * Two configurations c and c' are structurally equivalent if there exists an homomorphism from the facts of c to the facts of c' and vice-versa.
 *
 * @author Efthymia Tsamoura
 */
public interface StructuralEquivalence extends Equivalence<ChaseConfiguration>  {

	/**
	 * @return StructuralEquivalence
	 */
	StructuralEquivalence clone();
}