package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.CrossProduct;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.StaticInput;
import uk.ac.ox.cs.pdq.algebra.SubPlanAlias;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.EqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.io.xml.OperatorReader.Types;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Writes plans to XML.
 * 
 * @author Julien Leblay
 */
public class RelationalOperatorWriter extends PrettyWriter<RelationalOperator> implements Writer<RelationalOperator> {

	public static final String T = "T";
	public static final String EQUALITY = "=";
	public static final String SLASH = "/";
	public static final String INDEX = "#";
	public static final String SELECT = "\u03C3";
	public static final String RENAME = "\u03C1";
	public static final String PROJECT = "\u03C0";
	public static final String JOIN = " \u22C8";
	public static final String CROSS_PRODUCT = " \u2A2F ";
	public static final String OPEN = "(";
	public static final String CLOSE = ")";
	public static final String BRACKET_OPEN = "[";
	public static final String BRACKET_CLOSE = "]";
	public static final String ASSIGN = " \u21D0 ";
	public static final String EMPTY = " \u2205";
	public static final String CONJUNCTION = " \u2227 ";
	public static final String NEW_LINE = "\n";
	public static final String PERIOD = ".";
	public static final String COMMA = ",";
	public static final String SINGLE_QUOTE = "'";
	public static final String DOUBLE_QUOTE = "\"";

	/** Logger. */
	private static Logger log = Logger.getLogger(RelationalOperatorWriter.class);

	/**
	 * The default out to which operator should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;
	
	public RelationalOperatorWriter() {
		this(System.out);
	}
	
	public RelationalOperatorWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * Writes the given plan to the given output.
	 * @param out
	 * @param relation
	 */
	public void writeOperator(StringBuilder sb, RelationalOperator operator) {
		this.writeOperator(sb, operator, Maps.<RelationalOperator, String>newHashMap());
	}

	/**
	 * Writes the given plan to the given output.
	 * @param out
	 * @param relation
	 */
	public void writeOperator(StringBuilder sb, RelationalOperator operator, Map<RelationalOperator, String> aliases) {
		Preconditions.checkArgument(this.out != null);
		Preconditions.checkArgument(operator != null);
		Preconditions.checkArgument(aliases != null);

		String a = aliases.get(operator);
		if (a != null) {
			sb.append(a);
			return;
		}
		Types type = typeOf(operator);
		switch (type) {
		case JOIN:
		case DEPENDENT_JOIN:
			NaryOperator nop = ((NaryOperator) operator);
			this.writeOperator(sb, nop.getChildren().get(0), aliases);
			sb.append(JOIN).append(BRACKET_OPEN);
			this.formatPredicate(sb, ((Join) operator), aliases);
			sb.append(BRACKET_CLOSE);
			this.writeOperator(sb, nop.getChildren().get(1), aliases); 
			break;
		case CROSS_PRODUCT:
			this.writeOperator(sb, ((NaryOperator) operator).getChildren().get(0), aliases);
			sb.append(CROSS_PRODUCT);
			this.writeOperator(sb, ((NaryOperator) operator).getChildren().get(1), aliases); 
			break;
		case SELECT:
			sb.append(SELECT).append(BRACKET_OPEN);
			this.formatPredicate(sb, ((Selection) operator));
			sb.append(BRACKET_CLOSE).append(OPEN);
			this.writeOperator(sb, ((UnaryOperator) operator).getChild(), aliases); 
			sb.append(CLOSE);
			break;
		case PROJECT:
			sb.append(PROJECT).append(BRACKET_OPEN);
			this.writeProjectRename(sb, ((Projection) operator)); 
			sb.append(BRACKET_CLOSE).append(OPEN);
			this.writeOperator(sb, ((UnaryOperator) operator).getChild(), aliases); 
			sb.append(CLOSE);
			break;
		case RENAME:
			sb.append(RENAME).append(BRACKET_OPEN);
//			this.writeProjectRename(sb, ((Rename) operator));
			sb.append(BRACKET_CLOSE).append(OPEN);
			this.writeOperator(sb, ((UnaryOperator) operator).getChild(), aliases); 
			sb.append(CLOSE);
			break;
		case STATIC_INPUT:
			sb.append(OPEN);
			this.formatTuples(sb, ((StaticInput) operator).getTuples()); 
			sb.append(CLOSE);
			break;
		case DEPENDENT_ACCESS:
		case ACCESS:
			sb.append(((AccessOperator) operator).getRelation().getName())
				.append(SLASH)
				.append(((AccessOperator) operator).getAccessMethod().getName())
				.append(ASSIGN);
			if (operator instanceof Access) {
				this.writeOperator(sb, ((UnaryOperator) operator).getChild(), aliases);
			} else if (operator instanceof Scan) {
				sb.append(EMPTY);
			} 
			break;
		case ALIAS:
			RelationalOperator aliased =((SubPlanAlias) operator).getPlan().getOperator();
			String alias = aliases.get(aliased);
			Preconditions.checkState(alias != null);
			sb.append(alias);
			break;
		}
	}
	
