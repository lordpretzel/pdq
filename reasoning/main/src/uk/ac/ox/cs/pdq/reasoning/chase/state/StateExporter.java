package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.db.Instance;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Imports or exports a chase state. Normally we export the final state of the
 * chase, in order to run further queries on it later.
 * 
 * @author gabor
 */
public class StateExporter {

	private Instance instance;

	public StateExporter(Instance instance) {
		Preconditions.checkNotNull(instance);
		this.instance = instance;
	}

	/**
	 * Exports the current chase state to a folder.
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public void exportTo(File directory) throws IOException {
		Preconditions.checkArgument(directory.exists());
		Preconditions.checkArgument(directory.isDirectory());

		Map<String, Collection<Atom>> factsPerPredicate = new HashMap<>();
		Collection<Atom> facts = instance.getFacts();
		for (Atom a : facts) {
			if (factsPerPredicate.containsKey(a.getPredicate().getName())) {
				factsPerPredicate.get(a.getPredicate().getName()).add(a);
			} else {
				Collection<Atom> list = new ArrayList<>();
				list.add(a);
				factsPerPredicate.put(a.getPredicate().getName(), list);
			}
		}

		for (String predicateName : factsPerPredicate.keySet()) {
			DbIOManager.exportFacts(predicateName, directory, factsPerPredicate.get(predicateName));
		}
	}

	/**
	 * Reads csv files representing a chase state and loads them into the instance.
	 * 
	 * @param directory
	 * @param schema
	 */
	public void importFrom(File directory, Schema schema) {
		Preconditions.checkArgument(directory.exists());
		Preconditions.checkArgument(directory.isDirectory());

		for (Relation r : schema.getRelations()) {
			File csvFile = new File(directory, r.getName()+".csv");
			if (csvFile.exists()) {
				Collection<Atom> facts = DbIOManager.importFacts(r, csvFile);
				System.out.println("Imported " + facts.size() + " facts fpr relation " + r.getName());
				instance.addFacts(facts);
			} else {
				System.out.println("No data found for relation " + r.getName());
			}
		}
	}
}