package uk.ac.ox.cs.pdq.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates constants for existentially quantified variables.
 * It is called when a dependency is grounded
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */

public final class CanonicalNameGenerator {

	/** The Constant DEFAULT_PREFIX. */
	private static final String DEFAULT_PREFIX = "c";
	
	/** The Constant CANONICAL_PREFIX. */
	private static final String CANONICAL_PREFIX = "k";

	/** The global id. */
	private static int globalId = 0;

	/** Index storing all canonical name stored so far (value) and the canonical string there where generated from (key). */
	private static Map<String, String> skolems = new LinkedHashMap<>();

	/**
	 * Gets the name.
	 *
	 * @param dependency 		The input dependency
	 * @param universalVariables 		A string of universal variable-universal variable grounding pairs for all universally quantified variable
	 * @param existentialVariable 		The existential variable
	 * @return 		a canonical name which equals to the dependency name + the assignment
	 * 		of the canonical names to universal variables + the existential's variable name
	 */
	public static String getName(String dependency, String universalVariables, String existentialVariable) {
		String key = dependency + existentialVariable + universalVariables;
		String result = skolems.get(key);
		if (result == null) {
			result = getName(CANONICAL_PREFIX);
			skolems.put(key, result);
		}
		return result;
	}

	/**
	 * Gets the name.
	 *
	 * @param prefix the prefix
	 * @return a fresh constant name with the given prefix.
	 */
	private static String getName(String prefix) {
		return prefix + (++globalId);
	}

	/**
	 * Gets the name.
	 *
	 * @return a fresh constant name with the default prefix.
	 */
	public static String getName() {
		return getName(DEFAULT_PREFIX);
	}
}