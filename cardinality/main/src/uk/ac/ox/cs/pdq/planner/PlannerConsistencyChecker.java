package uk.ac.ox.cs.pdq.planner;

import uk.ac.ox.cs.pdq.ConsistencyChecker;
import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

/**
 * Check high-level planner parameters consistency.
 *
 * @author Julien Leblay
 */
public class PlannerConsistencyChecker implements ConsistencyChecker<PlannerParameters, CostParameters, ReasoningParameters> {

	/**
	 * @param p PlannerParameters
	 * @param c CostParameters
	 * @throws InconsistentParametersException
	 * @see uk.ac.ox.cs.pdq.ConsistencyChecker#check(PlannerParameters)
	 */
	@Override
	public void check(PlannerParameters p, CostParameters c, ReasoningParameters r) throws InconsistentParametersException {
	}
}
