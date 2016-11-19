package uk.ac.ox.cs.pdq.test.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import junit.framework.Assert;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitTofacts;

/**
 * Tests the getMatches method of the DatabaseChaseInstance class 
 * 
 * @author georgeK
 * @author Efthymia Tsamoura
 *
 */
public class TestDatabaseChaseInstance {	
	protected DatabaseChaseInstance chaseState;
	
	private Relation rel1;
	private Relation rel2;
	private Relation rel3;
	
	private TGD tgd;
	private TGD tgd2;
	private EGD egd;

	private Schema schema;
				
	@Before
	public void setup() throws SQLException {
		Attribute at11 = new Attribute(String.class, "at11");
		Attribute at12 = new Attribute(String.class, "at12");
		Attribute at13 = new Attribute(String.class, "at13");
		this.rel1 = new Relation("R1", Lists.newArrayList(at11, at12, at13)) {};
		
		Attribute at21 = new Attribute(String.class, "at21");
		Attribute at22 = new Attribute(String.class, "at22");
		this.rel2 = new Relation("R2", Lists.newArrayList(at21, at22)) {};
		
		Attribute at31 = new Attribute(String.class, "at31");
		Attribute at32 = new Attribute(String.class, "at32");
		this.rel3 = new Relation("R3", Lists.newArrayList(at31, at32)) {};
		
		Atom R1 = new Atom(this.rel1, 
				Lists.newArrayList(new Variable("x"),new Variable("y"),new Variable("z")));
		Atom R2 = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("z")));
		Atom R2p = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("w")));
		
		Atom R3 = new Atom(this.rel3, 
				Lists.newArrayList(new Variable("y"),new Variable("w")));
		
		this.tgd = new TGD(Conjunction.of(R1),Conjunction.of(R2));
		this.tgd2 = new TGD(Conjunction.of(R1),Conjunction.of(R3));
		this.egd = new EGD(Conjunction.of(R2,R2p), Conjunction.of(new Equality(new Variable("z"),new Variable("w"))));

		this.schema = new Schema(Lists.<Relation>newArrayList(this.rel1, this.rel2, this.rel3), Lists.<Dependency>newArrayList(this.tgd,this.tgd2, this.egd));
		
		/** The driver. */
		String driver = null;
		/** The url. */
		String url = "jdbc:mysql://localhost/";
		/** The database. */
		String database = "pdq_chase";
		/** The username. */
		String username = "root";
		/** The password. */
		String password ="root";
				
		this.chaseState = new DatabaseChaseInstance(new ArrayList<Atom>(),new DatabaseConnection(new DatabaseParameters(), this.schema));
	}
	
	@After
	public void tearDown() throws Exception {
		this.chaseState.close();
	}
	
	@Test 
	public void test_getMatches1() {	
		Atom f20 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k1"), new Skolem("c"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k2"), new Skolem("c"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k3"), new Skolem("c"),new Skolem("c3")));

		Atom f23 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k4"), new Skolem("c"),new Skolem("c4")));

		Atom f24 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k5"), new Skolem("c"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k6"), new Skolem("c"),new TypedConstant(new String("Michael"))));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25));
		List<Match> matches = this.chaseState.getTriggers(Lists.newArrayList(this.tgd),TriggerProperty.ACTIVE,LimitTofacts.THIS);
		Assert.assertEquals(6, matches.size());
	}
	
	@Test 
	public void test_getMatches2() {
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("k"),new Skolem("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new Skolem("c4")));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new TypedConstant(new String("Michael"))));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25));
		List<Match> matches = this.chaseState.getTriggers(Lists.newArrayList(this.egd),TriggerProperty.ACTIVE,LimitTofacts.THIS);
		Assert.assertEquals(4, matches.size());
	}
	
	@Test 
	public void test_getMatches3() {
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("k"),new Skolem("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new Skolem("c4")));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new TypedConstant(new String("Michael"))));
		
		Equality eq1 = new Equality(new Skolem("c1"), new Skolem("c2"));
		Equality eq2 = new Equality(new Skolem("c1"), new Skolem("c3"));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, eq1,eq2));
		List<Match> matches = this.chaseState.getTriggers(Lists.newArrayList(this.egd),TriggerProperty.ALL,LimitTofacts.THIS);
		Assert.assertEquals(4, matches.size());
	}	
	
	@Test 
	public void test_getMatches4() {
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("k"),new Skolem("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new Skolem("c4")));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("p"),new TypedConstant(new String("Michael"))));
		
		Equality eq1 = new Equality(new Skolem("c1"), new Skolem("c2"));
		Equality eq2 = new Equality(new Skolem("c1"), new Skolem("c3"));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, eq1,eq2));
		List<Match> matches = this.chaseState.getTriggers(Lists.newArrayList(this.egd),TriggerProperty.ACTIVE,LimitTofacts.THIS);
		Assert.assertEquals(3, matches.size());
	}	
	
	@Test 
	public void test_getMatches5() {
		Atom f20 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k1"), new Skolem("c"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k2"), new Skolem("c"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k3"), new Skolem("c"),new Skolem("c3")));

		Atom f23 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k4"), new Skolem("c"),new Skolem("c4")));

		Atom f24 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k5"), new Skolem("c"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k6"), new Skolem("c"),new TypedConstant(new String("Michael"))));
		
		Atom f26 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));

		Atom f27 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c2")));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, f26, f27));
		List<Match> matches = this.chaseState.getTriggers(Lists.newArrayList(this.tgd), TriggerProperty.ACTIVE,LimitTofacts.THIS);
		Assert.assertEquals(4, matches.size());
	}
	
	@Test 
	public void test_getMatches6() {
		Atom f20 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k1"), new Skolem("r1"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k2"), new Skolem("r2"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("k3"), new Skolem("r3"),new Skolem("c3")));
		
		Atom f26 = new Atom(this.rel3, 
				Lists.newArrayList(new Skolem("r1"),new Skolem("skolem1")));

		Atom f27 = new Atom(this.rel3, 
				Lists.newArrayList(new Skolem("r2"),new Skolem("skolem2")));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f26,f27));
		List<Match> matches = this.chaseState.getTriggers(Lists.newArrayList(this.tgd2), TriggerProperty.ACTIVE,LimitTofacts.THIS);
		Assert.assertEquals(1, matches.size());
	}
	
}