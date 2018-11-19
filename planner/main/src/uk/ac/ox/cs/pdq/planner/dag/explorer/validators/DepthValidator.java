package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;

/**
 * Requires the input pair of configurations combined depth to be <= the depth threshold.
 *
 * @author Efthymia Tsamoura
 */
public class DepthValidator implements Validator{

	/** The depth threshold. */
	private final int depthThreshold;

	/**
	 * Constructor for DepthThrottlingValidator.
	 * @param depthThreshold int
	 */
	public DepthValidator(int depthThreshold) {
		this.depthThreshold = depthThreshold;
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return left.getHeight() <= this.depthThreshold;
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @param depth int
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration, int)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth) {
		return left.getHeight() + right.getHeight() == depth && this.validate(left, right);
	}

	/**
	 * Clone.
	 *
	 * @return Validator
	 * @see uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator#clone()
	 */
	@Override
	public Validator clone() {
		return new DepthValidator(this.depthThreshold);
	}

}
