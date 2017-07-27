package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.IterativeExecutorTypes;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

// TODO: Auto-generated Javadoc
/**
 * Creates IterativeExecutor objects.
 * An IterativeExecutor object provides methods to create binary configurations or to
 * identify the minimum-cost configuration among a given set of configurations
 *
 * @author Efthymia Tsamoura
 */
public class IterativeExecutorFactory {

	/**
	 * Creates a new IterativeExecutor object.
	 *
	 * @param executorType the executor type
	 * @param parallelThreads the parallel threads
	 * @param chaser 		Runs the chase algorithm
	 * @param dbConn 		Detects homomorphisms during chasing
	 * @param estimator 		Estimates the cost of a plan
	 * @param successDominance 		Removes success dominated configurations
	 * @param dominance the dominance
	 * @param validators 		Validates pairs of configurations to be composed
	 * @param reasoningParameters 
	 * @return the iterative executor
	 * @throws Exception the exception
	 */
	public static IterativeExecutor createIterativeExecutor (
			IterativeExecutorTypes executorType,
			int parallelThreads,
			Chaser chaser,
			DatabaseConnection dbConn,
			CostEstimator estimator,
			SuccessDominance successDominance,
			Dominance[] dominance,
			List<Validator> validators, ReasoningParameters reasoningParameters) throws Exception{
		switch(executorType) {
		case MULTITHREADED:
			MultiThreadedContext mtcontext = new MultiThreadedContext(parallelThreads,
					chaser,
					dbConn,
					estimator,
					successDominance,
					dominance,
					validators, 
					reasoningParameters);
			return new MultiThreadedExecutor(mtcontext);
		default:
			throw new java.lang.IllegalArgumentException();
		}
	}
}
