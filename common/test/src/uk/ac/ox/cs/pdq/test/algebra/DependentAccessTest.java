package uk.ac.ox.cs.pdq.test.algebra;

import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;


/**
 * @author Julien Leblay
 */
public class DependentAccessTest extends RelationalOperatorTest {

	List<Term> outputTerms = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"), new TypedConstant<>("c"));
	List<Term> inputTerms = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"));
	EntityRelation R = new EntityRelation("R");
	TupleType outputType = TupleType.DefaultFactory.create(Integer.class, R, String.class);
	TupleType inputType = TupleType.DefaultFactory.create(Integer.class, R);

	DependentAccess operator;
	Relation r1, r2, r3, r4, r5, r6, r7, r8, r9;
	AccessMethod free, m1, m2;
	Map<Integer, TypedConstant<?>> staticInputs= new LinkedHashMap<>();

	@Before public void setup() throws RelationalOperatorException {
		staticInputs.put(2, (TypedConstant<?>) outputTerms.get(2));

		free = new AccessMethod();
		m1 = new AccessMethod("m1", Types.BOOLEAN, Lists.newArrayList(1));
		m2 = new AccessMethod("m2", Types.LIMITED, Lists.newArrayList(1, 3));
		
		r1 = new Relation("R1", Lists.<Attribute>newArrayList()) {};
		r2 = new Relation("R2", Lists.<Attribute>newArrayList(), Lists.newArrayList(free)) {};
		r3 = new Relation("R3", Lists.newArrayList(new Attribute(Integer.class, "a1"))) {};
		r4 = new Relation("R4", Lists.newArrayList(new Attribute(Integer.class, "a1")), Lists.newArrayList(free)) {};
		r5 = new Relation("R5", Lists.newArrayList(new Attribute(Integer.class, "a1")), Lists.newArrayList(m1)) {};
		r6 = new Relation("R6", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(R, "a2"), new Attribute(String.class, "a3"))) {};
		r7 = new Relation("R7", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(R, "a2"), new Attribute(String.class, "a3")), Lists.newArrayList(free)) {};
		r8 = new Relation("R8", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(R, "a2"), new Attribute(String.class, "a3")), Lists.newArrayList(m2)) {};
	}
	
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTest2ArgumentsInconsistentAccessMethod1() {
		new DependentAccess(r1, free);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTest2ArgumentsInconsistentAccessMethod2() {
		new DependentAccess(r4, m1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTest2ArgumentsInconsistentAccessMethod3() {
		new DependentAccess(r8, free);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTest2ArgumentsInconsistentAccessMethod4() {
		new DependentAccess(r8, m1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTest3ArgumentsInconsistentAccessMethod1() {
		new DependentAccess(r2, free, staticInputs);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTest3ArgumentsInconsistentAccessMethod2() {
		new DependentAccess(r4, free, staticInputs);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTest3ArgumentsInconsistentAccessMethod3() {
		new DependentAccess(r7, free, staticInputs);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNull2Arguments1() {
		new DependentAccess(null, free);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNull2Arguments2() {
		new DependentAccess(r2, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNull2Arguments() {
		new DependentAccess(null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNullArguments1() {
		new DependentAccess(null, null, (List) null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNullArguments2() {
		new DependentAccess(null, null, (Map) null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNullArgument1() {
		new DependentAccess(null, free, staticInputs);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNullArgument2() {
		new DependentAccess(r2, null, staticInputs);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNullArgument3() {
		new DependentAccess(r5, m1, (List) null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initDependentAccessTestNullArgument3bis() {
		new DependentAccess(r5, m1, (Map) null);
	}
	
	@Test public void initDependentAccessTest2ArgsArityNil() {
		this.operator = new DependentAccess(r2, free);
		Assert.assertEquals("DependentAccess operator type must match that of initialization", r2.getType(), this.operator.getType());
		Assert.assertTrue("DependentAccess operator inputs must match be empty", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("DependentAccess operator input type must be empty", TupleType.EmptyTupleType, this.operator.getInputType());
		Assert.assertEquals("DependentAccess operator relation must match that of initialization", r2, this.operator.getRelation());
		Assert.assertEquals("DependentAccess operator access method must match that of initialization", free, this.operator.getAccessMethod());
		Assert.assertTrue("DependentAccess operator static inputs must be empty", this.operator.getStaticInputs().isEmpty());
	}
	
	@Test public void initDependentAccessTest2ArgsArityOne() {
		this.operator = new DependentAccess(r4, free);
		Assert.assertEquals("DependentAccess operator type must match that of initialization", r4.getType(), this.operator.getType());
		Assert.assertTrue("DependentAccess operator inputs must match be empty", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("DependentAccess operator input type must match be empty", TupleType.EmptyTupleType, this.operator.getInputType());
		Assert.assertEquals("DependentAccess operator relation must match that of initialization", r4, this.operator.getRelation());
		Assert.assertEquals("DependentAccess operator access method must match that of initialization", free, this.operator.getAccessMethod());
		Assert.assertTrue("DependentAccess operator static inputs must be empty", this.operator.getStaticInputs().isEmpty());
	}
	
	@Test public void initDependentAccessTest2ArgsArityMoreThanOne() {
		this.operator = new DependentAccess(r7, free);
		Assert.assertEquals("DependentAccess operator type must match that of initialization", r7.getType(), this.operator.getType());
		Assert.assertTrue("DependentAccess operator inputs must be empty", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("DependentAccess operator input type must be empty", TupleType.EmptyTupleType, this.operator.getInputType());
		Assert.assertEquals("DependentAccess operator relation must match that of initialization", r7, this.operator.getRelation());
		Assert.assertEquals("DependentAccess operator access method must match that of initialization", free, this.operator.getAccessMethod());
		Assert.assertTrue("DependentAccess operator static inputs must be empty", this.operator.getStaticInputs().isEmpty());
	}
	
	@Test public void initDependentAccessTest3ArgsArityOne() {
		this.operator = new DependentAccess(r5, m1);
		Assert.assertEquals("DependentAccess operator type must match that of child", r5.getType(), this.operator.getType());
		Assert.assertEquals("DependentAccess operator inputs must match that of child", m1.getInputs().size(), this.operator.getInputTerms().size());
		Assert.assertEquals("DependentAccess operator input type must match that of child", TupleType.DefaultFactory.createFromTyped(r5.getInputAttributes(m1)), this.operator.getInputType());
		Assert.assertEquals("DependentAccess operator relation must match that of initialization", r5, this.operator.getRelation());
		Assert.assertEquals("DependentAccess operator access method must match that of initialization", m1, this.operator.getAccessMethod());
		Assert.assertTrue("DependentAccess operator static inputs must be empty", this.operator.getStaticInputs().isEmpty());
	}
	
	@Test public void initDependentAccessTest3ArgsArityMoreThanOne() {
		this.operator = new DependentAccess(r8, m2, staticInputs);
		Assert.assertEquals("DependentAccess operator type must match that of child", r8.getType(), this.operator.getType());
		Assert.assertEquals("DependentAccess operator relation must match that of initialization", r8, this.operator.getRelation());
		Assert.assertEquals("DependentAccess operator access method must match that of initialization", m2, this.operator.getAccessMethod());
		Assert.assertEquals("DependentAccess operator static inputs must match that of initialization", this.staticInputs, this.operator.getStaticInputs());
	}
	
	@Test public void initDependentAccessTest3ArgsArityMoreThanOne2() {
		this.operator = new DependentAccess(r8, m2, outputTerms);
		Assert.assertEquals("DependentAccess operator type must match that of child", r8.getType(), this.operator.getType());
		Assert.assertEquals("DependentAccess operator relation must match that of initialization", r8, this.operator.getRelation());
		Assert.assertEquals("DependentAccess operator access method must match that of initialization", m2, this.operator.getAccessMethod());
		Assert.assertEquals("DependentAccess operator static inputs must match that of initialization", this.staticInputs, this.operator.getStaticInputs());
	}
	
	@Test public void deepCopy() throws RelationalOperatorException {
		this.operator = new DependentAccess(r2, free);
		DependentAccess copy = this.operator.deepCopy();
		Assert.assertEquals("DependentAccess operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("DependentAccess copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentAccess copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentAccess copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentAccess copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentAccess copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("DependentAccess copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());
		Assert.assertEquals("DependentAccess copy static inputs must match that of operator", this.operator.getStaticInputs(), copy.getStaticInputs());

		this.operator = new DependentAccess(r4, free);
		copy = this.operator.deepCopy();
		Assert.assertEquals("DependentAccess operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("DependentAccess copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentAccess copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentAccess copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentAccess copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentAccess copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("DependentAccess copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());
		Assert.assertEquals("DependentAccess copy static inputs must match that of operator", this.operator.getStaticInputs(), copy.getStaticInputs());

		this.operator = new DependentAccess(r7, free);
		copy = this.operator.deepCopy();
		Assert.assertEquals("DependentAccess operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("DependentAccess copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentAccess copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentAccess copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentAccess copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentAccess copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("DependentAccess copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());
		Assert.assertEquals("DependentAccess copy static inputs must match that of operator", this.operator.getStaticInputs(), copy.getStaticInputs());

		this.operator = new DependentAccess(r5, m1);
		copy = this.operator.deepCopy();
		Assert.assertEquals("DependentAccess operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("DependentAccess copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentAccess copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentAccess copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentAccess copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentAccess copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("DependentAccess copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());
		Assert.assertEquals("DependentAccess copy static inputs must match that of operator", this.operator.getStaticInputs(), copy.getStaticInputs());

		this.operator = new DependentAccess(r8, m2, outputTerms);
		copy = this.operator.deepCopy();
		Assert.assertEquals("DependentAccess operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("DependentAccess copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentAccess copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentAccess copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentAccess copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentAccess copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("DependentAccess copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());
		Assert.assertEquals("DependentAccess copy static inputs must match that of operator", this.operator.getStaticInputs(), copy.getStaticInputs());

		this.operator = new DependentAccess(r8, m2, staticInputs);
		copy = this.operator.deepCopy();
		Assert.assertEquals("DependentAccess operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("DependentAccess copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentAccess copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentAccess copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentAccess copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentAccess copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("DependentAccess copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());
		Assert.assertEquals("DependentAccess copy static inputs must match that of operator", this.operator.getStaticInputs(), copy.getStaticInputs());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnNilArity() {
		this.operator = new DependentAccess(r2, free);
		this.operator.getColumn(this.r2.getArity());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnArityOne() {
		this.operator = new DependentAccess(r5, m1);
		this.operator.getColumn(this.r4.getArity());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnArityMoreThanOne() {
		this.operator = new DependentAccess(r8, m2, staticInputs);
		this.operator.getColumn(this.r7.getArity());
	}

	
	@Test public void getDepth() {
		this.operator = new DependentAccess(r2, free);
		Assert.assertEquals("DependentAccess depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
		this.operator = new DependentAccess(r4, free);
		Assert.assertEquals("DependentAccess depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
		this.operator = new DependentAccess(r7, free);
		Assert.assertEquals("DependentAccess depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
		this.operator = new DependentAccess(r5, m1);
		Assert.assertEquals("DependentAccess depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
		this.operator = new DependentAccess(r8, m2);
		Assert.assertEquals("DependentAccess depth must be exactly that of 1", (int) 1, (int) this.operator.getDepth());
	}

	@Test(expected=IllegalArgumentException.class) 
	public void getNegativeColumn() {
		this.operator = new DependentAccess(r2, free);
		this.operator.getColumn(-1);
	}

	@Test public void isClosed() {
		Assert.assertTrue("DependentAccess must closed on free access methods", new DependentAccess(r2, free).isClosed());
		Assert.assertTrue("DependentAccess must closed on free access methods", new DependentAccess(r4, free).isClosed());
		Assert.assertTrue("DependentAccess must closed on free access methods", new DependentAccess(r7, free).isClosed());
		Assert.assertFalse("DependentAccess must open on non-free access methods", new DependentAccess(r8, m2).isClosed());
		Assert.assertFalse("DependentAccess must open on non-free access methods", new DependentAccess(r8, m2).isClosed());
		Map<Integer, TypedConstant<?>> inputs = new LinkedHashMap<>();
		inputs.put(0, new TypedConstant<>("a"));
		inputs.put(1, new TypedConstant<>("b"));
		inputs.put(2, new TypedConstant<>("c"));
		Assert.assertTrue("DependentAccess must closed on non-free access methods where all inputs are statically satisfied", new DependentAccess(r8, m2, inputs).isClosed());
	}

	@Test public void isQuasiLeaf() {
		Assert.assertTrue("Operator's isQuasiLeaf must match always be true", new DependentAccess(r2, free).isQuasiLeaf());
		Assert.assertTrue("Operator's isQuasiLeaf must match always be true", new DependentAccess(r4, free).isQuasiLeaf());
		Assert.assertTrue("Operator's isQuasiLeaf must match always be true", new DependentAccess(r5, m1).isQuasiLeaf());
		Assert.assertTrue("Operator's isQuasiLeaf must match always be true", new DependentAccess(r7, free).isQuasiLeaf());
		Assert.assertTrue("Operator's isQuasiLeaf must match always be true", new DependentAccess(r8, m2).isQuasiLeaf());
	}

	@Test public void isLeftDeep() {
		Assert.assertTrue("Operator's isLeftDeep must match always be true", new DependentAccess(r2, free).isLeftDeep());
		Assert.assertTrue("Operator's isLeftDeep must match always be true", new DependentAccess(r4, free).isLeftDeep());
		Assert.assertTrue("Operator's isLeftDeep must match always be true", new DependentAccess(r5, m1).isLeftDeep());
		Assert.assertTrue("Operator's isLeftDeep must match always be true", new DependentAccess(r7, free).isLeftDeep());
		Assert.assertTrue("Operator's isLeftDeep must match always be true", new DependentAccess(r8, m2).isLeftDeep());
	}

	@Test public void isRightDeep() {
		Assert.assertTrue("Operator's isRightDeep must match always be true", new DependentAccess(r2, free).isRightDeep());
		Assert.assertTrue("Operator's isRightDeep must match always be true", new DependentAccess(r4, free).isRightDeep());
		Assert.assertTrue("Operator's isRightDeep must match always be true", new DependentAccess(r5, m1).isRightDeep());
		Assert.assertTrue("Operator's isRightDeep must match always be true", new DependentAccess(r7, free).isRightDeep());
		Assert.assertTrue("Operator's isRightDeep must match always be true", new DependentAccess(r8, m2).isRightDeep());
	}
}
