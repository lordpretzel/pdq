package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.util.Properties;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.GroupUsagePolicy;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTResponseEvent;

/**
 * Check whether the number of results for a given period has been exceeded,
 * waits or throws an exception if so.
 * 
 * @author Julien Leblay
 *
 */
public class ResultAllowance extends PeriodicalAllowance {
	
	public ResultAllowance(int limit, long period, boolean wait) {
		super(limit, period, wait);
	}
	
	public ResultAllowance(Properties properties) {
		super(properties);
	}

	public ResultAllowance(GroupUsagePolicy gup) {
		super(gup);
	}

	@Override
	public UsagePolicy copy() {
		return new ResultAllowance(this.getLimit(), this.getPeriod(), this.isWait());
	}

	@Override
	protected int getAmount(RESTResponseEvent event) {
		return event.getOutput().size();
	}
}
