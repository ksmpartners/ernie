package org.eclipse.birt.report.engine.emitter.csv;

import org.eclipse.birt.report.engine.api.IRenderOption;

public interface ICSVRenderOption extends IRenderOption{
	
	/**
	 * CSV Emitter Id
	 */
	public static final String OUTPUT_EMITTERID_CSV = "org.eclipse.birt.report.engine.emitter.csv";	
	
	/**
	 * CSV Output Format
	 */
	public static final String OUTPUT_FORMAT_CSV = "csv";
	
	/**
	 * The option to decide to show data type in second row of output CSV
	 */
	public static final String SHOW_DATATYPE_IN_SECOND_ROW = "csvRenderOption.showDatatypeInSecondRow";
	
	/**
	 * The option to export a specific table by name in the CSV Output
	 */
	public static final String EXPORT_TABLE_BY_NAME = "csvRenderOption.exportTableByName";
	
	/**
	 * The option to specify the field delimiter, default is comma
	 */
	public static final String DELIMITER = "csvRenderOption.Delimiter";
	
	/**
	 * The option to specify the character with which delimiter should be replaced with 
	 * if it occurs inside the text, default is blank space
	 */
	public static final String REPLACE_DELIMITER_INSIDE_TEXT_WITH = "csvRenderOption.replaceDelimiterInsideTextWith";
	
	public void setShowDatatypeInSecondRow(boolean showDatatypeInSecondRow);
	
	public boolean getShowDatatypeInSecondRow();
	
	public void setExportTableByName(String tableName);
	
	public String getExportTableByName();
	
	public void setDelimiter (String fieldDelimiter);
	
	public String getDelimiter ();
	
	public void setReplaceDelimiterInsideTextWith (String replaceDelimiterInsideTextWith);
	
	public String getReplaceDelimiterInsideTextWith ();
}
