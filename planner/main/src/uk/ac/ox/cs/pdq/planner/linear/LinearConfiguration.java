package uk.ac.ox.cs.pdq.planner.linear;

import java.util.Collection;
import java.util.Set;

import uk.ac.ox.cs.pdq.plan.LinearPlan;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

/**
 * A linear configuration
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface LinearConfiguration extends Configuration<LinearPlan> {

	/**
	 * @return the candidates to expose of this configuration
	 */
	Collection<Candidate> getCandidates();

	/**
	 * @return the exposed candidates of this configuration
	 */
	Collection<Candidate> getExposedCandidates();

	/**
	 * @return a randomly chosen candidate of this configuration
	 */
	Candidate chooseCandidate();

	/**
	 * @return true if the configuration has candidates
	 */
	Boolean hasCandidates();

	/**
	 * Remove the given candidates
	 * @param candidates Set<Candidate>
	 */
	void removeCandidates(Set<Candidate> candidates);

	/**
	 * @param candidate
	 * @return a list of candidates sharing the same constants in their input positions with this configuration
	 */
	Set<Candidate> getSimilarCandidates(Candidate candidate);
	
//	LinearPlan createPlan(LinearPlan parentPlan);
	
	@Override
	LinearConfiguration clone();

}
