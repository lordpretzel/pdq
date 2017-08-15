package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.io.File;

import org.junit.Test;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;

/**
 * @author Gabor
 *
 */
public class TestRestrictedChaserMultiRun {
	private static final int REPEAT = 50;
	
	@Test
	public void testSingleThreadDerby() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.setup();
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testMultiThreadDerby() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(new DatabaseParameters(), trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testSingleThreadMySQL() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(new DatabaseParameters(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\reasoning\\homomorphism\\MySql_case.properties")), trc.schema, 1));
		try {
			trc.test_reasonUntilTermination1();
		}catch(Throwable t) {
			t.printStackTrace();
			System.out.println(trc.state.obsoleteToRepresentative);
		}
		trc.tearDown();
	}
	
	@Test
	public void testMultiThreadMySQL() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(new DatabaseParameters(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\reasoning\\homomorphism\\MySql_case.properties")), trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testSingleThreadPostgres() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(new DatabaseParameters(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\reasoning\\homomorphism\\Postgres_case.properties")), trc.schema, 1));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testMultiThreadPostgres() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(new DatabaseParameters(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\reasoning\\homomorphism\\Postgres_case.properties")), trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testLongRunningMultiThreadMySql() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(new DatabaseParameters(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\reasoning\\homomorphism\\MySql_case.properties")), trc.schema, 1));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
		}
	}
	
	@Test
	public void testLongRunningMultiThreadPostgres() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(new DatabaseParameters(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\reasoning\\homomorphism\\Postgres_case.properties")), trc.schema, 1));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
		}
	}
	
	@Test
	public void testLongRunningMultiThreadDerby() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(new DatabaseParameters(), trc.schema, 1));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
		}
	}


}