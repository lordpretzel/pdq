package uk.ac.ox.cs.pdq.db;

import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
//TODO fix the equals and 
public class View extends Relation {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4961888228318423619L;

	/** 
	 * TOCOMMENT what is this supposed to mean, and why is it a LinearGuarded dependency?
	 *  The inverse dependency that defines the view. */
	protected LinearGuarded viewToRelationDependency;

	/**  
	 * TOCOMMENT So a view extends a Relation?? And does not have a declared connection to TGD except that it essentially is a TGD wrapper.
	 * The dependency that defines the view. */
	protected TGD relationToViewDependency;

	public View(String name, Attribute[] attributes) {
		super(name, attributes);
	}
	
	public View(String name, Attribute[] attributes, AccessMethod[] methods) {
		super(name, attributes, methods);
	}
	
	/**
	 * Instantiates a new view.
	 *
	 * @param dependency 		The dependency that defines the view
	 * @param accessMethods 		The binding patterns with which a view can be accessed. By default, a view has free access
	 */
	public View(LinearGuarded dependency, AccessMethod[] accessMethods) {
		super(dependency.getBodyAtom(0).getPredicate().getName(), Utility.getAttributes(dependency.getGuard()), accessMethods);
		this.viewToRelationDependency = dependency;
		this.relationToViewDependency = TGD.create(this.viewToRelationDependency.getHead(), this.viewToRelationDependency.getBody());
//		this.setAccessMethods(accessMethods);
	}

	/**
	 * TOCOMMENT ???
	 * Gets the dependency.
	 *
	 * @return LinearGuarded
	 */
	public LinearGuarded getViewToRelationDependency() {
		return this.viewToRelationDependency;
	}

	/**
	 * Gets the definition.
	 *
	 * @return the TGD defining the view
	 */
	public TGD getRelationToViewDependency() {
		return this.relationToViewDependency;
	}

	/**
	 * Sets the dependency.
	 *
	 * @param viewToRelationDependency LinearGuarded
	 */
	public void setViewToRelationDependency(LinearGuarded viewToRelationDependency) {
		this.viewToRelationDependency = LinearGuarded.create(
				Atom.create(this, viewToRelationDependency.getBodyAtom(0).getTerms()),
				viewToRelationDependency.getHead() instanceof QuantifiedFormula ? 
						viewToRelationDependency.getHead().getChild(0) :
				viewToRelationDependency.getHead());
		
		this.relationToViewDependency = TGD.create(
				this.viewToRelationDependency.getHead() instanceof QuantifiedFormula ? 
						this.viewToRelationDependency.getHead().getChild(0) :
							this.viewToRelationDependency.getHead(), 
							this.viewToRelationDependency.getBody());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.name.equals(((View) o).name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}
	
}