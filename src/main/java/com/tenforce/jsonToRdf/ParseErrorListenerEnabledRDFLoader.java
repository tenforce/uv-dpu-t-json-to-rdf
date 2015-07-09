/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.tenforce.jsonToRdf;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.*;
import com.github.jsonldjava.sesame.*;

import info.aduna.io.GZipUtil;
import info.aduna.io.ZipUtil;

import java.util.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.ParseErrorLogger;



/**
 * Handles common I/O to retrieve and parse RDF.
 * 
 * @author James Leigh
 */
public class ParseErrorListenerEnabledRDFLoader {

    private final ParserConfig config;

    private final ValueFactory vf;

    private final String vocab;

    /**
     * @param config
     * @param vf
     * @param vocab
     */
    public ParseErrorListenerEnabledRDFLoader(ParserConfig config, ValueFactory vf, String vocab) {
        this.config = config;
        this.vf = vf;
	this.vocab = vocab;
    }

    /**
     * Parses RDF data from the specified file to the given RDFHandler.
     * 
     * @param file
     *        A file containing RDF data.
     * @param baseURI
     *        The base URI to resolve any relative URIs that are in the data
     *        against. This defaults to the value of {@link java.io.File#toURI()
     *        file.toURI()} if the value is set to <tt>null</tt>.
     * @param dataFormat
     *        The serialization format of the data.
     * @param rdfHandler
     *        Receives RDF parser events.
     * @throws IOException
     *         If an I/O error occurred while reading from the file.
     * @throws UnsupportedRDFormatException
     *         If no parser is available for the specified RDF format.
     * @throws RDFParseException
     *         If an error was found while parsing the RDF data.
     * @throws RDFHandlerException
     *         If thrown by the RDFHandler
     */
    public void load(File file, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler, ParseErrorListener parseErrorListener)
        throws IOException, RDFParseException, RDFHandlerException, JsonLdError
    {
        if (baseURI == null) {
            // default baseURI to file
            baseURI = file.toURI().toString();
        }
        if (dataFormat == null) {
            dataFormat = Rio.getParserFormatForFileName(file.getName());
        }

        InputStream in = new FileInputStream(file);
        try {
            load(in, baseURI, dataFormat, rdfHandler, parseErrorListener);
        }
        finally {
            in.close();
        }
    }

    /**
     * Parses the RDF data that can be found at the specified URL to the
     * RDFHandler.
     * 
     * @param url
     *        The URL of the RDF data.
     * @param baseURI
     *        The base URI to resolve any relative URIs that are in the data
     *        against. This defaults to the value of
     *        {@link java.net.URL#toExternalForm() url.toExternalForm()} if the
     *        value is set to <tt>null</tt>.
     * @param dataFormat
     *        The serialization format of the data. If set to <tt>null</tt>, the
     *        format will be automatically determined by examining the content
     *        type in the HTTP response header, and failing that, the file name
     *        extension of the supplied URL.
     * @param rdfHandler
     *        Receives RDF parser events.
     * @throws IOException
     *         If an I/O error occurred while reading from the URL.
     * @throws UnsupportedRDFormatException
     *         If no parser is available for the specified RDF format, or the RDF
     *         format could not be automatically determined.
     * @throws RDFParseException
     *         If an error was found while parsing the RDF data.
     * @throws RDFHandlerException
     *         If thrown by the RDFHandler
     */
    public void load(URL url, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler, ParseErrorListener parseErrorListener)
        throws IOException, RDFParseException, RDFHandlerException, JsonLdError
    {
        if (baseURI == null) {
            baseURI = url.toExternalForm();
        }

        URLConnection con = url.openConnection();

        // Set appropriate Accept headers
        if (dataFormat != null) {
            for (String mimeType : dataFormat.getMIMETypes()) {
                con.addRequestProperty("Accept", mimeType);
            }
        }
        else {
            Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
            List<String> acceptParams = RDFFormat.getAcceptParams(rdfFormats, true, null);
            for (String acceptParam : acceptParams) {
                con.addRequestProperty("Accept", acceptParam);
            }
        }

        InputStream in = con.getInputStream();

        if (dataFormat == null) {
            // Try to determine the data's MIME type
            String mimeType = con.getContentType();
            int semiColonIdx = mimeType.indexOf(';');
            if (semiColonIdx >= 0) {
                mimeType = mimeType.substring(0, semiColonIdx);
            }
            dataFormat = Rio.getParserFormatForMIMEType(mimeType);

            // Fall back to using file name extensions
            if (dataFormat == null) {
                dataFormat = Rio.getParserFormatForFileName(url.getPath());
            }
        }

        try {
            load(in, baseURI, dataFormat, rdfHandler, parseErrorListener);
        }
        finally {
            in.close();
        }
    }

