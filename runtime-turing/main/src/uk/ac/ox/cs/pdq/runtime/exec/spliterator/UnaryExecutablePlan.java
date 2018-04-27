package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Iterator;

import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * Base class for executable plans having a single child.
 * @author Tim Hobson
 *
 */
public abstract class UnaryExecutablePlan extends ExecutablePlan {

	protected ExecutablePlan child;

	public UnaryExecutablePlan(Plan plan) {
		super(plan);
	}
	
	@Override
	public void close() {
		this.child.close();
	}

	// Base setInputTuples method simply delegates to the child.
	// Note that we cannot validate the input tuples against the
	// input attributes since inputTuples is an iterator.
	@Override
	public void setInputTuples(Iterator<Tuple> inputTuples) {
		this.child.setInputTuples(inputTuples);
	}
}