// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Spliterator;
import java.util.stream.StreamSupport;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;

/**
 * An executable plan implementing the nested loop join algorithm. 
 * 
 * @author Tim Hobson
 */
public class NestedLoopJoin extends CartesianProduct {

	public NestedLoopJoin(Plan plan, PlanDecorator decorator) throws Exception {
		super(plan,decorator);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan instanceof JoinTerm);
	}

	@Override
	public Spliterator<Tuple> spliterator() {
		
		// Filter the Cartesian plan on the tuple-dependent joinCondition. 
		return StreamSupport.stream(super.spliterator(), false)
				.filter(tuple -> this.getJoinCondition().isSatisfied(tuple))
				.spliterator();
	}
}