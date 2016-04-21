package com.sddc.models.datacollectors;

public class UserDesignInput {
	private String designId;
	
	public UserDesignInput() { }
	/**
	 * @param dname Name of the design
	 */
	public void setDesignId(String dname) {
		this.designId = dname;
	}
	
	/**
	 * @return Name of the design
	 */
	public String getDesignId() {
		return this.designId;
	}
}
