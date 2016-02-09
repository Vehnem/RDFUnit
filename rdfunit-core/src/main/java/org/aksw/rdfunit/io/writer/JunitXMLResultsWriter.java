package org.aksw.rdfunit.io.writer;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.rdfunit.model.impl.results.DatasetOverviewResults;
import org.aksw.rdfunit.model.interfaces.results.TestExecution;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * <p>Abstract JunitXMLResultsWriter class.</p>
 *
 * @author Martin Bruemmer
 *         Writes results in JUnit XML format
 * @since 11/14/13 1:04 PM
 * @version $Id: $Id
 */
public abstract class JunitXMLResultsWriter extends AbstractRDFWriter implements RDFWriter  {
	protected final TestExecution testExecution;
	private final OutputStream outputStream;

    /**
     * <p>Constructor for JunitXMLResultsWriter.</p>
     *
     * @param outputStream a {@link java.io.OutputStream} object.
     */
    public JunitXMLResultsWriter(TestExecution testExecution, OutputStream outputStream) {
        super();
        this.testExecution = testExecution;
        this.outputStream = outputStream;
    }

    /**
     * <p>Constructor for JunitXMLResultsWriter.</p>
     *
     * @param filename a {@link java.lang.String} object.
     */
    public JunitXMLResultsWriter(TestExecution testExecution, String filename) {
        this(testExecution, RDFStreamWriter.getOutputStreamFromFilename(filename));
    }

    /** {@inheritDoc} */
    @Override
    public void write(QueryExecutionFactory qef) throws RDFWriterException {
  
        try {
            // TODO not efficient StringBuilder.toString().getBytes()
            outputStream.write(getHeader().toString().getBytes("UTF8"));
            outputStream.write(getTestExecutionStats().toString().getBytes("UTF8"));
            outputStream.write(getTestExecutionResults().toString().getBytes("UTF8"));
            outputStream.write(getFooter().toString().getBytes("UTF8"));
            outputStream.close();
        } catch (IOException e) {
            throw new RDFWriterException("Cannot write XML", e);
        }
    }

    /**
     * <p>getResultsHeader.</p>
     *
     * @return a {@link java.lang.StringBuffer} object.
     */
    protected abstract StringBuffer getResultsHeader();

    /**
     * <p>getResultsList.</p>
     *
     * @param qef a {@link org.aksw.jena_sparql_api.core.QueryExecutionFactory} object.
     * @param testExecutionURI a {@link java.lang.String} object.
     * @return a {@link java.lang.StringBuffer} object.
     * @throws org.aksw.rdfunit.io.writer.RDFWriterException if any.
     */
    protected abstract StringBuffer getResultsList()  throws RDFWriterException;

    private StringBuffer getHeader() {
        StringBuffer header = new StringBuffer();
        header.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        return header;
    }

    private StringBuffer getFooter() {
        return new StringBuffer("</testsuite>");
    }
    
    /**
     * Create <testsuite> element containing test execution stats
     * @param qef
     * @param testExecution
     * @return
     * @throws RDFWriterException
     */

    private StringBuffer getTestExecutionStats() throws RDFWriterException {
        StringBuffer stats = new StringBuffer();
        stats.append("<testsuite name=\"").append(testExecution.getTestExecutionUri()).append("\" ");
  
        DatasetOverviewResults dor = testExecution.getDatasetOverviewResults();
        stats.append("timestamp=\"").append(dor.getEndTime()).append("\" ");
        String length = testLength(dor.getStartTime(), dor.getEndTime());
        if(length!=null) {
        	stats.append("time=\"").append(length).append("\" ");
        }
        stats.append("tests=\"").append(dor.getTotalTests()).append("\" ");
        stats.append("failures=\"").append(dor.getFailedTests()).append("\" ");
        stats.append("errors=\"").append(dor.getTimeoutTests()+dor.getErrorTests()).append("\" ");
        stats.append("package=\"").append(testExecution.getTestedDatasetUri()).append("\"");
        stats.append(">\n");
        return stats;
    }
    
    private String testLength(XSDDateTime datetimeStart, XSDDateTime datetimeEnd) {
    	long diff = datetimeEnd.asCalendar().getTimeInMillis() - datetimeStart.asCalendar().getTimeInMillis();
    	return String.format("%02d:%02d:%02d", 
    		    TimeUnit.MILLISECONDS.toHours(diff),
    		    TimeUnit.MILLISECONDS.toMinutes(diff) - 
    		    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
    		    TimeUnit.MILLISECONDS.toSeconds(diff) - 
    		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
    }

    private StringBuffer getTestExecutionResults() throws RDFWriterException {
        StringBuffer results = new StringBuffer();
        results.append(getResultsList());
        return results;
    }
}
