package uk.ac.ox.cs.pdq.test.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

/**
 * @author Julien Leblay
 */
public class ScanTest extends RelationalOperatorTest {

	Scan operator;
	Relation r1, r2, r3, r4, r5, r6, r7, r8, r9;
	AccessMethod free, m1;

	@Before public void setup() throws RelationalOperatorException {
		free = new AccessMethod();
		m1 = new AccessMethod("m1", Types.BOOLEAN, Lists.newArrayList(1));
		
		r1 = new Relation("R1", Lists.<Attribute>newArrayList()) {};
		r2 = new Relation("R2", Lists.<Attribute>newArrayList(), Lists.newArrayList(free)) {};
		r3 = new Relation("R3", Lists.newArrayList(new Attribute(Integer.class, "a1"))) {};
		r4 = new Relation("R4", Lists.newArrayList(new Attribute(Integer.class, "a1")), Lists.newArrayList(free)) {};
		r5 = new Relation("R5", Lists.newArrayList(new Attribute(Integer.class, "a1")), Lists.newArrayList(m1)) {};
		r6 = new Relation("R6", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(Integer.class, "a2"), new Attribute(Integer.class, "a3"))) {};
		r7 = new Relation("R7", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(Integer.class, "a2"), new Attribute(Integer.class, "a3")), Lists.newArrayList(free)) {};
		r8 = new Relation("R8", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(Integer.class, "a2"), new Attribute(Integer.class, "a3")), Lists.newArrayList(m1)) {};
	}
	
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initScanTestNullArgument() {
		new Scan(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initScanTestInaccessibleRelation0() {
		new Scan(r1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initScanTestInaccessibleRelation1() {
		new Scan(r3);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initScanTestInaccessibleRelation3() {
		new Scan(r6);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initScanTestNonFreeRelation1() {
		new Scan(r5);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initScanTestNonFreeRelation2() {
		new Scan(r8);
	}
	
	@Test public void initScanTestArityNil() {
		this.operator = new Scan(r2);
		Assert.assertEquals("Scan operator relation must match that of initialization", r2, this.operator.getRelation());
		Assert.assertEquals("Scan operator access method must match that of initialization", free, this.operator.getAccessMethod());
		Assert.assertEquals("Scan operator output type must match that of relation", r2.getType(), this.operator.getType());
		Assert.assertTrue("Scan operator inputs must be empty", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("Scan operator input type must match that of child", TupleType.EmptyTupleType, this.operator.getInputType());
	}
	
	@Test public void initScanTestArityOne() {
		this.operator = new Scan(r4);
		Assert.assertEquals("Scan operator relation must match that of initialization", r4, this.operator.getRelation());
		Assert.assertEquals("Scan operator access method must match that of initialization", free, this.operator.getAccessMethod());
		Assert.assertEquals("Scan operator output type must match that of relation", r4.getType(), this.operator.getType());
		Assert.assertTrue("Scan operator inputs must be empty", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("Scan operator input type must match that of child", TupleType.EmptyTupleType, this.operator.getInputType());
	}
	
	@Test public void initScanTestArityMoreThanOne() {
		this.operator = new Scan(r7);
		Assert.assertEquals("Scan operator relation must match that of initialization", r7, this.operator.getRelation());
		Assert.assertEquals("Scan operator access method must match that of initialization", free, this.operator.getAccessMethod());
		Assert.assertEquals("Scan operator output type must match that of relation", r7.getType(), this.operator.getType());
		Assert.assertTrue("Scan operator inputs must be empty", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("Scan operator input type must match that of child", TupleType.EmptyTupleType, this.operator.getInputType());
	}
	
	@Test public void deepCopyArityNil() throws RelationalOperatorException {
		this.operator = new Scan(r2);
		Scan copy = this.operator.deepCopy();
		Assert.assertEquals("Scan operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Scan operator type must match that of child", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Scan operator type must match that of child", this.operator.getType(), copy.getType());
		Assert.assertEquals("Scan operator inputs must match that of child", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Scan operator input type must match that of child", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Scan operator relation must match that of child", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Scan operator access method must match that of child", this.operator.getAccessMethod(), copy.getAccessMethod());

		this.operator = new Scan(r4);
		copy = this.operator.deepCopy();
		Assert.assertEquals("Scan operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Scan operator type must match that of child", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Scan operator type must match that of child", this.operator.getType(), copy.getType());
		Assert.assertEquals("Scan operator inputs must match that of child", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Scan operator input type must match that of child", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Scan operator relation must match that of child", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Scan operator access method must match that of child", this.operator.getAccessMethod(), copy.getAccessMethod());

		this.operator = new Scan(r7);
		copy = this.operator.deepCopy();
		Assert.assertEquals("Scan operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Scan operator type must match that of child", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Scan operator type must match that of child", this.operator.getType(), copy.getType());
		Assert.assertEquals("Scan operator inputs must match that of child", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Scan operator input type must match that of child", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Scan operator relation must match that of child", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Scan operator access method must match that of child", this.operator.getAccessMethod(), copy.getAccessMethod());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnNilArity() {
		this.operator = new Scan(r2);
		this.operator.getColumn(this.r2.getArity());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnArityOne() {
		this.operator = new Scan(r4);
		this.operator.getColumn(this.r4.getArity());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnArityMoreThanOne() {
		this.operator = new Scan(r7);
		this.operator.getColumn(this.r7.getArity());
	}
	
	@Test public void testHashCode() throws RelationalOperatorException {
		Set<RelationalOperator> s = new LinkedHashSet<>();
		this.operator = new Scan(r2);
		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1", 1, s.size());

		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1, after being adding twice to set", 1, s.size());

		s.add(new Scan(r4));
		Assert.assertEquals("Operator set must have size 2, after new count is added", 2, s.size());

		// TODO: agree on semantics of adding deep copy.
		s.add(getOperator().deepCopy());
		Assert.assertEquals("Operator set must have size 2, after deep copy is added", 2, s.size());
	}

	
	@Test public void getDepth() {
		this.operator = new Scan(r2);
		Assert.assertEquals("Scan depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
		this.operator = new Scan(r4);
		Assert.assertEquals("Scan depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
		this.operator = new Scan(r7);
		Assert.assertEquals("Scan depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
	}

	@Test public void isClosed() {
		Assert.assertTrue("Scan isClosed must match always be true", new Scan(r2).isClosed());
		Assert.assertTrue("Scan isClosed must match always be true", new Scan(r4).isClosed());
		Assert.assertTrue("Scan isClosed must match always be true", new Scan(r7).isClosed());
	}

	@Test public void isQuasiLeaf() {
		Assert.assertTrue("Scan isQuasiLeaf must match always be true", new Scan(r2).isQuasiLeaf());
		Assert.assertTrue("Scan isQuasiLeaf must match always be true", new Scan(r4).isQuasiLeaf());
		Assert.assertTrue("Scan isQuasiLeaf must match always be true", new Scan(r7).isQuasiLeaf());
	}

	@Test public void isLeftDeep() {
		Assert.assertTrue("Scan isLeftDeep must match always be true", new Scan(r2).isLeftDeep());
		Assert.assertTrue("Scan isLeftDeep must match always be true", new Scan(r4).isLeftDeep());
		Assert.assertTrue("Scan isLeftDeep must match always be true", new Scan(r7).isLeftDeep());
	}

	@Test public void isRightDeep() {
		Assert.assertTrue("Scan isRightDeep must match always be true", new Scan(r2).isRightDeep());
		Assert.assertTrue("Scan isRightDeep must match always be true", new Scan(r4).isRightDeep());
		Assert.assertTrue("Scan isRightDeep must match always be true", new Scan(r7).isRightDeep());
	}
}
