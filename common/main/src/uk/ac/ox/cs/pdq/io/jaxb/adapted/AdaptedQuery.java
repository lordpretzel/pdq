package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * @author Gabor
 *
 */
@XmlRootElement(name = "query")
public class AdaptedQuery {

	private Variable[] freeVariables;
	private Atom[] atoms;
	private String type;

	public AdaptedQuery() {
	}

	public AdaptedQuery(ConjunctiveQuery v) {
		freeVariables = v.getFreeVariables();
		atoms = v.getAtoms();
		type = "unset";
		if (v instanceof ConjunctiveQuery) {
			type = "conjunctive";
		}
	}

	public ConjunctiveQuery toQuery() {
		try {
			Formula something = Conjunction.of(atoms);
			ConjunctiveQuery ret = null;
			if (something instanceof Atom) {
				ret = ConjunctiveQuery.create(freeVariables, (Atom) something);
			}
			if (something instanceof Conjunction) {
				ret = ConjunctiveQuery.create(freeVariables, (Conjunction) something);
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@XmlAttribute
	public String getType() {
		return type;
	}

	@XmlElement(name = "atom")
	@XmlElementWrapper(name = "body")
	public Atom[] getAtoms() {
		return atoms;
	}

	@XmlElement(name = "variable")
	@XmlElementWrapper(name = "head")
	public Variable[] getHead() {
		return null;
	}

	public void setHead(Variable[] freeVariables) {
		this.freeVariables = freeVariables;
	}

	@XmlElement(name = "variable")
	@XmlElementWrapper(name = "free-variables")
	public Variable[] getFreeVariables() {
		return this.freeVariables;
	}

	public void setFreeVariables(Variable[] freeVariables) {
		this.freeVariables = freeVariables;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAtoms(Atom[] atoms) {
		this.atoms = atoms;
	}

}
