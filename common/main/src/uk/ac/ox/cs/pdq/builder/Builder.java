package uk.ac.ox.cs.pdq.builder;

// TODO: Auto-generated Javadoc
/**
 * Interface common to builder class.
 * Builder are typically use to instantiate object that are too complex to
 * initialise with a single constructor calls, e.g. if many fields are
 * mandatory and many consistency checks are required.
 * The builder class works as a proxy, receiving all necessary initialisation
 * on behalf to the object to be created.
 * The final object is actually instantiated upon a call to the build() method.
 *
 * @author Julien Leblay
 *
 * @param <T> the type of objects built
 */
public interface Builder<T> {

	/**
	 * Builds the.
	 *
	 * @return a newly instantiated object.
	 */
	T build();
}
