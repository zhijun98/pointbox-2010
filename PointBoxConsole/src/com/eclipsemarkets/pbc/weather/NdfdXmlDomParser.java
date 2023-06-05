package com.eclipsemarkets.pbc.weather;

import java.io.BufferedReader;
import java.io.File;

// JAXP
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

// DOM

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;



/**
 *   @author Rueyfarn Wang
 */
class NdfdXmlDomParser
{
   private boolean printToConsole = false;
   private NdfdWebServiceParseResult parseResult;


   private void printToConsoleNewLine ( String text)
   {
      if ( printToConsole )
      {
         System.out.println (text);
      }
   }

   private void printToConsoleNewLine ( )
   {
      if ( printToConsole )
      {
         System.out.println ();
      }
   }

   private void printToConsole ( String text)
   {
      if ( printToConsole )
      {
         System.out.println (text);
      }
   }

   private void printNode(Node node, String indent)
   {
      switch (node.getNodeType())
      {
         case Node.DOCUMENT_NODE:
            printToConsoleNewLine("<xml version=\"1.0\">\n");
            // recurse on each child
            NodeList nodes = node.getChildNodes();
            if (nodes != null)
            {
               for (int i = 0; i < nodes.getLength(); i++)
               {
                  printNode(nodes.item(i), "");
               }
            }
            break;

         case Node.ELEMENT_NODE:
            String name = node.getNodeName();
            printToConsole(indent + "<" + name);


            if ("location".equals(name))
            {
               saveLocation(node);
            }
            else
            if ( "parameters".equals(name))
            {
               this.saveParameters(node);
            }
            else
            if ( "time-layout".equals(name))
            {
               this.saveTimeLayout(node);
            }

            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++)
            {
               Node current = attributes.item(i);
               printToConsole(
                       " " + current.getNodeName()
                       + "=\"" + current.getNodeValue()
                       + "\"");
            }
            printToConsole(">");

            // recurse on each child
            NodeList children = node.getChildNodes();
            if (children != null)
            {
               for (int i = 0; i < children.getLength(); i++)
               {
                  printNode(children.item(i), indent + "  ");
               }
            }
            printToConsole("</" + name + ">");
            break;

         case Node.TEXT_NODE:
            printToConsole(node.getNodeValue());
            break;
      }
   }




   private void saveLocation(Node node)
   {
      Location loc = new Location();
      NodeList list = node.getChildNodes();
      for (int j = 0; j < list.getLength(); j++)
      {
         Node child = list.item(j);

         if (child.getNodeType() == Node.ELEMENT_NODE)
         {
            String name = child.getNodeName();
            printToConsoleNewLine("child node name = " + name);
            if (name.equals("location-key"))
            {
               Node textNode = child.getFirstChild();
               String location = textNode.getNodeValue();
               loc.location = location;
            }
            else if (name.equals("point"))
            {
               NamedNodeMap attributes = child.getAttributes();
               for (int i = 0; i < attributes.getLength(); i++)
               {
                  Node current = attributes.item(i);
                  if ("latitude".equals(current.getNodeName()))
                  {
                     loc.latitude = current.getNodeValue();
                  }
                  else if ("longitude".equals(current.getNodeName()))
                  {
                     loc.longitude = current.getNodeValue();
                  }
               }
            }
         }
      }

      parseResult.locationMap.put(loc.location, loc);
   }




   void saveParameters(Node node)
   {
      Parameters params = this.parseResult.getNewParametersInstance();

      NamedNodeMap attrs = node.getAttributes();
      Node item = attrs.getNamedItem("applicable-location");
      params.applicableLocation = item.getNodeValue();


      NodeList list = node.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
      {
         Node child = list.item(i);

         if (child.getNodeType() == Node.ELEMENT_NODE)
         {
            String name = child.getNodeName();
            printToConsoleNewLine("child node name = " + name);
            if (name.equals("temperature"))
            {
               saveTemperatures(child, params);
            }
         }
      }

      parseResult.parametersMap.put ( params.applicableLocation, params);
      printToConsoleNewLine ( "\n========================================\n");
      printToConsoleNewLine( "saving parameters : " + params.toString());
      printToConsoleNewLine ( "\n========================================\n");
   }



   private void saveTemperatures(Node tempNode, Parameters params)
   {
      Temperatures temps = null;
      NamedNodeMap attributes = tempNode.getAttributes();

      Node attr = attributes.getNamedItem("type");
      String attrName = attr.getNodeName();
      String attrValue = attr.getNodeValue();

      if (attrValue.equals("minimum"))
      {
         temps = params.minTemps;
      }
      else
      {
         temps = params.maxTemps;
      }

      Node item = attributes.getNamedItem("units");
      temps.units = item.getNodeValue();


      item = attributes.getNamedItem("time-layout");
      temps.timeLayout = item.getNodeValue();
      

      NodeList children = tempNode.getChildNodes();
      for (int k = 0; k < children.getLength(); k++)
      {
         Node valueNode = children.item(k);
         if (valueNode.getNodeType() == Node.ELEMENT_NODE)
         {
            String valueNodeName = valueNode.getNodeName();
            if (valueNodeName.equals("value"))
            {
               Node n = valueNode.getFirstChild();
               if (n.getNodeType() == Node.TEXT_NODE)
               {
                  String text = n.getNodeValue();
                  Integer iValue = Integer.valueOf(text);
                  temps.values.add(iValue);
               }
            }
         }
      }
   }


   void saveTimeLayout(Node node)
   {
       TimeLayout tl = new TimeLayout();
       NodeList nl = node.getChildNodes();
       boolean expectStartTime = true;
       Period period = null;
       boolean consecutiveStartValidTime = false;
       for ( int i = 0; i < nl.getLength(); i++ )
       {
          Node child = nl.item(i);
          if ( child.getNodeType() == Node.ELEMENT_NODE )
          {
             String name = child.getNodeName();
             if ( name.equals("layout-key"))
             {
                Node textNode = child.getFirstChild();
                if ( textNode.getNodeType() == Node.TEXT_NODE)
                {
                   tl.layoutKey = textNode.getNodeValue();
                }
             }
             else
             if ( name.equals("start-valid-time"))
             {
                if ( expectStartTime)
                {
                   period = new Period();
                   NamedNodeMap attrs = child.getAttributes();
                   Node periodName = attrs.getNamedItem("period-name");

                   if ( periodName != null )
                   {
                      period.name = periodName.getNodeValue();
                   }

                   expectStartTime = false;
                   Node textNode = child.getFirstChild();

                   if ( textNode.getNodeType() == Node.TEXT_NODE)
                   {
                      period.startTime = textNode.getNodeValue();
                   }
                }
                else
                {
                   // we have consecutive start-valid-time
                   consecutiveStartValidTime = true;
                   break;
                }
             }
             else
             if ( name.equals("end-valid-time"))
             {
                Node textNode = child.getFirstChild();
                if ( textNode.getNodeType() == Node.TEXT_NODE)
                {
                   period.endTime = textNode.getNodeValue();
                }
                tl.periodList.add(period);
                expectStartTime = true;
             }
          }
       }

       if ( consecutiveStartValidTime == false)
       {
          parseResult.timeLayoutMap.put ( tl.layoutKey, tl);
       }
       else
       {
          tl.cleanUp();
          tl = null;
       }

   }



    void parse()
   {
      String fileName = "nfdf14cities.xml";
      // Create File object from incoming file
      File xmlFile = new File(fileName);

      try
      {
         DocumentBuilderFactory factory =
                 DocumentBuilderFactory.newInstance();

         // Turn on validation, and turn off namespaces
         factory.setValidating(false);
         factory.setNamespaceAware(false);


         DocumentBuilder builder = factory.newDocumentBuilder();

         printToConsoleNewLine();
         printToConsoleNewLine("Passed in File         : " + fileName);
         printToConsoleNewLine("Object to Parse (File) : " + xmlFile);
         printToConsoleNewLine("Parser Implementation  : " + builder.getClass());
         printToConsoleNewLine();

         // Parse the do*****ent
         Document doc = builder.parse(xmlFile);

         // Print the do*****ent from the DOM tree and feed it an initial
         // indentation of nothing
         printNode(doc, "");


      }
      catch (ParserConfigurationException e)
      {
         System.out.println("The underlying parser does not support "
                 + "the requested features.");
         e.printStackTrace();
      }
      catch (FactoryConfigurationError e)
      {
         System.out.println("Error occurred obtaining Do*****ent Builder "
                 + "Factory.");
         e.printStackTrace();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }


   void parse(String xmlContent, NdfdWebServiceParseResult parseResult)
   {
      this.parseResult = parseResult;
      try
      {
         DocumentBuilderFactory factory =
                 DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(false);
         DocumentBuilder builder = factory.newDocumentBuilder();
         InputSource xmlIS = new InputSource(new StringReader(xmlContent));
         Document doc = builder.parse(xmlIS);
         printNode(doc, "");
      }
      catch (ParserConfigurationException e)
      {
         System.out.println("The underlying parser does not support "
                 + "the requested features.");
         e.printStackTrace();
      }
      catch (FactoryConfigurationError e)
      {
         System.out.println("Error occurred obtaining Do*****ent Builder "
                 + "Factory.");
         e.printStackTrace();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }





static String getFileContent(File aFile)
{

    StringBuilder contents = new StringBuilder();

    try
    {
      BufferedReader input =  new BufferedReader(new FileReader(aFile));
      try
      {
        String line = null;
        while (( line = input.readLine()) != null)
        {
          contents.append(line);
          contents.append(System.getProperty("line.separator"));
        }
      }
      finally
      {
        input.close();
      }
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }

    return contents.toString();
  }


   static NdfdWebServiceParseResult parseSimpleWeatherData ()
   {
      NdfdXmlDomParser e = new NdfdXmlDomParser();

      //String fileName = "nfdf14cities.xml";
      String fileName = "NewFileAllCities.xml";
      File file = new File(fileName);
      String fileContent = getFileContent(file);

      NdfdWebServiceParseResult parseResult = new NdfdWebServiceParseResult("http://www.nws.noaa.gov/ndfd/");
      e.parse(fileContent, parseResult);

      parseResult.updateParametersWithCity();
      parseResult.printResult();
      return parseResult;
   }


   static NdfdWebServiceParseResult parseWeatherServiceXMLResult ( String xmlContent)
   {
      NdfdXmlDomParser e = new NdfdXmlDomParser();
      NdfdWebServiceParseResult parseResult = new NdfdWebServiceParseResult("http://www.nws.noaa.gov/ndfd/");
      e.parse(xmlContent, parseResult);
      parseResult.updateParametersWithCity();
      //parseResult.printResult();
      return parseResult;
   }

  
}
