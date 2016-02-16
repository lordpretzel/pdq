package uk.ac.ox.cs.pdq.util;


// TODO: Auto-generated Javadoc
/**
 * An iterator that can be reset, i.e. the cursor can be placed back to the
 * beginning of the underlying Iterable at any time.
 *
 * @author Julien Leblay
 * @param <I> the generic type
 * @param <O> the generic type
 */
public interface ResetableTranslatingIterator<I, O> extends TranslatingIterator<I, O>, ResetableIterator<O> {

	/** Opens the iterator to its initial position.
	 * @see uk.ac.ox.cs.pdq.util.ResetableIterator#open()
	 */
	@Override
	void open();

	/** Resets the iterator to its initial position.
	 * @see uk.ac.ox.cs.pdq.util.ResetableIterator#reset()
	 */
	@Override
	void reset();

	/**
	 * Deep copy.
	 *
	 * @return a copy of the iterator.
	 * @see uk.ac.ox.cs.pdq.util.TranslatingIterator#deepCopy()
	 */
	@Override
	ResetableTranslatingIterator<I, O> deepCopy();

}
