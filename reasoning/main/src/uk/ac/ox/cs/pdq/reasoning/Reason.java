package uk.ac.ox.cs.pdq.reasoning;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Bootstrapping class for starting the reasoner. 
 * 
 * @author Efthymia Tsamoura
 */
public class Reason {

	/** Logger. */
	private static Logger log = Logger.getLogger(Reason.class); 
	
	/** The Constant PROGRAM_NAME. */
	private static final String PROGRAM_NAME = "pdq-reasoning-<version>.jar";
	
	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	/**
	 * Checks if is help.
	 *
	 * @return true, if is help
	 */
	public boolean isHelp() {
		return this.help;
	}
	
	/** The schema path. */
	@Parameter(names = { "-s", "--schema" }, required = true,
			 validateWith=FileValidator.class,
		description ="Path to the input schema definition file.")
	private String schemaPath;
	
	/**
	 * Gets the schema path.
	 *
	 * @return the schema path
	 */
	public String getSchemaPath() {
		return this.schemaPath;
	}
	
	/** The query path. */
	@Parameter(names = { "-q", "--query" }, required = true,
			 validateWith=FileValidator.class,
		description ="Path to the input query definition file.")
	private String queryPath;
	
	/**
	 * Gets the query path.
	 *
	 * @return the query path
	 */
	public String getQueryPath() {
		return this.queryPath;
	}
	
	/** The config file. */
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
			description = "Directory where to look for configuration files. "
			+ "Default is the current directory.")
	private File configFile;
	
	/**
	 * Gets the config file.
	 *
	 * @return the config file
	 */
	public File getConfigFile() {
		return this.configFile;
	}
	
	/** The verbose. */
	@Parameter(names = { "-v", "--verbose" }, required = false,
		description ="Path to the input query definition file.")
	private boolean verbose = false;
	
	/**
	 * Checks if is verbose.
	 *
	 * @return true, if is verbose
	 */
	public boolean isVerbose() {
		return this.verbose;
	}

	/** The dynamic params. */
	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();

	/**
	 * Initialize the Bootstrap by reading command line parameters, and running
	 * the planner on them.
	 * @param args String[]
	 */
	private Reason(String... args) {
		JCommander jc = new JCommander(this);
		jc.setProgramName(PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		if (this.isHelp()) {
			jc.usage();
			return;
		}
		run();
	}

	/**
	 * Runs the planner from the input parameters, schema and query.
	 *
	 * @param <S> the generic type
	 */
	public <S extends ChaseState> void run() {				
		ReasoningParameters reasoningParams = this.getConfigFile() != null ?
				new ReasoningParameters(this.getConfigFile()) :
				new ReasoningParameters() ;
		for (String k : this.dynamicParams.keySet()) {
			reasoningParams.set(k, this.dynamicParams.get(k));
		}
		try(FileInputStream sis = new FileInputStream(this.getSchemaPath());
			FileInputStream qis = new FileInputStream(this.getQueryPath())) {

			Schema schema = new SchemaReader().read(sis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			
			if (schema == null || query == null) {
				throw new IllegalStateException("Schema and query must be provided.");
			}
			schema.updateConstants(query.getSchemaConstants());
			
			HomomorphismManager detector =
					new HomomorphismManagerFactory().getInstance(schema, reasoningParams);
			
			ReasonerFactory reasonerFactory = new ReasonerFactory(
					new EventBus(),
					true,
					reasoningParams);
								
			Chaser reasoner = reasonerFactory.getInstance();
//			//Creates a chase state that consists of the canonical database of the input query.
			ChaseState state = new DatabaseChaseListState(query, (DatabaseHomomorphismManager) detector);
			reasoner.reasonUntilTermination(state, schema.getDependencies());
			
			//TODO show something 
			
		} catch (Throwable e) {
			log.error("Planning aborted: " + e.getMessage(), e);
			System.exit(-1);
		}
	}

	/**
	 * Filters out files that do not exist or are directories.
	 * @author Julien LEBLAY
	 */
	public static class FileValidator implements IParameterValidator {
		
		/* (non-Javadoc)
		 * @see com.beust.jcommander.IParameterValidator#validate(java.lang.String, java.lang.String)
		 */
		@Override
		public void validate(String name, String value) throws ParameterException {
			try {
				File f = new File(value);
				if (!f.exists() || f.isDirectory()) {
					throw new ParameterException(name + " must be a valid configuration file.");
				}
			} catch (Exception e) {
				throw new ParameterException(name + " must be a valid configuration file.");
			}
		}
	}

	/**
	 * Instantiates the bootstrap.
	 *
	 * @param args String[]
	 */
	public static void main(String... args) {
		new Reason(args);
	}
}