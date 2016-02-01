package uk.ac.ox.cs.pdq.planner.dag.cost;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

/**
 * @deprecated
 * @author Efthymia Tsamoura
 *
 */
public class ConfigurationCostEstimator implements CostEstimator<Configuration> {

	private final CostEstimator<Plan> estimator;
	
	public ConfigurationCostEstimator(CostEstimator<Plan> ce) {
		this.estimator = ce;
	}
	
	@Override
	public Cost cost(Configuration config) {
		return estimator.cost(config.getPlan());
	}

	@Override
	public Cost estimateCost(Configuration config) {
		return estimator.estimateCost(config.getPlan());
	}

	@Override
	public CostEstimator<Configuration> clone() {
		return new ConfigurationCostEstimator(this.estimator);
	}
}