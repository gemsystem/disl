package org.disl.meta


@UniqueKey(columns=["ID","NAME"])
@Description("This is testing dimension.")
class TestDimensionTable extends Table {
	String schema="L2"
	
	@PrimaryKey
	@Description("Surrogate key.")
	Column KEY
	
	@UniqueKey
	@Description("Natural key")
	Column ID
	
	
	@Description("Dimension name.")
	Column NAME
	
	
	
}
