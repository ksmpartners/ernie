package org.eclipse.birt.report.engine.emitter.csv;


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.content.IBandContent;
import org.eclipse.birt.report.engine.content.ICellContent;
import org.eclipse.birt.report.engine.content.IElement;
import org.eclipse.birt.report.engine.content.ILabelContent;
import org.eclipse.birt.report.engine.content.IPageContent;
import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.content.IRowContent;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.content.ITableContent;
import org.eclipse.birt.report.engine.content.ITextContent;
import org.eclipse.birt.report.engine.css.engine.value.birt.BIRTConstants;
import org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter;
import org.eclipse.birt.report.engine.emitter.EmitterUtil;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.ir.EngineIRConstants;
import org.eclipse.birt.report.engine.presentation.ContentEmitterVisitor;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.elements.structures.OdaResultSetColumn;
import org.eclipse.birt.report.model.elements.Cell;
import org.eclipse.birt.report.model.elements.DataItem;
import org.eclipse.birt.report.model.elements.OdaDataSet;
import org.eclipse.birt.report.model.elements.interfaces.IDataItemModel;

public class CSVReportEmitter extends ContentEmitterAdapter
{		
	protected static Logger logger = Logger.getLogger( CSVReportEmitter.class.getName( ) );
	
	protected static final String OUTPUT_FORMAT_CSV = "csv";

	protected static final String REPORT_FILE = "report.csv";
	
	protected ContentEmitterVisitor contentVisitor;
	
	protected IEmitterServices services;
	
	protected CSVWriter writer; 
	
	protected IReportContent report;
	
	protected IRenderOption renderOption;
	
	protected int totalColumns;
	
	protected int currentColumn;
	
	protected OutputStream out = null;	
	
	protected boolean isFirstPage = false;	
	
	protected long firstTableID = -1;
	
	protected boolean writeData = true;
	
	protected Boolean showDatatypeInSecondRow = false;
	
	protected String tableToOutput;
	
	protected boolean outputCurrentTable;
	
	protected String delimiter = null;
	
	protected String replaceDelimiterInsideTextWith = null;
	
