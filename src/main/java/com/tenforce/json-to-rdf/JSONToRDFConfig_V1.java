package com.tenforce.json-to-rdf;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONToRDFConfig_V1 {

    public static final String STOP_EXTRACTION_ERROR_HANDLING = "STOP_EXTRACTION";

    public static final String SKIP_CONTINUE_THIS_FILE_ERROR_HANDLING = "SKIP_CONTINUE_THIS_FILE";

    public static final String SKIP_CONTINUE_NEXT_FILE_ERROR_HANDLING = "SKIP_CONTINUE_NEXT_FILE";

    public static final String USE_INPUT_SYMBOLIC_NAME = "USE_INPUT_GRAPH_NAME";

    public static final String USE_FIXED_SYMBOLIC_NAME = "USE_SINGLE_SYMBOLIC_NAME";



    /**
     * Not used - left here for possible usage in future.
     */
    private Map<String, String> symbolicNameToBaseURIMap;

    /**
     * Not used - left here for possible usage in future.
     */
    private Map<String, String> symbolicNameToFormatMap;

    private int commitSize = 100000;

    private String vocab = "http://testruimte/";

    /**
     * Used to determine action if an exception is thrown during loading of a single file.
     */
    private String fatalErrorHandling = STOP_EXTRACTION_ERROR_HANDLING;

    /**
     * Not used - left here for possible usage in future.
     */
    private String errorHandling = SKIP_CONTINUE_THIS_FILE_ERROR_HANDLING;

    /**
     * Not used - left here for possible usage in future.
     */
    private String warningHandling = SKIP_CONTINUE_THIS_FILE_ERROR_HANDLING;

    /**
     * Policy for output graph naming.
     */
    private String outputNaming = USE_INPUT_SYMBOLIC_NAME;

    /**
     * If outputNaming == USE_FIXED_SYMBOLIC_NAME then this value specify name of output graph.
     */
    private String outputSymbolicName = null;

    public JSONToRDFConfig_V1() {
        this.symbolicNameToBaseURIMap = new LinkedHashMap<>();
        this.symbolicNameToFormatMap = new LinkedHashMap<>();
    }

    public Map<String, String> getSymbolicNameToBaseURIMap() {
        return symbolicNameToBaseURIMap;
    }

    public void setSymbolicNameToBaseURIMap(Map<String, String> symbolicNameToBaseURIMap) {
        this.symbolicNameToBaseURIMap = symbolicNameToBaseURIMap;
    }

    public Map<String, String> getSymbolicNameToFormatMap() {
        return symbolicNameToFormatMap;
    }

    public void setSymbolicNameToFormatMap(Map<String, String> symbolicNameToFormatMap) {
        this.symbolicNameToFormatMap = symbolicNameToFormatMap;
    }

    public String getVocab() {
	return vocab;
    }

    public void setVocab(String v) {
	vocab = v;
    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }

    public String getFatalErrorHandling() {
        return fatalErrorHandling;
    }

    public void setFatalErrorHandling(String fatalErrorHandling) {
        this.fatalErrorHandling = fatalErrorHandling;
    }

    public String getErrorHandling() {
        return errorHandling;
    }

    public void setErrorHandling(String errorHandling) {
        this.errorHandling = errorHandling;
    }

    public String getWarningHandling() {
        return warningHandling;
    }

    public void setWarningHandling(String warningHandling) {
        this.warningHandling = warningHandling;
    }

    public String getOutputNaming() {
        return outputNaming;
    }

    public void setOutputNaming(String graphNaming) {
        this.outputNaming = graphNaming;
    }

    public String getOutputSymbolicName() {
        return outputSymbolicName;
    }

    public void setOutputSymbolicName(String outputSymbolicName) {
        this.outputSymbolicName = outputSymbolicName;
    }

}
