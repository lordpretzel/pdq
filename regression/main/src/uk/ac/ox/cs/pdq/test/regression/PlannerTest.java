package uk.ac.ox.cs.pdq.test.regression;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LeftDeepPlanReader;
import uk.ac.ox.cs.pdq.io.xml.PlanWriter;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.DominanceTypes;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.SuccessDominanceTypes;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion;
import uk.ac.ox.cs.pdq.test.acceptance.ApproximateCostAcceptanceCheck;
import uk.ac.ox.cs.pdq.test.acceptance.SameCostAcceptanceCheck;
import uk.ac.ox.cs.pdq.test.regression.Bootstrap.Command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;

/**
 * Runs regression tests.
 * 
 * @author Julien Leblay
 */
public class PlannerTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(PlannerTest.class);

	private static enum Modes {COMPARE, PREPARE, VALIDATE};
	
	/** File name where planning related parameters must be stored in a test case directory */
	private static final String PLAN_PARAMETERS_FILE = "case.properties";
	
	/** File name where the schema must be stored in a test case directory */
	private static final String SCHEMA_FILE = "schema.xml";

	/** File name where the query must be stored in a test case directory */
	private static final String QUERY_FILE = "query.xml";

	/** File name where the expected plan must be stored in a test case directory */
	private static final String PLAN_FILE = "expected-plan.xml";

	private final Modes mode;

	private Map<String, String> paramOverrides = new HashMap<>();
	
	@Parameters(separators = ",", commandDescription = "Runs regression tests on the planner libraries.")
	public static class PlannerTestCommand extends Command {
		@Parameter(names = { "-p", "--prepare" }, required = false, 
				description = "Prepare test case (outputs a plan.xml file in the input directory), without actually checking anything.")
		private boolean prepare = false;

		@Parameter(names = { "-v", "--validate" }, required = false, 
				description = "Does not actually runs test, but merely checks whether the settings are correct.")
		private boolean validate = false;

		public PlannerTestCommand() {
			super("planner");
		}

		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new PlannerTest(
					this.prepare ? Modes.PREPARE : (this.validate ? Modes.VALIDATE : Modes.COMPARE),
					getParameterOverrides()).recursiveRun(new File(getInput()));
		}
	}
	
	/**
	 * Sets up a regression test for the given test case directory.
	 * 
	 * @param input the command line parameters as given by the main method.
	 * @throws ReflectiveOperationException 
	 * @throws IOException
	 * @throws RegressionTestException 
	 */
	public PlannerTest(Modes mode, Map<String, String> paramOverrides) throws ReflectiveOperationException, IOException, RegressionTestException {
		super();
		this.mode = mode;
		this.paramOverrides = paramOverrides;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.regression.RegressionTest#run(java.io.File)
	 */
	@Override
	protected boolean run(File directory) throws RegressionTestException, IOException, ReflectiveOperationException {
		switch(this.mode) {
		case PREPARE:
			return this.prepare(directory);
		case VALIDATE:
			return this.validate(directory);
		default:
			return this.compare(directory);
		}
	}

	/**
	 * Runs a single test case base on the 
	 * @param directory
	 * @return boolean
	 * @throws ReflectiveOperationException
	 * @throws SQLException
	 */
	private boolean compare(File directory) throws ReflectiveOperationException {
		try(FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
			FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE)) {

			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
			PlannerParameters plannerParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			override(plannerParams, paramOverrides);
			override(costParams, paramOverrides);
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			
			Schema schema = new SchemaReader().read(sis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			Plan expectedPlan = obtainPlan(directory, schema, query);
			if (schema == null || query == null) {
				throw new RegressionTestException(
					"Schema and query must be provided for each regression test. "
					+ "(schema:" + schema + ", query: " + query + ", plan: " + expectedPlan + ")");
			}
			
			Plan observedPlan = null;
			try(ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				Planner planner = new Planner(plannerParams, costParams, reasoningParams, schema, query);
				planner.registerEventHandler(
						new IntervalEventDrivenLogger(
								pLog, plannerParams.getLogIntervals(),
								plannerParams.getShortLogIntervals()));
				observedPlan = planner.search();
			} catch (LimitReachedException lre) {
				log.warn(lre);
			}
			AcceptanceCriterion<Plan, Plan> acceptance = acceptance(plannerParams, costParams);
			this.out.print("Using " + acceptance.getClass().getSimpleName() + ": ");
			acceptance.check(expectedPlan, observedPlan).report(this.out);
			
			if (observedPlan != null
				&& (expectedPlan == null || expectedPlan.getCost().greaterThan(observedPlan.getCost())) ) {
				this.out.print("\twriting plan: " + observedPlan + " " + observedPlan.getCost());
				try (PrintStream o = new PrintStream(directory.getAbsolutePath() + '/' + PLAN_FILE)) {
					PlanWriter.to(o).write(observedPlan);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			return handleException(e, directory);
		}
		return true;
	}

	private boolean handleException(Throwable e, File directory) {
		if (e instanceof InconsistentParametersException) {
			log.debug(e);
			this.out.println("SKIP: Inconsistent parameters " + e.getMessage());
			return true;
		}
		if (e instanceof FileNotFoundException) {
			log.debug(e);
			this.out.println("SKIP: ('" + directory + "' not a valid case directory)");
			return true;
		}
		if (e instanceof PlannerException) {
			e.printStackTrace();
			log.warn(e);
			Throwable cause = e.getCause();
			if (cause instanceof LimitReachedException) {
				Reasons reason = ((LimitReachedException) cause).getReason();
				this.out.println("LIMIT: " 
						+ (reason != null ? " " + reason : " ") 
						+ Strings.nullToEmpty(cause.getMessage()));
			} else if (cause != null) {
				this.out.println("FAIL: " + cause.getClass().getSimpleName() + " " + cause.getMessage());
				cause.printStackTrace(this.out);
			}
			return false;
		}
		if (e instanceof Exception) {
			log.warn(e);
			e.printStackTrace(this.out);
			this.out.println("FAIL: " + e.getClass().getSimpleName() + " " + e.getMessage());
			return false;
		}
		log.error(e);
		this.out.println("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
		e.printStackTrace(this.out);
		System.exit(-1);
		return false;
	}
	
	/**
	 * @param directory File
	 * @param schema Schema
	 * @param query Query
	 * @return Plan
	 */
	private Plan obtainPlan(File directory, Schema schema, Query<?> query) {
		try(FileInputStream pis = new FileInputStream(directory.getAbsolutePath() + '/' + PLAN_FILE);
			BufferedInputStream bis = new BufferedInputStream(pis)) {
			try {
				bis.mark(1024);
				return new LeftDeepPlanReader(schema).read(bis); 
			} catch (Exception re) {
				bis.reset();
			}
		} catch (IOException e) {
		}
		try(FileInputStream pis = new FileInputStream(directory.getAbsolutePath() + '/' + PLAN_FILE);
			BufferedInputStream bis = new BufferedInputStream(pis)) {
			try {
				bis.mark(1024);
				return new DAGPlanReader(schema).read(bis); 
			} catch (Exception re) {
				bis.reset();
				log.error(re);
			}
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * @param params planning parameters
	 * @return a acceptance matching the given parameters
	 */
	private static AcceptanceCriterion<Plan, Plan> acceptance(
			PlannerParameters params, CostParameters cost) {
		switch (params.getPlannerType()) {
		case DAG_CHASEFRIENDLYDP:
		case DAG_GENERIC:
		case DAG_SIMPLEDP:
		case DAG_OPTIMIZED:
			if (params.getSuccessDominanceType() == SuccessDominanceTypes.OPEN
			|| params.getDominanceType() == DominanceTypes.OPEN) {
				return new ApproximateCostAcceptanceCheck();
			}
			switch (params.getValidatorType()) {
			case DEPTH_VALIDATOR:
			case APPLYRULE_DEPTH_VALIDATOR:
			case RIGHT_DEPTH_VALIDATOR:
				return new ApproximateCostAcceptanceCheck();
			}
			if (params.getFilterType() != null) {
				return new ApproximateCostAcceptanceCheck();
			}
			break;
		default:
			return new SameCostAcceptanceCheck();
		}
		return new SameCostAcceptanceCheck();
	}
	
	/**
	 * @return the value of the prepare argument
	 */
	public boolean isPrepare() {
		return this.mode == Modes.PREPARE;
	}
	
	/**
	 * @return the value of the valide argument
	 */
	public boolean isValidate() {
		return this.mode == Modes.VALIDATE;
	}

	/**
	 * Runs a single test case base on the 
	 * @param directory
	 * @return boolean
	 * @throws ReflectiveOperationException
	 * @throws SQLException
	 */
	private boolean validate(File directory) throws ReflectiveOperationException {
		try(FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
			FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE)) {
			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
			PlannerParameters plannerParams = new PlannerParameters(
					new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE), true);
			CostParameters costParams = new CostParameters(
					new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE), true);
			Schema schema = new SchemaReader().read(sis);
			Query<?> query = new QueryReader(schema).read(qis);
			if (schema == null || query == null) {
				throw new RegressionTestException("Schema and query must be provided for each regression test. (schema:" + schema + ", query: " + query + ")");
			}
		} catch (IllegalStateException e) {
			log.warn(e);
			this.out.println("WARN: " + e.getMessage());
		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("SKIP: (not a valid case directory)");
		} catch (Exception e) {
			this.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			return false;
		}
		return true;
	}

	/**
	 * Runs a single test case base on the 
	 * @param directory
	 * @return boolean
	 * @throws ReflectiveOperationException
	 * @throws SQLException
	 */
	private boolean prepare(File directory) throws ReflectiveOperationException {
		try(FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
			FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE)) {
			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
			PlannerParameters plannerParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			
			Schema schema = new SchemaReader().read(sis);
			Query<?> query = new QueryReader(schema).read(qis);
			if (schema == null || query == null) {
				throw new RegressionTestException("Schema and query must be provided for each regression test. (schema:" + schema + ", query: " + query + ")");
			}
			
			Plan plan = null;
			try(ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				Planner planner = new Planner(plannerParams, costParams, reasoningParams, schema, query);
				planner.registerEventHandler(new IntervalEventDrivenLogger(pLog, plannerParams.getLogIntervals(), plannerParams.getShortLogIntervals()));
				plan = planner.search();
			} catch (LimitReachedException lre) {
				log.warn(lre);
			}
			if (plan != null) {
				this.out.print("\twriting plan: " + plan + " " + plan.getCost());
				try (PrintStream o = new PrintStream(directory.getAbsolutePath() + '/' + PLAN_FILE)) {
					PlanWriter.to(o).write(plan);
				}
			} else {
				this.out.print("\tno plan found.");
//				new File(directory.getAbsolutePath() + '/' + PLAN_FILE).delete();
			}
		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("SKIP: (not a valid case directory)");
		} catch (Exception e) {
			this.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			return false;
		}
		return true;
	}
}
