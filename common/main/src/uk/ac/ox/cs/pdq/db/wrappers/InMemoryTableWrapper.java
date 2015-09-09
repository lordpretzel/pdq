package uk.ac.ox.cs.pdq.db.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.metadata.StaticMetadata;
import uk.ac.ox.cs.pdq.util.Pipelineable;
import uk.ac.ox.cs.pdq.util.ResetableIterator;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Preconditions;

/**
 * In memory relation wrapper. This is the default implementation of a relation,
 * where the data associated with a relation resides in memory, and does not
 * rely on any external support.
 * 
 * @author Julien Leblay
 */
public class InMemoryTableWrapper extends Relation
		implements Pipelineable, RelationAccessWrapper, InMemoryRelation {

	/** */
	private static final long serialVersionUID = -3167783211904676965L;

	/** The underlying data */
	private Collection<Tuple> data = new ArrayList<>();
	
	/**
	 * @param relation Relation
	 */
	public InMemoryTableWrapper(Relation relation) {
		this(relation.getName(), relation.getAttributes(),
				relation.getAccessMethods(), relation.isEquality());
	}
	
	/**
	 * @param name String
	 * @param attributes List<Attribute>
	 * @param bm List<AccessMethod>
	 */
	public InMemoryTableWrapper(String name, List<Attribute> attributes,
			List<AccessMethod> bm, boolean isEquality) {
		super(name, attributes, bm, isEquality);
	}
	
	/**
	 * @param name String
	 * @param attributes List<Attribute>
	 * @param bm List<AccessMethod>
	 */
	public InMemoryTableWrapper(String name, List<Attribute> attributes,
			List<AccessMethod> bm) {
		this(name, attributes, bm, false);
	}
	
	/**
	 * @param name String
	 * @param attributes List<Attribute>
	 */
	public InMemoryTableWrapper(String name, List<Attribute> attributes, boolean isEquality) {
		this(name, attributes, new LinkedList<AccessMethod>(), isEquality);
	}
	
	/**
	 * @param name String
	 * @param attributes List<Attribute>
	 */
	public InMemoryTableWrapper(String name, List<Attribute> attributes) {
		this(name, attributes, false);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.InMemoryRelation#load(java.util.Collection)
	 */
	public void load(Collection<Tuple> d) {
		for (Tuple t: d) {
			Preconditions.checkState(t.getType().equals(this.getType()));
			this.data.add(t);
		}
		this.setMetadata(new StaticMetadata((long) this.data.size()));
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.InMemoryRelation#clear()
	 */
	public void clear() {
		this.data.clear();
	}
	
	/**
	 * @return the collection of tuple stored in memory for this relation
	 */
	public Collection<Tuple> getData() {
		return this.data;
	}
	
	/**
	 * @param inputHeader 
	 * @param inputTuples 
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.runtime.RelationAccessWrapper#access(Table)
	 */
	@Override
	public Table access(List<? extends Attribute> inputHeader,
			ResetableIterator<Tuple> inputTuples) {
		Preconditions.checkArgument(inputHeader != null);
		Preconditions.checkArgument(inputTuples != null);
		
		Table result = new Table(this.attributes);

		ResetableIterator<Tuple> iterator = this.iterator(inputHeader, inputTuples);
		iterator.open();
		while (iterator.hasNext()) {
			result.appendRow(iterator.next());
		}
		return result;
	}

	/**
	 * @return the content of the view materialized in memory 
	 * @see uk.ac.ox.cs.pdq.runtime.RelationAccessWrapper#access()
	 */
	@Override
	public Table access() {
		Table result = new Table(this.attributes);
		ResetableIterator<Tuple> iterator = this.iterator();
		iterator.open();
		while (iterator.hasNext()) {
			result.appendRow(iterator.next());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.RelationAccessWrapper#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return new Properties();
	}

	/**
	 * @param inputAttributes List<? extends Attribute>
	 * @param inputs ResetableIterator<Tuple>
	 * @return ResetableIterator<Tuple>
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.Pipelineable#iterator(List<? extends Attribute>, ResetableIterator<Tuple>)
	 */
	public ResetableIterator<Tuple> iterator(
			List<? extends Attribute> inputAttributes, ResetableIterator<Tuple> inputs) {
		return new AccessIterator((List<Attribute>) inputAttributes, inputs);
	}

	/**
	 * @return ResetableIterator<Tuple>
	 * @see uk.ac.ox.cs.pdq.runtime.Pipelineable#iterator()
	 */
	public ResetableIterator<Tuple> iterator() {
		return new AccessIterator();
	}

	/**
	 * The class encapsulates the pipelined behaviour of the Wrapper.
	 * 
	 * @author Julien Leblay
	 */
	private class AccessIterator implements ResetableIterator<Tuple> {

		/** The list of input attributes */
		private final List<Attribute> inputAttributes;
		
		/** Iterator over a set of the input tuples. */
		private final ResetableIterator<Tuple> inputs;

		/** Iterator over a set of the output tuples. */
		private Iterator<Tuple> outputs;

		private final TupleType inputType;
		
		private Set<Tuple> filter = new LinkedHashSet<>();
		
		private Tuple nextTuple = null;

		/**
		 * Constructor with input tuples, i.e. free access
		 */
		public AccessIterator() {
			this(null, (ResetableIterator<Tuple>) null);
		}
		
		/**
		 * Constructor with input tuple iterator
		 * @param inputAttributes List<Attribute>
		 * @param inputTuples ResetableIterator<Tuple>
		 */
		public AccessIterator(List<Attribute> inputAttributes, ResetableIterator<Tuple> inputTuples) {
			this.inputAttributes = inputAttributes;
			this.inputs = inputTuples;
			this.outputs = InMemoryTableWrapper.this.getData().iterator();
			if (inputAttributes != null) {
				this.inputType = TupleType.DefaultFactory.createFromTyped(inputAttributes);
			} else {
				this.inputType = TupleType.EmptyTupleType;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#open()
		 */
		@Override
		public void open() {
			if (this.inputs != null) {
				this.inputs.open();
				while (this.inputs.hasNext()) {
					this.filter.add(this.inputs.next());
				}
			}
			this.nextTuple();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#reset()
		 */
		@Override
		public void reset() {
			this.outputs = InMemoryTableWrapper.this.getData().iterator();
			this.nextTuple();
		}

		/**
		 * @return AccessIterator
		 * @see uk.ac.ox.cs.pdq.util.ResetableIterator#deepCopy()
		 */
		@Override
		public AccessIterator deepCopy() {
			return new AccessIterator(
					this.inputAttributes,
					this.inputs != null ? this.inputs.deepCopy() : null);
		}
		
		/**
		 * @return boolean
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			synchronized (this) {
				return this.nextTuple != null;
			}
		}

		/**
		 * @return Tuple
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Tuple next() {
			synchronized (this) {
				if (this.nextTuple == null) {
					throw new NoSuchElementException();
				}
				Tuple result = this.nextTuple;
				this.nextTuple();
				return result;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(Relation.class.getSimpleName()).append('{')
				.append(InMemoryTableWrapper.this.getName()).append('}').append('.')
				.append(this.getClass().getSimpleName()).append('(')
				.append(this.inputs).append(')');
			
			return result.toString();
		}
		
		/*
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Set the next tuple to the following item on the output iterator, 
		 * using the next input tuple if necessary.
		 * The nextTuple is set to null, if the iterator has reached the end.
		 */
		public void nextTuple() {
			this.nextTuple = null;

			Object[] subTuple = new Object[this.inputType.size()];
			while (this.outputs.hasNext()) {
				Tuple t = this.outputs.next();
				int i = 0;
				if (this.inputAttributes != null) {
					for (Attribute att: this.inputAttributes) {
						subTuple[i] = t.getValue(InMemoryTableWrapper.this.getAttributeIndex(att.getName()));
						i++;
					}
					if (this.filter.contains(this.inputType.createTuple(subTuple))) {
						this.nextTuple = t;
						return;
					}
				} else if (this.filter.isEmpty()) {
					this.nextTuple = t;
					return;
				} else {
					throw new IllegalStateException();
				}
			}
		}
	}
}
