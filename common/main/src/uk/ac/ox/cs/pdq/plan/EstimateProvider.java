package uk.ac.ox.cs.pdq.plan;

import uk.ac.ox.cs.pdq.algebra.Operator;


// TODO: Auto-generated Javadoc
/**
 * Interface providing cardinality estimations for plan operators.
 *
 * @author Julien Leblay
 * @param <P> the generic type
 */
 //Efi: This class should be moved to the cost package 
public interface EstimateProvider<P extends Operator> {

	/**
	 * Gets the parent.
	 *
	 * @return LogicalOperator
	 */
	P getParent();
	
	/**
	 * Sets the parent.
	 *
	 * @param parent LogicalOperator
	 */
	void setParent(P parent);
	
	/**
	 * Gets the input cardinality.
	 *
	 * @return Double
	 */
	Double getInputCardinality();
	
	/**
	 * Gets the output cardinality.
	 *
	 * @return Double
	 */
	Double getOutputCardinality();
	
	/**
	 * Sets the input cardinality.
	 *
	 * @param l Double
	 */
	void setInputCardinality(Double l);
	
	/**
	 * Sets the output cardinality.
	 *
	 * @param l Double
	 */
	void setOutputCardinality(Double l);
}
