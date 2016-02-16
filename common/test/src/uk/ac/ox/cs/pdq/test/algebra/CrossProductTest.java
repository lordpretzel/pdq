package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.CrossProduct;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class CrossProductTest.
 *
 * @author Julien Leblay
 */
public class CrossProductTest extends NaryOperatorTest {

	/** The operator. */
	CrossProduct operator;
	
	/** The children. */
	List<RelationalOperator> children;
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.NaryOperatorTest#setup()
	 */
	@Before public void setup() throws RelationalOperatorException {
        super.setup();
        
        children = Lists.newArrayList(child1, child2, child3);
        this.operator = new CrossProduct(children);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#getOperator()
	 */
	@Override
	RelationalOperator getOperator() {
		return this.operator;
	}

	/**
	 * Inits the cross product test null argument2.
	 */
	@Test(expected=NullPointerException.class) 
	public void initCrossProductTestNullArgument2() {
		new CrossProduct((List) null);
	}

	/**
	 * Inits the cross product test null argument1.
	 */
	@Test(expected=NullPointerException.class) 
	public void initCrossProductTestNullArgument1() {
		new CrossProduct((RelationalOperator[]) null);
	}

	/**
	 * Inits the cross product test empty argument.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initCrossProductTestEmptyArgument() {
		new CrossProduct(Lists.<RelationalOperator>newArrayList());
	}

	/**
	 * Inits the cross product test.
	 */
	@Test public void initCrossProductTest() {
		Assert.assertEquals("CrossProduct children must match that of initialiazation", children, this.operator.getChildren());
		Assert.assertEquals("CrossProduct output must match the concatenation of childrens", outputTerms, this.operator.getColumns());
		Assert.assertEquals("CrossProduct output type must match the concatenation of childrens", outputType, this.operator.getType());
		Assert.assertEquals("CrossProduct input must match the concatenation of childrens", inputTerms, this.operator.getInputTerms());
		Assert.assertEquals("CrossProduct input type must match the concatenation of childrens", inputType, this.operator.getInputType());
	}

	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		CrossProduct copy = this.operator.deepCopy();
		Assert.assertEquals("CrossProduct copy's children must match that of operator", this.operator.getChildren(), copy.getChildren());
		Assert.assertEquals("CrossProduct copy's output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("CrossProduct copy's output type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("CrossProduct copy's input must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("CrossProduct copy's input type must match that of operator", this.operator.getInputType(), copy.getInputType());
	}
}