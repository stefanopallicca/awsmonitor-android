package net.stefanopallicca.android.awsmonitor;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GsnServer {
	private String url = "";
	private int port = 22001;
	
	private String name = "";
	public ArrayList<VirtualSensor> virtualSensors = new ArrayList<VirtualSensor>();;
	
	public GsnServer(String url, int port){
		this.url = url;
		this.port = port;
	}
	
	public void getSummary(){
		try {
			URL url = new URL("http://"+this.url+":"+this.port+"/gsn");
			URLConnection conn = url.openConnection();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(conn.getInputStream());
			
			Element gsn = (Element) doc.getElementsByTagName("gsn").item(0);
			this.name = gsn.getAttribute("name");

			NodeList nodes = doc.getElementsByTagName("virtual-sensor");
			for (int i = 0; i < nodes.getLength(); i++) {
				this.virtualSensors.add(new VirtualSensor((Element) nodes.item(i)));
				/*Element element = (Element) nodes.item(i);
				NodeList title = element.getElementsByTagName();
				Element line = (Element) title.item(0);
				phoneNumberList.add(line.getTextContent());*/
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	public class VirtualSensor {
		private String name = "";
		public ArrayList<VSField> fields = new ArrayList<VSField>();
		
		public VirtualSensor(Element e){
			this.name = e.getAttribute("name");
			NodeList nodes = e.getElementsByTagName("field");
			for (int i = 0; i < nodes.getLength(); i++) {
				this.fields.add(new VSField((Element) nodes.item(i)));
			}
		}
		
		public String getName() {
			return name;
		}

		public ArrayList<VSField> getFields() {
			return fields;
		}
		
		public class VSField{
			private String name = "";
			private String type = "";
			
			public VSField(Element e){
				this.name = e.getAttribute("name");
				this.type = e.getAttribute("type");
			}

			public String getName() {
				return name;
			}

			public String getType() {
				return type;
			}
			
		}
	}
	
	
}
