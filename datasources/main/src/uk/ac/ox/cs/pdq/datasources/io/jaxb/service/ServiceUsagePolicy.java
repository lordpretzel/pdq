package uk.ac.ox.cs.pdq.datasources.io.jaxb.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// ServiceUsagePolicy is the XML element which corresponds to the <policy> tag
@XmlType (propOrder= {"name", "type", "limit", "startIndex", "pageSize", "pageIndex", "totalItems"})
public class ServiceUsagePolicy {
	
	private String name;
	private String type;
	private String limit;
	private String startIndex;
	private String pageSize;
	private String pageIndex;
	private String totalItems;

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
	public String getLimit() {
		return limit;
	}


	public void setLimit(String limit) {
		this.limit = limit;
	}

	@XmlAttribute (name = "start-index")
	public String getStartIndex() {
		return startIndex;
	}


	public void setStartIndex(String startIndex) {
		this.startIndex = startIndex;
	}


	@XmlAttribute (name = "page-size")
	public String getPageSize() {
		return pageSize;
	}


	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}


	@XmlAttribute (name = "page-index")
	public String getPageIndex() {
		return pageIndex;
	}


	public void setPageIndex(String pageIndex) {
		this.pageIndex = pageIndex;
	}


	@XmlAttribute (name = "total-items")
	public String getTotalItems() {
		return totalItems;
	}


	public void setTotalItems(String totalItems) {
		this.totalItems = totalItems;
	}

	
	public String toString()
	{
		return "Services";
	}
}