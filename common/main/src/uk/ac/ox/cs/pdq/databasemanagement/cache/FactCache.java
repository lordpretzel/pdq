package uk.ac.ox.cs.pdq.databasemanagement.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Memory storage for a set of facts. The set can be named with an InstanceID.
 * 
 * @author Gabor
 *
 */
public class FactCache {
	/**
	 * Optional name for this set.
	 */
	private int databaseInstanceID;
	/**
	 * The actual data, grouped by relation names.
	 */
	private Map<String,Collection<Atom>> cache;
	/**
	 * Lock object for synchronising access to the data.
	 */
	private Object LOCK = new Object();

	/**
	 * Creates a new cache.
	 * 
	 * @param databaseInstanceID
	 *            - optional name.
	 */
	public FactCache(int databaseInstanceID) {
		this.databaseInstanceID = databaseInstanceID;
		cache = new HashMap<>();
	}

	/**
	 * Stores facts in the cache.
	 * 
	 * @param toAdd
	 */
	public Collection<Atom> addFacts(Collection<Atom> toAdd) {
		Collection<Atom> results = new ArrayList<>();
		synchronized (LOCK) {
			for (Atom a:toAdd) {
				if (cache.containsKey(a.getPredicate().getName())) {
					if (cache.get(a.getPredicate().getName()).add(a)) {
						results.add(a);
					}
				} else {
					List<Atom> predicateFacts = new ArrayList<>();
					predicateFacts.add(a);
					// we know that the fact is new since we had to create the list for this predicate.
					results.add(a);
					cache.put(a.getPredicate().getName(), predicateFacts);
				}
			}
		}
		return results;
	}

	/**
	 * Recalls facts from the cache.
	 * 
	 * @return
	 */
	public Collection<Atom> getFacts() {
		ArrayList<Atom> result = new ArrayList<>();
		synchronized (LOCK) {
			for (String key:cache.keySet()) {
				result.addAll(cache.get(key));
			}
		}
		return result;
	}
	
	/** Gets all facts of a certain relation. 
	 * @param relationName
	 * @return
	 */
	public List<Atom> getFactsOfRelation(String relationName) {
		ArrayList<Atom> result = new ArrayList<>();
		synchronized (LOCK) {
			if (cache.containsKey(relationName)) {
				result.addAll(cache.get(relationName));
			}
		}
		return result;
	}

	/**
	 * @return optional name of this cache, could be null.
	 */
	public int getDatabaseInstanceID() {
		return databaseInstanceID;
	}

	/**
	 * @param facts
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
	 */
	public boolean removeFacts(Collection<Atom> facts) {
		synchronized (LOCK) {
			boolean ret = false;
			for (Atom fact: facts) {
				Collection<Atom> listOfFacts = cache.get(fact.getPredicate().getName());
				if (listOfFacts!=null) {
					if (listOfFacts.remove(fact)) {
						ret = true;
					}
				}
			}
			return ret;
		}
	}
	/**
	 *  Clears the cache.
	 */
	public void clearCache() {
		synchronized (LOCK) {
			cache.clear();
		}
	}
}
