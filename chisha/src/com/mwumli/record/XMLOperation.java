package com.mwumli.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Environment;
import android.widget.Toast;

public class XMLOperation {

	//把list转化成XML
	public void saxCreateXML(List<HashMap<String, Object>> sourceList)  {
		StringWriter xmlWriter = new StringWriter();
		try {
			SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			TransformerHandler handler = factory.newTransformerHandler();

			Transformer transformer = handler.getTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

			StreamResult result = new StreamResult(xmlWriter);
			handler.setResult(result);

			handler.startDocument();
			AttributesImpl attr = new AttributesImpl();

			attr.clear();
			attr.addAttribute("", "", "author", "", "mwumli");
			attr.addAttribute("", "", "date", "", "2014-07-27");
			handler.startElement("", "", "dianzan", attr);

			int sourceListLen = sourceList.size();
		
			for (int i = 0; i < sourceListLen; i++) {

				HashMap<String, Object> item = sourceList.get(i);
				attr.clear();
				handler.startElement("", "", "item", attr);

				attr.clear();
				handler.startElement("", "", "foodIndex", attr);
				String date = (String) item.get("foodIndex").toString();
				handler.characters(date.toCharArray(), 0, date.length());
				handler.endElement("", "", "foodIndex");

				attr.clear();
				handler.startElement("", "", "zan", attr);
				String foodname = (String) item.get("zan").toString();
				handler.characters(foodname.toCharArray(), 0, foodname.length());
				handler.endElement("", "", "zan");

				handler.endElement("", "", "item");
			}

			handler.endElement("", "", "dianzan");
			handler.endDocument();

		} catch (TransformerFactoryConfigurationError e) { // SAXTransformerFactory.newInstance
			e.printStackTrace();
		} catch (TransformerConfigurationException e) { // factory.newTransformerHandler
			e.printStackTrace();
		} catch (IllegalArgumentException e) { // transformer.setOutputProperty
			e.printStackTrace();
		} catch (SAXException e) { // handler.startDocument
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			saveFile(xmlWriter.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return xmlWriter.toString();
	}

	public List<HashMap<String, Object>> initSourceList() {
		
		List<HashMap<String, Object>> sourceList = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < 9; i++) {
			HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
			itemHashMap.put("foodIndex", i);
			itemHashMap.put("zan", false);
			sourceList.add(itemHashMap);
		}
		
		return sourceList;
	}

	//解析文件为一个  list 
	public List<HashMap<String, Object>> parseXMLWithPull(File file) {
		
		List<HashMap<String, Object>> sourceList = null;
		//文件不存在，初始化文件，并返回初始化list
		if (!file.exists()) {
			
			sourceList = initSourceList();

			saxCreateXML(sourceList);
			return sourceList;
		}

		//如果文件存在，解析出list，并返回
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			HashMap<String, Object> item;

			sourceList = new ArrayList<HashMap<String, Object>>();
			xmlPullParser.setInput(new FileInputStream(file), "UTF-8");
			;
			int eventType = xmlPullParser.getEventType();

			String date = null;
			String foodname = null;
			String web_icon = null;

			item = new HashMap<String, Object>();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodename = xmlPullParser.getName();
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if ("zan".equals(nodename)) {
						date = xmlPullParser.nextText();
						item.put("zan", date);
					} else if ("foodIndex".equals(nodename)) {
						foodname = xmlPullParser.nextText();
						item.put("foodIndex", foodname);
					}
					break;
				case XmlPullParser.END_TAG: {
					if ("item".equals(nodename)) {
						sourceList.add(item);
						item = new HashMap<String, Object>();
					}
					break;
				}
				default:
					break;
				}
				eventType = xmlPullParser.next();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sourceList;
	}

	public boolean saveFile(String data) throws IOException {

		File targetFile = new File(Environment.getExternalStorageDirectory().getCanonicalPath() + "/chisha/" + "dianzan.xml");
		targetFile.delete();
		FileOutputStream outStream = new FileOutputStream(targetFile);

		outStream.write(data.getBytes());
		outStream.close();
		return true;

	}// saveFile
}
