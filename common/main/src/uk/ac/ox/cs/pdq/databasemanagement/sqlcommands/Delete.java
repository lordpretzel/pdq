package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Represents a DELETE FROM sql statement, can be used to delete a single fact.
 * 
 * @author Gabor
 *
 */
public class Delete extends Command {
	public final String SEMICOLON = "{SEMICOLON}";

	/**
	 * Constructor.
	 * 
	 * @param factToDelete
	 *            fact to delete
	 * @param schema
	 *            - schema is needed to get the relation (the fact contains a
	 *            predicate only that might not be a relation)
	 * @throws DatabaseException in case the input fact contains some error. For example it is not connected to the given schema by having an unknown predicate.
	 */
	public Delete(Atom factToDelete, Schema schema) throws DatabaseException {
		super();
		// add dialect specific mappings.
		replaceTagsDerby.put(SEMICOLON, ""); // derby doesn't like the semicolon at the end of the line.
		replaceTagsMySql.put(SEMICOLON, ";");
		replaceTagsPostgres.put(SEMICOLON, ";");

		// add the actual DELETE FROM statement.
		statements.add(createDeleteStatement(factToDelete, schema) + SEMICOLON);
	}

	/**
	 * Creates a delete form statement string
	 * 
	 * @return
	 * @throws DatabaseException 
	 */
	protected String createDeleteStatement(Atom fact, Schema schema) throws DatabaseException {
		// header
		String deleteFrom = "DELETE FROM " + DATABASENAME + "." + fact.getPredicate().getName() + " WHERE ";
		Relation r = schema.getRelation(fact.getPredicate().getName());
		int index = 0;

		// condition
		for (Attribute a : r.getAttributes()) {
			if (index != 0)
				deleteFrom += " AND ";
			deleteFrom += a.getName() + " = " + convertTermToSQLString(a, fact.getTerm(index)) + " ";
			index++;
		}
		deleteFrom += "\n";
		return deleteFrom;
	}
}
