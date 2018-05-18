package uk.ac.ox.cs.pdq.datasources.io.jaxb.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// AccessMethod is the XML element which corresponds to the <access-method> tag
@XmlType (propOrder= {"name", "type", "cost", "template", "rest", "attributes"})
public class AccessMethod {
	
	private String name;
	private String type;
	private String cost;
	private String template;
	private StaticAttribute rest;
	private AccessMethodAttribute[] attributes;
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlAttribute
	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	@XmlAttribute
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@XmlElement (name="rest")
	public StaticAttribute getRest() {
		return rest;
	}

	public void setRest(StaticAttribute rest) {
		this.rest = rest;
	}

	@XmlElement (name="attribute")
	public AccessMethodAttribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(AccessMethodAttribute[] attributes) {
		this.attributes = attributes;
	}

	public String toString()
	{
		return "AccessMethod";
	}
}