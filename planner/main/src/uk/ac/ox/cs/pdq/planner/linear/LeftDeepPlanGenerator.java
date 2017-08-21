package uk.ac.ox.cs.pdq.planner.linear;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;

// TODO: Auto-generated Javadoc
/**
 * Transforms linear chase configurations to left-deep plans. 
 *
 * @author Efthymia Tsamoura
 */
public class LeftDeepPlanGenerator {

	//TODO comment
	/**
	 * Creates a linear plan using the subplans of the input sequence of nodes.
	 *
	 * @param <T> the generic type
	 * @param nodes List<T>
	 * @return the left deep plan
	 */
	public static<T extends SearchNode> RelationalTerm createLeftDeepPlan(List<T> nodes) {
		RelationalTerm parentPlan = null;
		for (T node: nodes) 
			parentPlan = LeftDeepPlanGenerator.createLeftDeepPlan(node.getConfiguration().getRule().getBaseRelation(), node.getConfiguration().getRule().getAccessMethod(), node.getConfiguration().getFacts(), parentPlan);
		return parentPlan;
	}
	
	/**
	 * Creates a linear plan by appending the access and middlewares commands of the input configuration to the input parent plan.
	 * The top level operator is a projection that projects the terms toProject.
	 * 
	 * The newly created access and middleware command are created as follows:
	 * For an exposed fact f, If f has been exposed by an input-free accessibility axiom (access method), 
	 * then create an input-free access else create a dependent access operator.
	 * If f has schema constants in output positions or repeated constants, then these schema constants map to filtering predicates.
	 * Finally, project the variables that correspond to output chase constants. 
	 *
	 * @param configuration the c
	 * @param parent 		The input parent plan. This is the plan of the parent configuration of c, i.e., the configuration that is augmented with the exposed facts of c.
	 * @param toProject 		Terms to project in the resulting plan
	 * @return the left deep plan
	 */
	public static RelationalTerm createLeftDeepPlan(Relation relation, AccessMethod accessMethod, Set<Atom> exposedFacts, RelationalTerm parent) {
		RelationalTerm op1 = PlanCreationUtility.createSingleAccessPlan(relation, accessMethod, exposedFacts);
		if (parent != null) {
			//Check if the parent outputs any of the inputs of its child
			Set<Attribute> outputs = new HashSet<Attribute>(Arrays.asList(parent.getOutputAttributes()));
			Set<Attribute> inputs = new HashSet<Attribute>(Arrays.asList(op1.getInputAttributes()));
			if (accessMethod.getNumberOfInputs() > 0 && CollectionUtils.containsAny(outputs, inputs)) 
				op1 = DependentJoinTerm.create(parent, op1);
			else 
				op1 = JoinTerm.create(parent, op1);
		}
		return op1;
	}

}
