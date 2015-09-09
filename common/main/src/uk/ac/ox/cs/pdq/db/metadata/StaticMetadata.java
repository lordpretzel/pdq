package uk.ac.ox.cs.pdq.db.metadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.util.Tuple;



/**
 * Implementation of relation statistics
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class StaticMetadata implements RelationMetadata {

	/** The size (in tuples of the relation) */
	private long size;

	private final Map<AccessMethod, Cost> perInputTupleCost;

	public StaticMetadata() {
		this(0L, new LinkedHashMap<AccessMethod, Cost>());
	}

	/**
	 * Constructor for StaticMetadata.
	 * @param size Long
	 */
	public StaticMetadata(Long size) {
		this(size, new LinkedHashMap<AccessMethod, Cost>());
	}

	/**
	 * Constructor for StaticMetadata.
	 * @param perTupleCost Map<AccessMethod,Cost>
	 */
	public StaticMetadata(Map<AccessMethod, Cost> perTupleCost) {
		this(1L, perTupleCost);
	}

	/**
	 * Constructor for StaticMetadata.
	 * @param size Long
	 * @param perTupleCost Map<AccessMethod,Cost>
	 */
	public StaticMetadata(Long size, Map<AccessMethod, Cost> perTupleCost) {
		this.size = size;
		this.perInputTupleCost = perTupleCost;
	}

	/**
	 * @return Long
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#getSize()
	 */
	@Override
	public Long getSize() {
		return this.size;
	}

	/**
	 * @param positions List<Integer>
	 * @return Double
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#getSelectivity(List<Integer>)
	 */
	@Override
	public Double getSelectivity(List<Integer> positions) {
		return 0.1;
	}

	/**
	 * @param positions List<Integer>
	 * @param tuple Tuple
	 * @return Double
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#getSelectivity(List<Integer>, Tuple)
	 */
	@Override
	public Double getSelectivity(List<Integer> positions, Tuple tuple) {
		return 0.1;
	}

	/**
	 * @param binding AccessMethod
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#getPerInputTupleCost(AccessMethod)
	 */
	@Override
	public Cost getPerInputTupleCost(AccessMethod binding) {
		Cost result = this.perInputTupleCost.get(binding);
		if (result == null) {
			result = DoubleCost.UPPER_BOUND;
		}
		return result;
	}

	/**
	 * @param binding AccessMethod
	 * @param c Cost
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#setPerInputTupleCost(AccessMethod, Cost)
	 */
	@Override
	public void setPerInputTupleCost(AccessMethod binding, Cost c) {
		this.perInputTupleCost.put(binding, c);
	}

	/**
	 * @param accessCosts Map<AccessMethod,Cost>
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#setPerInputTupleCosts(Map<AccessMethod,Cost>)
	 */
	@Override
	public void setPerInputTupleCosts(Map<AccessMethod, Cost> accessCosts) {
		this.perInputTupleCost.putAll(accessCosts);
	}

	/**
	 * @param s Long
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#setSize(Long)
	 */
	@Override
	public void setSize(Long s) {
		this.size = s;
	}
}
