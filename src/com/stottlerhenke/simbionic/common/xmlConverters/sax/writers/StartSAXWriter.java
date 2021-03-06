

/*
 * Class automatically generated using XSLT translator
 * See Taskguide/xslt/Readme.doc describing how to run tthe XSLT translator and
 * an explanation of the generated code.
 *
 
 <pre> 
   &lt;xsd:all /> 
   &lt;xsd:element name="id" type="xsd:integer" /> 
   &lt;xsd:element name="type" type="xsd:integer" /> 
   &lt;xsd:element name="connectors" type="ConnectorGroup" /> 
  &lt;/xsd:all> 
   </pre>
*/


package com.stottlerhenke.simbionic.common.xmlConverters.sax.writers;
import com.stottlerhenke.simbionic.common.xmlConverters.model.*;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.Parser;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.StackParser;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.readers.*;
import com.stottlerhenke.simbionic.common.xmlConverters.sax.writers.Utils;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
    

public class StartSAXWriter  {
 
  
  /** 
   * write the given taskguide object to the given xml file
   * 
   * @param dmObject -- object to be writen
   * @param writer -- xml output file
   * @param indent -- indent used to generate the xml tags associated with the input object
  **/
  
  public static void write (com.stottlerhenke.simbionic.common.xmlConverters.model.Start dmObject, PrintWriter writer, int indent) {
   
    Utils.writeField(StartSAXReader.id,dmObject.getId(),writer,indent+1);
     
    Utils.writeField(StartSAXReader.type,dmObject.getType(),writer,indent+1);
     List<com.stottlerhenke.simbionic.common.xmlConverters.model.Connector>  connectors = (List<com.stottlerhenke.simbionic.common.xmlConverters.model.Connector>)dmObject.getConnectors();
      if (connectors != null && !connectors.isEmpty()) {
        Utils.writeStartTag(StartSAXReader.connectors,writer,indent+1);
        ConnectorGroupSAXWriter.write(connectors, writer,indent+2);
        Utils.writeEndTag(StartSAXReader.connectors,writer,indent+1);
      }
      
  }




 } 
 
