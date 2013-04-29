/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.wms;

import biz.source_code.base64Coder.Base64Coder;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;


import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Represents a remote WMS Service.
 *
 * @author Chris Hodgson chodgson@refractions.net
 */
public class WMService {
    private static final Logger LOGGER = Logger.getLogger(WMService.class);
    
    public static final String WMS_1_0_0 = "1.0.0";

    public static final String WMS_1_1_0 = "1.1.0";
    
    public static final String WMS_1_1_1 = "1.1.1";
    
    public static final String WMS_1_3_0 = "1.3.0";
    
    
  private String serverUrl;
  private String wmsVersion = "";
  private Capabilities cap;
  
  /**
   * Constructs a WMService object from a server URL.
   * @param serverUrl the URL of the WMS server
   * @param wmsVersion 
   */
  public WMService( String serverUrl, String wmsVersion ) {
    this.serverUrl = serverUrl;   
    this.wmsVersion = wmsVersion;
    this.cap = null;
  }
  /**
   * Constructs a WMService object from a server URL.
   * @param serverUrl the URL of the WMS server
   */
  public WMService( String serverUrl ) {
    this.serverUrl = serverUrl;   
    this.cap = null;
  }

   /**
    * @throws IOException
    */
    public void initialize() throws IOException {
        initialize(false);
    }

    /**
     * Get a parser for the given version
     * @param version
     * @return
     */
    private IParser getParser(String version) {
        IParser parser;
        if (WMS_1_0_0.equals(version)) {
            parser = new ParserWMS1_0();
        } else if (WMS_1_1_0.equals(version)) {
            parser = new ParserWMS1_1();
        } else if (WMS_1_1_1.equals(version)) {
            parser = new ParserWMS1_1();
        } else if (WMS_1_3_0.equals(version)) {
            parser = new ParserWMS1_3();
        } else {
            parser = new ParserWMS1_1();
        }
        return parser;
    }

    /**
     * Gets the DOM Document hidden behind the given URL.
     * @param requestUrlString The URL as a String
     * @return The DOM Document containing the parsed GetCapabilities answer.
     * @throws IOException
     */
    private Document getDOMDocument(String requestUrlString) throws IOException{
            URL requestUrl = new URL( requestUrlString );
            URLConnection con = requestUrl.openConnection();
            if(requestUrl.getUserInfo() != null) {
                con.setRequestProperty("Authorization", "Basic " +
                        Base64Coder.encode(requestUrl.getUserInfo().getBytes()));
            }
            DOMParser domParser = new DOMParser();
            try {
                domParser.setFeature("http://xml.org/sax/features/validation", false);
                domParser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                domParser.parse(new InputSource(con.getInputStream()));
            } catch (SAXException ex) {
                LOGGER.error("Error during sax initialization", ex);
            } catch (IOException ex) {
                LOGGER.error("Error during while parsing the document", ex);
            }
            return domParser.getDocument();
    }

  /**
   * Connect to the service and get the capabilities.
   * This must be called before anything else is done with this service.
   * @param alertDifferingURL alert the user if a different GetMap URL is available
   * @throws IOException 
   */
	public void initialize(boolean alertDifferingURL) throws IOException {
	    // [UT]
	    String req = "SERVICE=WMS&REQUEST=GetCapabilities";
	    if( WMS_1_0_0.equals( wmsVersion) ){
	    	req = "SERVICE=WMS&VERSION=1.0.0&REQUEST=GetCapabilities";
	    } else if( WMS_1_1_0.equals( wmsVersion) ){
	    	req = "SERVICE=WMS&VERSION=1.1.0&REQUEST=GetCapabilities";
	    } else if ( WMS_1_1_1.equals( wmsVersion) ){
	    	req = "SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities";
	    } else if ( WMS_1_3_0.equals( wmsVersion) ){
	    	req = "SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities";
	    }
        if(!serverUrl.contains("?")){
            serverUrl = serverUrl + "?";
        } else if(serverUrl.charAt(serverUrl.length() -1) != '&') {
            serverUrl = serverUrl + "&";
        }
        String requestUrlString = this.serverUrl + req;
        Document doc = getDOMDocument(requestUrlString);
        wmsVersion = VersionFinder.findVersion(doc);
	    IParser parser = getParser(wmsVersion);
        try {
            cap = parser.parseCapabilities( this, doc );
            String url1 = cap.getService().getServerUrl();
            String url2 = cap.getGetMapURL();
            if(!url1.equals(url2)){
                //if the difference is only in credentials then use url1 else ask from user
                if(!new URL(url1).equals(new URL(url2)) && alertDifferingURL) {
                    int resp = showConfirmDialog(null, "Url of the getMap not found", null, YES_NO_OPTION);
                    if(resp == NO_OPTION) {
                        cap.setGetMapURL(url1);
                    }
                } else {
                    //changed 24.06.2011 (Wilfried Hornburg, LGLN) url1 --> url2; original: cap.setGetMapURL(url1);
                    //revert to url1, following Jukka's advice a discussion is on-going on JPP mailing list
                    cap.setGetMapURL(url1);
                }
            }
        } catch ( FileNotFoundException e ){
            LOGGER.error("WMS not found !", e);
            throw e;
        } catch (final WMSException e){
            LOGGER.error("WMS error !", e);
            throw e;
        } catch ( IOException e ) {
            LOGGER.error("WMS not found !", e);
            throw e;
        }
  }


  /**
   * Gets the url of the map service.
   * @return the url of the WMService
   */
  public String getServerUrl() {
    return serverUrl;
  }

  /**
   * Gets the title of the map service.
   * The service must have previously been initialized, otherwise null is returned.
   * @return the title of the WMService
   */
  public String getTitle() {
    return cap.getTitle();
  }

  /**
   * Gets the Capabilities for this service.
   * The service must have previously been initialized, otherwise null is returned.
   * @return a copy of the MapDescriptor for this service
   */
  public Capabilities getCapabilities() {
    return cap;
  }

  /**
   * Creates a new MapRequest object which can be used to retrieve a Map
   * from this service.
   * @return a MapRequest object which can be used to retrieve a map image
   *         from this service
   */
  	public MapRequest createMapRequest() {
  	    // [UT] 04.02.2005 changed
  	    MapRequest mr = new MapRequest( this );
  	    mr.setVersion( this.wmsVersion );
        return mr;
	}
      
  	public String getVersion(){
  	    return wmsVersion;
	}
}
