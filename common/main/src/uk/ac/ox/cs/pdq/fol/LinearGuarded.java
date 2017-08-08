package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class LinearGuarded extends TGD {

	private static final long serialVersionUID = -6527657738786432098L;

	private LinearGuarded(Formula body, Formula head) {
		super(body,head);
		Assert.assertTrue(body.getAtoms().length == 1);
	}
	
	/**
	 * Constructs a guarded dependency based on the input key-foreign key
	 * dependency.
	 *
	 * @param relation the relation
	 * @param foreignKey One of the foreign keys of this relation
	 */
	public LinearGuarded(Relation relation, ForeignKey foreignKey) {
		super(createBody(relation), createHead(relation, foreignKey));
	}

	/**
	 *
	 * @param relation Relation
	 * @return the body formula of a linear guarded dependency for the given relation
	 */
	private static Formula createBody(Relation relation) {
		Variable[] free = new Variable[relation.getArity()];
		int index = 0;
		for (int variableIndex = 0; variableIndex < relation.getArity(); variableIndex++) 
			free[variableIndex] = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
		return Atom.create(relation, free);
	}

	/**
	 *
	 * @param relation the relation
	 * @param foreignKey the foreign key
	 * @return the head formula of a linear guarded dependency for the given relation and foreign key constraint
	 */
	private static Formula createHead(Relation relation, ForeignKey foreignKey) {
		Variable[] free = new Variable[relation.getArity()];
		int index = 0;
		for (int variableIndex = 0; variableIndex < relation.getArity(); variableIndex++) 
			free[variableIndex] = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));

		Variable[] remoteTerms = new Variable[foreignKey.getForeignRelation().getArity()];
		for (int variableIndex = 0; variableIndex < foreignKey.getForeignRelation().getArity(); variableIndex++) 
			remoteTerms[variableIndex] = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
		
		Reference[] references = foreignKey.getReferences();
		for (Reference rf:references) {
			int remoteTermIndex = foreignKey.getForeignRelation().getAttributePosition(rf.getForeignAttributeName());
			int localTermIndex = relation.getAttributePosition(rf.getLocalAttributeName());
			remoteTerms[remoteTermIndex] = free[localTermIndex];
		}
		return Atom.create(foreignKey.getForeignRelation(), remoteTerms);
	}

	/**
	 * Gets the guard atom.
	 *
	 */
	public Atom getGuard() {
		return this.getBodyAtom(0);
	}
	
	@Override
	public boolean isGuarded() {
		return true;
	}
    
    public static LinearGuarded create(Formula head, Formula body) {
        return Cache.linearGuarded.retrieve(new LinearGuarded(head, body));
    }
}
