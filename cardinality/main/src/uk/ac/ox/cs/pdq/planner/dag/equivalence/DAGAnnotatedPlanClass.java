/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Collection;

import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;

// TODO: Auto-generated Javadoc
/**
 * Class of structurally equivalent configurations.
 *
 * @author Efthymia Tsamoura
 */
public abstract class DAGAnnotatedPlanClass {

	/**  The representative of this class. */
	protected DAGAnnotatedPlan representative;

	/**  The minimum depth configuration of this class. */
	protected Integer minHeight;

	/**
	 * Adds the entry.
	 *
	 * @param configuration Adds the input configuration to the class
	 */
	public abstract void addEntry(DAGAnnotatedPlan configuration);

	/**
	 * Removes the input configuration from the class.
	 *
	 * @param configuration the configuration
	 */
	public abstract void removeEntry(DAGAnnotatedPlan configuration);

	/**
	 * Gets the all.
	 *
	 * @return the configurations of the class
	 */
	public abstract Collection<DAGAnnotatedPlan> getAll();

	/**
	 * Removes all input configurations.
	 *
	 * @param configurations the configurations
	 */
	public abstract void removeAll(Collection<DAGAnnotatedPlan> configurations);

	/**
	 * Dominated by.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return the class configurations that are dominated by the input configuration
	 */
	public abstract Collection<DAGAnnotatedPlan> dominatedBy(Dominance[] dominance, DAGAnnotatedPlan configuration);


	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration the configuration
	 * @return true if the configuration is structurally equivalent to the configurations of this class
	 */
	public abstract boolean structurallyEquivalentTo(DAGAnnotatedPlan configuration);

	/**
	 * Dominate.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return the configurations that dominate the input configuration
	 */
	public abstract DAGAnnotatedPlan dominate(Dominance[] dominance, DAGAnnotatedPlan configuration);

	/**
	 * Checks if is empty.
	 *
	 * @return true if the class is empty
	 */
	public abstract boolean isEmpty();

	/**
	 * Size.
	 *
	 * @return the size of the class
	 */
	public abstract int size();

	/**
	 * Gets the representative.
	 *
	 * @return DAGChaseConfiguration
	 */
	public DAGAnnotatedPlan getRepresentative() {
		return this.representative;
	}

	/**
	 * Gets the min height.
	 *
	 * @return Integer
	 */
	public Integer getMinHeight() {
		return this.minHeight;
	}
}