    /**
     * Parses RDF data from an InputStream to the RDFHandler.
     * 
     * @param in
     *        An InputStream from which RDF data can be read.
     * @param baseURI
     *        The base URI to resolve any relative URIs that are in the data
     *        against.
     * @param dataFormat
     *        The serialization format of the data.
     * @param rdfHandler
     *        Receives RDF parser events.
     * @throws IOException
     *         If an I/O error occurred while reading from the input stream.
     * @throws UnsupportedRDFormatException
     *         If no parser is available for the specified RDF format.
     * @throws RDFParseException
     *         If an error was found while parsing the RDF data.
     * @throws RDFHandlerException
     *         If thrown by the RDFHandler
     */
    public void load(InputStream in, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler, ParseErrorListener parseErrorListener)
        throws IOException, RDFParseException, RDFHandlerException, JsonLdError
    {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in, 1024);
        }

        if (ZipUtil.isZipStream(in)) {
            loadZip(in, baseURI, dataFormat, rdfHandler, parseErrorListener);
        }
        else if (GZipUtil.isGZipStream(in)) {
            load(new GZIPInputStream(in), baseURI, dataFormat, rdfHandler, parseErrorListener);
        }
        else {
            loadInputStreamOrReader(in, baseURI, dataFormat, rdfHandler, parseErrorListener);
        }
    }

    /**
     * Parses RDF data from a Reader to the RDFHandler. <b>Note: using a Reader
     * to upload byte-based data means that you have to be careful not to destroy
     * the data's character encoding by enforcing a default character encoding
     * upon the bytes. If possible, adding such data using an InputStream is to
     * be preferred.</b>
     * 
     * @param reader
     *        A Reader from which RDF data can be read.
     * @param baseURI
     *        The base URI to resolve any relative URIs that are in the data
     *        against.
     * @param dataFormat
     *        The serialization format of the data.
     * @param rdfHandler
     *        Receives RDF parser events.
     * @throws IOException
     *         If an I/O error occurred while reading from the reader.
     * @throws UnsupportedRDFormatException
     *         If no parser is available for the specified RDF format.
     * @throws RDFParseException
     *         If an error was found while parsing the RDF data.
     * @throws RDFHandlerException
     *         If thrown by the RDFHandler
     */
    public void load(Reader reader, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler, ParseErrorListener parseErrorListener)
        throws IOException, RDFParseException, RDFHandlerException, JsonLdError
    {
        loadInputStreamOrReader(reader, baseURI, dataFormat, rdfHandler, parseErrorListener);
    }

    private void loadZip(InputStream in, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler, ParseErrorListener parseErrorListener)
        throws IOException, RDFParseException, RDFHandlerException, JsonLdError
    {
        ZipInputStream zipIn = new ZipInputStream(in);

        try {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                if (entry.isDirectory()) {
                    continue;
                }

                RDFFormat format = Rio.getParserFormatForFileName(entry.getName(), dataFormat);

                try {
                    // Prevent parser (Xerces) from closing the input stream
                    FilterInputStream wrapper = new FilterInputStream(zipIn) {

                        public void close() {
                        }
                    };
                    load(wrapper, baseURI, format, rdfHandler, parseErrorListener);

                }
                catch (RDFParseException e) {
                    String msg = e.getMessage() + " in " + entry.getName();
                    RDFParseException pe = new RDFParseException(msg, e.getLineNumber(), e.getColumnNumber());
                    pe.initCause(e);
                    throw pe;
                }
                finally {
                    zipIn.closeEntry();
                }
            } // end for
        }
        finally {
            zipIn.close();
        }
    }


   private void parseJSON(RDFHandler handler, Object json) throws JsonLdError {
       SesameTripleCallback callback = new SesameTripleCallback(handler);
       //JsonLdOptions options = new JsonLdOptions("http://data.opendatasupport.eu/raw/");
        if (json instanceof List) {
            HashMap<String,Object> newJson = new HashMap<String,Object>();
            newJson.put("datasets",json);
            json = newJson;
        }
	if (json instanceof Map) {
	    HashMap<String,Object> hm = new HashMap<String,Object>();
	    hm.put("@vocab", vocab); 
//            ((Map) json).put("@id","joost");
            ((Map) json).put("@context", hm); //"{\"@vocab\": \"http://testruimte/\"}");//getConfig().getJsonContext().toString());
//            ((Map) json).put("@type", "Product");
	}
        JsonLdProcessor.toRDF(json, callback);
    }


    /**
     * Adds the data that can be read from the supplied InputStream or Reader to
     * this repository.
     * 
     * @param inputStreamOrReader
     *        An {@link InputStream} or {@link Reader} containing RDF data that
     *        must be added to the repository.
     * @param baseURI
     *        The base URI for the data.
     * @param dataFormat
     *        The file format of the data.
     * @param rdfHandler
     *        handles all data from all documents
     * @throws IOException
     * @throws UnsupportedRDFormatException
     * @throws RDFParseException
     * @throws RDFHandlerException
     */
    private void loadInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
            RDFHandler rdfHandler, ParseErrorListener parseErrorListener)
        throws IOException, RDFParseException, RDFHandlerException, JsonLdError
    {
	Object jsonObject = JsonUtils.fromInputStream((InputStream) inputStreamOrReader);
	parseJSON(rdfHandler, jsonObject);
	return;
     }
}