	public CSVReportEmitter( )
	{
		contentVisitor = new ContentEmitterVisitor( this );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#initialize(org.eclipse.birt.report.engine.emitter.IEmitterServices)
	 */
	public void initialize( IEmitterServices services ) throws EngineException
	{
		this.services = services;		
		this.out = EmitterUtil.getOuputStream( services, REPORT_FILE );			
		
		writer = new CSVWriter( );	
	}
	
	public void start( IReportContent report )
	{
		logger.log( Level.FINE,"Starting CSVReportEmitter." );
		
		this.report = report;
		
		this.renderOption = report.getReportContext().getRenderOption();
		
		this.tableToOutput= (String)renderOption.getOption(ICSVRenderOption.EXPORT_TABLE_BY_NAME);		
		
		// Setting tableToOutput to Default as user has not set any Render Option to Output a specific Table
		if(tableToOutput == null)
		{
			this.tableToOutput="Default";
		}
		
		this.delimiter = (String)renderOption.getOption(ICSVRenderOption.DELIMITER);
		
		// Setting Default Field Delimiter if user has not specified any Delimiter
		if(delimiter == null)
		{
			delimiter = CSVTags.TAG_COMMA;
		}
		
		this.replaceDelimiterInsideTextWith = (String)renderOption.getOption(ICSVRenderOption.REPLACE_DELIMITER_INSIDE_TEXT_WITH);
		
		// Setting Default Value Blank Space if user has not specified any value to replace the delimiter if it occurs inside text
		if(replaceDelimiterInsideTextWith == null)
		{
			replaceDelimiterInsideTextWith = " ";
		}
		
		// checking csv render option if set to export data type in second row of the output
		this.showDatatypeInSecondRow = (Boolean)renderOption.getOption(ICSVRenderOption.SHOW_DATATYPE_IN_SECOND_ROW);
		
		// Setting Default value to false if user has not specified aany value
		if(showDatatypeInSecondRow == null)
			showDatatypeInSecondRow = false;
		
		writer.open( out, "UTF-8" );
		writer.startWriter( );
	}
	
	public void end( IReportContent report )
	{
		logger.log( Level.FINE,"CSVReportEmitter end report." );
		
		writer.endWriter( );
		writer.close( );
		
		// Informing user if Table Name provided in Render Option is not found and Blank Report is getting generated
		if(tableToOutput != "Default" && report.getDesign().getReportDesign().findElement(tableToOutput) == null)
		{
			System.out.println(tableToOutput + " Table not found in Report Design. Blank Report Generated!!");
			logger.log(Level.WARNING, tableToOutput+ " Table not found in Report Design. Blank Report Generated!!");
		}
		if( out != null )
		{
			try
			{
				out.close( );
			}
			catch ( IOException e )
			{
				logger.log( Level.WARNING, e.getMessage( ), e );
			}
		}
	}
	
	public void startPage( IPageContent page ) throws BirtException
	{
		logger.log( Level.FINE,"CSVReportEmitter startPage" );
		
		startContainer( page );
		
		if(page.getPageNumber()>1){
			isFirstPage = false;
		}else{
			isFirstPage = true;			
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#startLabel(org.eclipse.birt.report.engine.content.ILabelContent)
	 * To avoid framework to print label for every page.
	 */
	public void startLabel( ILabelContent label ) throws BirtException
	{
		if(isFirstPage)
			startText( label );		
	}
	
	public void startTable( ITableContent table )
	{
		assert table != null;
		totalColumns = table.getColumnCount();
		
		if(firstTableID == -1)
			firstTableID = table.getInstanceID().getComponentID();		
		
		String currentTableName = table.getName();
		
		if(tableToOutput.equals("Default") && table.getInstanceID().getComponentID() == firstTableID)
		{
			this.outputCurrentTable = true;
		}
		else if(currentTableName != null && currentTableName.equals(this.tableToOutput))
		{
			this.outputCurrentTable = true;
		}
		else
		{
			this.outputCurrentTable = false;
		}			
	}	

	public void startRow( IRowContent row )
	{
		assert row != null;
		
		if ( isRowInFooter( row ) || isRowInHeaderExceptFirstHeader(row) || outputCurrentTable!=true)
			writeData = false;		
		
		// rowID will be 1 for the first data row, before printing the data row we need to print the datatype
		// printDatatypeInSecondRow will be called only once
		if ( showDatatypeInSecondRow && row.getRowID()==1)			
			printDatatypeInSecondRow(row);				
		
		currentColumn = 0;		
	}
	
	public void startText( ITextContent text )
	{				
		if ( isHidden(text.getStyle()) )
		{
			logger.log( Level.FINE,"Skipping Hidden text" );
			return;
		}
		
		logger.log( Level.FINE,"Start text" );
		String textValue = text.getText( );
		
		if ( writeData )
		{
			writer.text( textValue, delimiter, replaceDelimiterInsideTextWith );
			currentColumn++;			
		}
	}
	
	
	public void endCell( ICellContent cell )
	{
		if ( isHidden(cell.getStyle()))
		{
			logger.log( Level.FINE,"Skipping Hidden cell" );
			return;
		}		
		
		if ( ( currentColumn < totalColumns )&& writeData )
		{
			writer.closeTag( delimiter );
		}		
	}
	
	public void endRow( IRowContent row )
	{		
		if ( writeData )
			writer.closeTag( CSVTags.TAG_CR );
		
		writeData = true;
	}	
	
	private boolean isHidden(IStyle style)
	{		
		String format=style.getVisibleFormat();
		
		if ( format != null && ( format.indexOf( EngineIRConstants.FORMAT_TYPE_VIEWER ) >= 0 || format.indexOf( BIRTConstants.BIRT_ALL_VALUE ) >= 0 ) )
		{
			return true;
		}
		else
		{
			return false;
		}		
	}
	
	private boolean isRowInFooter( IRowContent row )
	{
		IElement parent = row.getParent( );
		if ( !( parent instanceof IBandContent ) )
		{
			return false;
		}
		
		IBandContent band = ( IBandContent )parent;
		if ( band.getBandType( ) == IBandContent.BAND_FOOTER )
		{
			return true;
		}
		return false;
	}
	
	private boolean isRowInHeaderExceptFirstHeader( IRowContent row )
	{
		if(isFirstPage)
			return false;
		
		IElement parent = row.getParent( );
		if ( !( parent instanceof IBandContent ) )
		{
			return false;
		}
		
		IBandContent band = ( IBandContent )parent;
		if ( band.getBandType( ) == IBandContent.BAND_HEADER )
		{
			return true;
		}
		
		return false;
	}
	
	private void printDatatypeInSecondRow(IRowContent row)
	{		
		if(writeData)
		{			
			// ArrayList of columns used in Table in the same order as they appear in Report Design
			ArrayList<String> columnNamesInTableOrder = new ArrayList<String> ();
			
			// HashMap to contain ResultSetMetaData column and its respective DataType
			HashMap<String,String> resultSetMetaDatacolumnsWithDataType = new HashMap<String, String>();
			
			ReportDesignHandle reportDesignHandle=report.getDesign().getReportDesign();
			
			Object obj=reportDesignHandle.getElementByID(row.getInstanceID().getComponentID());
			
			RowHandle rowHandle=null;
			
			if(obj instanceof RowHandle)
			{
				rowHandle=(RowHandle)obj;
			}
			else
			{
				return; //not a row handle, nothing to do
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<CellHandle> cells= (ArrayList<CellHandle>)rowHandle.getCells().getContents();
			
			for(CellHandle cellHandle:cells)
			{				
				Cell cell=(Cell)cellHandle.getElement();			
				
				@SuppressWarnings("rawtypes")
				ArrayList cellContents=(ArrayList)cell.getSlot(0).getContents();
				
				// Currently hard coded to get the first content only
				
				if(cellContents.get(0) instanceof DataItem)
				{
					DataItem cellDataItem=(DataItem)cellContents.get(0);									
					columnNamesInTableOrder.add((String)cellDataItem.getLocalProperty(report.getDesign()
							.getReportDesign().getModule(), IDataItemModel.RESULT_SET_COLUMN_PROP));					
				}
			}
			
			// fetching all data sets in report
			
			@SuppressWarnings("unchecked")
			ArrayList<OdaDataSetHandle> odaDataSetHandles=(ArrayList<OdaDataSetHandle>)report.getDesign()
					.getReportDesign().getAllDataSets();
			
			for(OdaDataSetHandle odaDataSetHandle:odaDataSetHandles)
			{
				OdaDataSet odaDataSet=(OdaDataSet)odaDataSetHandle.getElement();
				@SuppressWarnings("unchecked")
				ArrayList<OdaResultSetColumn> odaResultSetColumns=(ArrayList<OdaResultSetColumn>)odaDataSet
						.getLocalProperty(report.getDesign().getReportDesign().getModule(), "resultSet");
				for(OdaResultSetColumn odaResultSetColumn:odaResultSetColumns)
				{					
					resultSetMetaDatacolumnsWithDataType.put(odaResultSetColumn.getColumnName(), 
							odaResultSetColumn.getDataType());
				}
			}
			
			// printing the datatype in the same order as column appearing in the report
			for(int i=0;i<columnNamesInTableOrder.size();i++)
			{
				String dataType=resultSetMetaDatacolumnsWithDataType.get(columnNamesInTableOrder.get(i));
				
				if(dataType != null)
					writer.text(dataType,delimiter, replaceDelimiterInsideTextWith);
				
				if(i < columnNamesInTableOrder.size()-1)
					writer.closeTag(delimiter);
				else
					writer.closeTag(CSVTags.TAG_CR);
			}
	
		}		
	}
	
}
