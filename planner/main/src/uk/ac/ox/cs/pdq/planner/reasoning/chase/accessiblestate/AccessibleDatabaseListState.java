package uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.reasoning.MatchFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 *
 * @author Efthymia Tsamoura
 *
 * 	Organises the facts during chasing into a list. 
 *	This type of state is used in terminating chase implementations.
 *	It also maintains the classes of equal chase constants that are derived after chasing with EGDs.
 *	This implementation does not store equality facts into the database, but when a class of equal constants is created
 *	the database facts are updated; update includes replacing every chase constant c, with a constant c' that is equal to c
 *	under the constraints and c' is a representative.
 *	The database is cleared from the obsolete facts after a chase step is applied.
 *
 */
public class AccessibleDatabaseListState extends uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState implements AccessibleChaseState {

	/** String signatures of the inferred accessible facts*/
	private final Collection<String> inferred;

	/** The inferred accessible facts that were derived when chasing this.state **/
	protected final Collection<Predicate> derivedInferred;

	/** Maps each schema signature (relation) to its chase facts*/
	private final Multimap<Signature, Predicate> signatureGroups;

	/** Maps each chase constant the Accessed facts it appears*/
	private final Multimap<Term,Predicate> accessibleTerms;

	/**
	 * 
	 * @param query
	 * @param manager
	 */
	public AccessibleDatabaseListState(Query<?> query, Schema schema, DBHomomorphismManager manager) {
		this(manager, 
				createInitialFacts(query, schema), 
				new MapFiringGraph(),
				inferEqualConstantsClasses(createInitialFacts(query, schema)),
				Utility.inferInferred(createInitialFacts(query, schema)),
				Utility.inferDerivedInferred(),
				Utility.inferSignatureGroups(createInitialFacts(query, schema)),
				Utility.inferAccessibleTerms(createInitialFacts(query, schema)));
		this.manager.addFacts(facts);
	}

	/**
	 * 
	 * @param query
	 * @param schema
	 * @return 
	 * 		the facts of the canonical query and the Accessible(.) facts of the schema constants
	 */
	private static Collection<Predicate> createInitialFacts(Query<?> query, Schema schema) {
		// Gets the canonical database of the query
		Collection<Predicate> facts = query.getCanonical().getPredicates();
		// Create the Accessible(.) facts
		// One Accessible(.) is being created for every schema constant
		for (TypedConstant<?> constant : query.getSchemaConstants()) {
			facts.add(AccessibleRelation.getAccessibleFact(constant));
		}
		for (TypedConstant<?> constant:schema.getDependencyConstants()) {
			facts.add(AccessibleRelation.getAccessibleFact(constant));
		}
		return facts;
	}


	/**
	 * 
	 * @param accessibleQuery
	 * @param manager
	 * @param facts
	 * @param graph
	 * @param inferred
	 * @param derivedInferred
	 * @param signatureGroups
	 * @param accessibleTerms
	 */
	private AccessibleDatabaseListState(
			DBHomomorphismManager manager,
			Collection<Predicate> facts,
			FiringGraph graph,
			EqualConstantsClasses constantClasses,
			Collection<String> inferred,
			Collection<Predicate> derivedInferred,
			Multimap<Signature, Predicate> signatureGroups,
			Multimap<Term,Predicate> accessibleTerms
			) {
		super(manager, facts, graph, constantClasses);
		this.inferred = inferred;
		this.derivedInferred = derivedInferred;
		this.signatureGroups = signatureGroups;
		this.accessibleTerms = accessibleTerms;
	}

	/**
	 * @return Collection<String>
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#getInferred()
	 */
	@Override
	public Collection<String> getInferred() {
		return this.inferred;
	}

	/**
	 * @return Collection<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#getDerivedInferred()
	 */
	@Override
	public Collection<Predicate> getDerivedInferred() {
		return this.derivedInferred;
	}

	/**
	 * @return Multimap<Signature,PredicateFormula>
	 */
	protected Multimap<Signature, Predicate> getSignatureGroups() {
		return this.signatureGroups;
	}

	/**
	 * @return Multimap<Term,PredicateFormula>
	 */
	protected Multimap<Term,Predicate> getAccessibleTerms() {
		return this.accessibleTerms;
	}

	/**
	 * Updates this.state
	 * 		
	 */
	@Override
	public boolean chaseStep(Match match) {
		return this.chaseStep(Sets.newHashSet(match));
	}

	@Override
	public boolean chaseStep(Collection<Match> matches) {
		super.chaseStep(matches);
		for(Match match:matches) {
			//The dependency to fire
			Constraint dependency = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			//The grounded left-hand side of the input dependency
			Constraint grounded = dependency.fire(mapping, this.canonicalNames);
			//The grounded right-hand side of the input dependency
			Formula right = grounded.getRight();
			for(Predicate fact:right.getPredicates()) {
				if(fact.getSignature() instanceof InferredAccessibleRelation) {
					this.derivedInferred.add(fact);
				}
				if (!(fact.getSignature() instanceof AccessibleRelation) && 
						!(fact.getSignature() instanceof InferredAccessibleRelation)) {
					this.signatureGroups.put(fact.getSignature(), fact);
				}
				if (fact.getSignature() instanceof AccessibleRelation) {
					this.accessibleTerms.put(fact.getTerm(0), fact);
				}
				if (fact.getSignature() instanceof InferredAccessibleRelation) {
					this.inferred.add(fact.toString());
				}
			}
		}
		return true;
	}

