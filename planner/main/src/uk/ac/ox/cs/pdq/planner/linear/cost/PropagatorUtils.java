package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.BlackBoxCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.linear.LeftDeepPlanGenerator;
import uk.ac.ox.cs.pdq.planner.linear.LinearUtility;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.util.IndexedDirectedGraph;

import com.google.common.base.Preconditions;


// TODO: Auto-generated Javadoc
/**
 * The Class PropagatorUtils.
 *
 * @author Efthymia Tsamoura
 */
public class PropagatorUtils {
	
	/**
	 * Gets the propagator.
	 *
	 * @param e the e
	 * @return the propagator
	 */
	public static CostPropagator getPropagator(CostEstimator<LeftDeepPlan> e) {
		if (e instanceof BlackBoxCostEstimator) {
			return new BlackBoxPropagator((BlackBoxCostEstimator) e);
		}
		if (e instanceof SimpleCostEstimator) {
			return new SimplePropagator((SimpleCostEstimator) e);
		}
		throw new IllegalStateException("Attempting to get a propagator for a cost estimator that is neither blackbox nor simple");
	}
	

	/**
	 * Creates the left deep plan.
	 *
	 * @param <T> the generic type
	 * @param nodesSet            The nodes of the plan tree
	 * @param path            A successful path (sequence of nodes). The corresponding nodes must
	 *            correspond to a successful path (a path from the root to a
	 *            success node)
	 * @param costEstimator CostEstimator<LeftDeepPlan>
	 * @return a linear plan that corresponds to the input path to success
	 */
	public static <T extends SearchNode> LeftDeepPlan createLeftDeepPlan(IndexedDirectedGraph<T> nodesSet, List<Integer> path, CostEstimator<LeftDeepPlan> costEstimator) {
		Preconditions.checkArgument(path != null && !path.isEmpty());
		List<T> nodes = LinearUtility.createPath(nodesSet, path);
		LeftDeepPlan plan = LeftDeepPlanGenerator.createLeftDeepPlan(nodes);
		costEstimator.cost(plan); 

		for (T next:nodes) {
			costEstimator.cost(next.getConfiguration().getPlan());
		}
		return plan;
	}
}