	static Types typeOf(RelationalOperator operator) {
		if (operator instanceof Selection) {
			return Types.SELECT;
		}
		if (operator instanceof Projection) {
			return Types.PROJECT;
		}
		if (operator instanceof DependentJoin) {
			return Types.DEPENDENT_JOIN;
		}
		if (operator instanceof Join) {
			if (!(operator instanceof DependentJoin) && !((Join) operator).hasPredicate()) {
				return Types.CROSS_PRODUCT;
			}
			return Types.JOIN;
		}
		if (operator instanceof CrossProduct) {
			return Types.CROSS_PRODUCT;
		}
		if (operator instanceof DependentAccess || operator instanceof Access || operator instanceof Scan) {
			return Types.ACCESS;
		}
		if (operator instanceof StaticInput) {
			return Types.STATIC_INPUT;
		}
		if (operator instanceof SubPlanAlias) {
			return Types.ALIAS;
		}
		throw new IllegalArgumentException("Unsupported operator type " + operator);
	}

	/**
	 * Writes the given plan to the given output.
	 * @param out
	 * @param relation
	 */
	public void writeProjectRename(StringBuilder sb, Projection proj) {
		List<Term> projected = proj.getProjected();
		Map<Integer, Term> renaming = proj.getRenaming();
		int i = 0;
		String sep = "";
		for (Term t: projected) {
			sb.append(sep);
			if (t.isVariable() || t.isSkolem()) {
				sb.append(new Attribute(proj.getType().getType(i), t.toString()));
			} else {
				sb.append(t);
			}
			if (renaming != null && renaming.containsKey(i)) {
				sb.append(SLASH).append(renaming.get(i));
			}
			sep = ", ";
			i++;
		}
	}

	private void formatPredicate(StringBuilder sb, Selection selection) {
		Predicate predicate = selection.getPredicate();
		List<Term> columns = selection.getColumns();
		this.formatPredicate(sb, predicate, columns);
	}

	private void formatConjunctivePredicate(StringBuilder sb, ConjunctivePredicate<Predicate> conjunction, List<Term> columns) {
		String sep = "";
		for (Predicate predicate : conjunction) {
			sb.append(sep);
			this.formatPredicate(sb, predicate, columns);
			sep = CONJUNCTION;
		}
	}

	private void formatPredicate(StringBuilder sb, Predicate predicate, List<Term> columns) {
		if (predicate instanceof ConjunctivePredicate) {
			this.formatConjunctivePredicate(sb, (ConjunctivePredicate) predicate, columns);
		}
 		if (predicate instanceof AttributeEqualityPredicate) {
 			sb.append(columns.get(((EqualityPredicate) predicate).getPosition()))
 				.append(EQUALITY)
				.append(columns.get(((AttributeEqualityPredicate) predicate).getOther()));
		}
		if (predicate instanceof ConstantEqualityPredicate) {
 			sb.append(columns.get(((EqualityPredicate) predicate).getPosition()))
				.append(EQUALITY).append(SINGLE_QUOTE)
				.append(((ConstantEqualityPredicate) predicate).getValue()).append(SINGLE_QUOTE);
		}
	}

