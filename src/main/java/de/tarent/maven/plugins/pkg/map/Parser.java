/**
 * 
 */
package de.tarent.maven.plugins.pkg.map;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * A parser for the package maps XML document.
 * 
 * <p>When finished successfully it will contain a map of all parsed
 * distributions and their corresponding package map.</p> 
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 *
 */
class Parser
  {
    HashMap/*<String, Mapping>*/ mappings = new HashMap();
    
    Parser(URL packageMapDocument, URL auxMapDocument)
      throws Exception
    {
      // Initialize the XML parsing part.
      Parser.State s = new State(packageMapDocument);
      
      s.nextMatch("package-maps");
      parsePackageMaps(s);
      
      if (auxMapDocument != null)
        {
          s = new State(packageMapDocument);
          
          s.nextMatch("package-maps");
          parsePackageMaps(s);
        }
      
    }
    
    private void handleInclude(Parser.State currentState, String includeUrl) throws Exception
    {
      try
      {
        // Automagically handles relative and absolute URLs.
        URL url = new URL(currentState.url, includeUrl);
        Parser.State s = new State(url);
        
        s.nextMatch("package-maps");
        parsePackageMaps(s);
        
      }
      catch (MalformedURLException mfue)
      {
        throw new Exception("URL in <include> tag is invalid '" + includeUrl + "'");
      }
    }
    
    private void parsePackageMaps(Parser.State s) throws Exception
    {
      s.nextMatch("version");
      
      String vc = s.nextElement();
      if (Double.parseDouble(vc) != 1.0)
        throw new Exception("unsupported document: document version " + vc + " is not supported");
     
      s.nextElement();
      while (s.token != null)
        {
          if (s.peek("distro"))
          {
            parseDistro(s);
          }
          else if (s.peek("include"))
            {
              handleInclude(s, s.nextElement());
              s.nextElement();
            }
          else
            throw new Exception("malformed document: unexpected token " + s.token);
        }
      
    }
    
    private void parseDistro(Parser.State s) throws Exception
    {
      s.nextMatch("id");
      Mapping distroMapping = getMappingImpl(s.nextElement());
      s.nextMatch("label");
      distroMapping.label = s.nextElement();
      
      s.nextElement();
      // Either "inherit" or "packaging".
      if (s.peek("inherit"))
        distroMapping.parent = s.nextElement();
      else if(s.peek("packaging"))
        distroMapping.packaging = s.nextElement();
      else 
        throw new Exception("malformed document: unexpected token " + s.token);
      
      s.nextElement();
      // debian naming parameter is optional
      if (s.peek("debianNaming"))
        {
          distroMapping.debianNaming = Boolean.valueOf(s.nextElement().toString());
          s.nextElement();
        }
      
      // Default bin (scripts) path is optional
      if (s.peek("defaultBinPath"))
        {
          distroMapping.defaultBinPath = s.nextElement();
          s.nextElement();
        }
      
      // Default jar path is optional
      if (s.peek("defaultJarPath"))
        {
          distroMapping.defaultJarPath = s.nextElement();
          s.nextElement();
        }

      // Default JNI path is optional
      if (s.peek("defaultJNIPath"))
        {
          distroMapping.defaultJNIPath = s.nextElement();
          s.nextElement();
        }
      
      // Default dependency line is optional
      if (s.peek("defaultDependencyLine"))
        {
          distroMapping.defaultDependencyLine = s.nextElement();
          s.nextElement();
        }
      
      if (s.peek("map"))
        {
          parseMap(s, distroMapping);
        }
      
    }
    
    private void parseMap(Parser.State s, Mapping distroMapping) throws Exception
    {
      s.nextElement();
      while (s.peek("entry"))
        {
          parseEntry(s, distroMapping);
        }
    }

    private void parseEntry(Parser.State s, Mapping distroMapping) throws Exception
    {
      String artifactSpec;
      String dependencyLine;
      
      s.nextMatch("artifactSpec");
      dependencyLine = getArtifactId(artifactSpec = s.nextElement());
      
      s.nextElement();
      if (s.peek("ignore"))
        {
          distroMapping.putEntry(artifactSpec, Entry.IGNORE_ENTRY);
          s.nextElement();
          return;
        }
      else if (s.peek("bundle"))
        {
          distroMapping.putEntry(artifactSpec, Entry.BUNDLE_ENTRY);
          s.nextElement();
          return;
        }
      else if (s.peek("dependencyLine"))
        {
          dependencyLine = s.nextElement();
          s.nextElement();
        }
      
      boolean isBootClaspath = false;
      if (s.peek("boot"))
        {
          isBootClaspath = true;
          s.nextElement();
        }
      
      HashSet jarFileNames = new HashSet();
      if (s.peek("jars"))
        {
          parseJars(s, jarFileNames);
        }
      
      distroMapping.putEntry(artifactSpec, new Entry(artifactSpec, dependencyLine, jarFileNames, isBootClaspath));
    }
    
    /**
     * Returns the artifactId of an artifactSpec string. Eg. "foo" from "org.baz:foo". 
     * 
     * @param artifactSpec
     * @return
     */
    private String getArtifactId(String artifactSpec)
    {
      return artifactSpec.substring(artifactSpec.indexOf(':') + 1);
    }

    private void parseJars(Parser.State s, HashSet jarFileNames) throws Exception
    {
      s.nextElement();
      while (s.peek("jar"))
        {
          jarFileNames.add(s.nextElement());
          s.nextElement();
        }
    }

    Mapping getMapping(String distro)
    {
      Mapping m = (Mapping) mappings.get(distro);
      
      if (m == null)
        mappings.put(distro, m = new Mapping(distro));
      else if (m.parent != null)
        {
          return new Mapping(m, getMapping(m.parent));
        }
      
      return m;
    }
    
    private Mapping getMappingImpl(String distro)
    {
      Mapping m = (Mapping) mappings.get(distro);
      
      if (m == null)
        mappings.put(distro, m = new Mapping(distro));
      
      return m;
    }
    
    private static class State
    {
      String token;
      
      XmlPullParser parser;
      
      URL url;
      
      State(URL url) throws Exception
      {
        parser = new MXParser();
        
        this.url = url;
        
        try
        {
          parser.setInput(url.openStream(), null);
        }
        catch (XmlPullParserException xmlppe)
        {
          throw new Exception("XML document malformed");
        }
        catch (IOException ioe)
        {
          throw new Exception("I/O error when accessing XML document: " + url);
        }

      }
      
      String nextElement() throws Exception
      {
        do
          {
            try 
            {
              switch (parser.next())
              {
                case XmlPullParser.START_TAG:
//                  System.err.println("start: " + parser.getName());
                  return token = parser.getName();
                case XmlPullParser.END_TAG:
//                  System.err.println("end");
                  continue;
                case XmlPullParser.END_DOCUMENT:
                  return token = null;
                case XmlPullParser.TEXT:
                  // We don't care about whitespace characters.
                  if (parser.isWhitespace())
                    continue;
                  
//                  System.err.println("text: " + parser.getText());
                  
                  return token = parser.getText();
              }
            }
            catch (XmlPullParserException xmlppe)
            {
              throw new Exception("XML document malformed", xmlppe);
            }
            catch (IOException ioe)
            {
              throw new Exception("I/O error when accessing XML document: " + url, ioe);
            }
          }
        while (true);
      }

      private void nextMatch(String expected) throws Exception
      {
        nextElement();
        if (!expected.equals(token))
          throw new Exception("malformed document: expected " + expected + " got '" + token + "'");
      }

      private boolean peek(String expected)
      {
        return expected.equals(token);
      }
      
    }
    
    static class Exception extends java.lang.Exception
    {
      private static final long serialVersionUID = - 8872495978331881464L;

      Exception(String msg, Throwable cause)
      {
        super(msg, cause);
      }

      Exception(String msg)
      {
        super(msg);
      }
      
    }
    
  }