	/**
	 * For each input accessibility axiom, it
	 * groups the corresponding state facts based on the chase constants assigned to their input positions.
	 * This function is called when creating the initial ApplyRule configurations
	 * @param axioms
	 *
	 * @return
	 * 		pairs of accessibility axioms to chase facts
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#groupByBinding(Collection<AccessibilityAxiom>)
	 */
	@Override
	public List<Pair<AccessibilityAxiom, Collection<Predicate>>> groupByBinding(Collection<AccessibilityAxiom> axioms) {
		return new Utility().groupByBinding(axioms, this.getSignatureGroups());
	}

	/**
	 *
	 * @param accessibleSchema
	 * @param axioms
	 * @return
	 * 		the unexposed facts and information to expose them
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getUnExposedFacts(AccessibleSchema, Collection<AccessibilityAxiom>)
	 */
	@Override
	public Map<AccessibilityAxiom, List<Match>> getUnexposedFacts(AccessibleSchema accessibleSchema) {
		return this.getUnexposedFacts(accessibleSchema, this.getSignatureGroups(), this.getAccessibleTerms(), this.getFiringGraph());
	}

	@Override
	public void generate(AccessibleSchema schema, AccessibilityAxiom axiom, Collection<Predicate> facts) {
		Collection<Predicate> generatedFacts = new Utility().generateFacts(schema, axiom, facts, this.getInferred(), this.getDerivedInferred(), this.getFiringGraph());
		this.addFacts(generatedFacts);
	}


	/**
	 *
	 * @param accessibleSchema
	 * @param axioms
	 * @param signatureGroups
	 * 		Maps each schema signature (relation) to its chase facts
	 * @param accessibleTerms
	 * 		Maps each chase constant the Accessed facts it appears
	 * @return
	 * 		the non-fired accessibility axioms of the state and information to fire them
	 */
	private Map<AccessibilityAxiom, List<Match>> getUnexposedFacts(
			AccessibleSchema accessibleSchema,
			Multimap<Signature, Predicate> signatureGroups,
			Multimap<Term,Predicate> accessibleTerms,
			FiringGraph graph) {
		Map<AccessibilityAxiom, List<Match>> ret = new LinkedHashMap<>();
		List<Pair<AccessibilityAxiom,Collection<Predicate>>> groups = 
				new Utility().groupByBinding(accessibleSchema.getAccessibilityAxioms(), signatureGroups);
		for(Pair<AccessibilityAxiom, Collection<Predicate>> pair: groups) {
			AccessibilityAxiom axiom = pair.getLeft();
			Iterator<Predicate> iterator = pair.getRight().iterator();
			while (iterator.hasNext()) {
				Predicate fact = iterator.next();
				Predicate accessedFact = new Predicate(accessibleSchema.getInferredAccessibleRelation((Relation) fact.getSignature()), fact.getTerms());
				Collection<Term> inputTerms = accessedFact.getTerms(axiom.getAccessMethod().getZeroBasedInputs());
				if(graph.getFactProvenance(accessedFact) == null && accessibleTerms.keySet().containsAll(inputTerms)) {
					Match matching = MatchFactory.getMatch(pair.getLeft(), fact);
					List<Match> matchings = ret.get(pair.getLeft());
					if(matchings == null) {
						ret.put(axiom, Lists.newArrayList(matching));
					}
					else {
						matchings.add(matching);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * @return Map<PredicateFormula,Pair<Constraint,Collection<PredicateFormula>>>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getProvenance()
	 */
	@Override
	public Map<Predicate, Pair<Constraint, Collection<Predicate>>> getProvenance() {
		return this.getFiringGraph().getFactProvenance();
	}


	/**
	 * @param fact PredicateFormula
	 * @return Pair<Constraint,Collection<PredicateFormula>>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getProvenance(Predicate)
	 */
	@Override
	public Pair<Constraint, Collection<Predicate>> getProvenance(Predicate fact) {
		return this.getFiringGraph().getFactProvenance(fact);
	}

	/**
	 * @return DatabaseListState
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#clone()
	 */
	@Override
	public AccessibleDatabaseListState clone() {
		return new AccessibleDatabaseListState(this.manager, Sets.newHashSet(this.facts), 
				this.graph.clone(),
				this.constantClasses.clone(),
				new LinkedHashSet<>(this.inferred),
				new LinkedHashSet<>(this.derivedInferred), 
				LinkedHashMultimap.create(this.signatureGroups), 
				LinkedHashMultimap.create(this.accessibleTerms));
	}

	@Override
	public AccessibleChaseState merge(AccessibleChaseState s) {
		Preconditions.checkState(s instanceof AccessibleDatabaseListState);
		Collection<Predicate> facts =  new LinkedHashSet<>(this.facts);
		facts.addAll(s.getFacts());
		
		Collection<String> inferred = CollectionUtils.union(this.inferred, ((AccessibleDatabaseListState)s).inferred);
		Collection<Predicate> derivedInferred = CollectionUtils.union(this.derivedInferred, ((AccessibleDatabaseListState)s).derivedInferred);
		Multimap<Signature, Predicate> signatureGroups = LinkedHashMultimap.create(this.signatureGroups);
		signatureGroups.putAll(((AccessibleDatabaseListState)s).signatureGroups);
		Multimap<Term,Predicate> accessibleTerms = LinkedHashMultimap.create(this.accessibleTerms);
		accessibleTerms.putAll(((AccessibleDatabaseListState)s).accessibleTerms);
		
		EqualConstantsClasses classes = this.constantClasses.clone();
		if(!classes.merge(((DatabaseListState)s).getConstantClasses())) {
			return null;
		}
		return new AccessibleDatabaseListState(
				this.getManager(),
				facts, 
				this.getFiringGraph().merge(s.getFiringGraph()),
				classes,
				inferred,
				derivedInferred, 
				signatureGroups, 
				accessibleTerms);
	}
}