	private void formatPredicate(StringBuilder sb, Join join, Map<RelationalOperator, String> aliases) {
		Predicate predicate = join.getPredicate();
		List<Term> columns = join.getColumns();
		RelationalOperator subLeft = this.findSubPlanAlias(join.getChildren().get(0));
		String lAlias = aliases.get(subLeft);
		RelationalOperator subRight = this.findSubPlanAlias(join.getChildren().get(1));
		String rAlias = aliases.get(subRight);
		this.formatJoinPredicate(sb, predicate, lAlias, rAlias, columns);
	}
	
	private RelationalOperator findSubPlanAlias(RelationalOperator logOp) {
		if (logOp instanceof AccessOperator) {
			return logOp;
		}
		if (logOp instanceof SubPlanAlias) {
			return ((SubPlanAlias) logOp).getPlan().getOperator();
		}
		if (logOp instanceof UnaryOperator && !(logOp instanceof Access)) {
			return this.findSubPlanAlias(((UnaryOperator) logOp).getChild());
		}
		if (logOp instanceof NaryOperator) {
			for (RelationalOperator child: ((NaryOperator) logOp).getChildren()) {
				return this.findSubPlanAlias(child);
			}
		}
		return null;
	}

	private void formatJoinPredicate(StringBuilder sb, Predicate predicate, String left, String right, List<Term> terms) {
		if (predicate instanceof ConjunctivePredicate) {
			this.formatJoinConjunctivePredicate(sb, (ConjunctivePredicate) predicate, left, right, terms);
		}
		if (predicate instanceof AttributeEqualityPredicate) {
			if (left != null) {
				sb.append(left).append(PERIOD)
					.append(terms.get(((EqualityPredicate) predicate).getPosition()));
			} else {
				sb.append(INDEX).append(((EqualityPredicate) predicate).getPosition());
			}
			sb.append(EQUALITY);
			if (right != null) {
				sb.append(right).append(PERIOD)
					.append(terms.get(((AttributeEqualityPredicate) predicate).getOther()));
			} else {
				sb.append(INDEX).append(((AttributeEqualityPredicate) predicate).getOther());
			}
		}
		if (predicate instanceof ConstantEqualityPredicate) {
			if (left != null) {
				sb.append(left).append(PERIOD)
					.append(terms.get(((EqualityPredicate) predicate).getPosition()));
			} else {
				sb.append(INDEX)
					.append(((EqualityPredicate) predicate).getPosition());
			}
			sb.append(EQUALITY).append(SINGLE_QUOTE)
				.append(((ConstantEqualityPredicate) predicate).getValue()).append(SINGLE_QUOTE);
		}
	}

	private void formatJoinConjunctivePredicate(StringBuilder sb, ConjunctivePredicate<Predicate> conjunction, String left, String right, List<Term> columns) {
		String sep = "";
		for (Predicate predicate : conjunction) {
			sb.append(sep);
			this.formatJoinPredicate(sb, predicate, left, right, columns);
			sep = CONJUNCTION;
		}
	}
	
	static void formatTuples(StringBuilder sb, Collection<Tuple> tuples) {
		for (Tuple t: tuples) {
			String sep = OPEN;
			for (Object o: t.getValues()) {
				sb.append(sep).append(SINGLE_QUOTE).append(o).append(SINGLE_QUOTE);
				sep = COMMA;
			}
			sb.append(CLOSE);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.benchmark.io.AbstractWriter#save(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, RelationalOperator o) {
		StringBuilder sb = new StringBuilder();
		this.writeOperator(sb, o);
		out.print(sb);
	}

	@Override
	public void write(RelationalOperator o) {
		this.write(this.out, o);
	}
}
