package com.mwumli.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
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

import android.os.Environment;

public class XMLOperation {

	public void saxCreateXML(List<HashMap<String, Object>> sourceList) throws IOException {
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
			handler.startElement("", "", "food", attr);

			int sourceListLen = sourceList.size();
			for (int i = 0; i < sourceListLen; i++) {

				HashMap<String, Object> item = sourceList.get(i);
				attr.clear();
				handler.startElement("", "", "item", attr);

				attr.clear();
				handler.startElement("", "", "date", attr);
				String date = (String) item.get("date");
				handler.characters(date.toCharArray(), 0, date.length());
				handler.endElement("", "", "date");

				attr.clear();
				handler.startElement("", "", "foodname", attr);
				String foodname = (String) item.get("foodname");
				handler.characters(foodname.toCharArray(), 0, foodname.length());
				handler.endElement("", "", "foodname");

				handler.endElement("", "", "item");
			}

			handler.endElement("", "", "food");
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

		saveFile(xmlWriter.toString());
		// return xmlWriter.toString();
	}

	public boolean saveFile(String data) throws IOException {

		File targetFile = new File(Environment.getExternalStorageDirectory().getCanonicalPath() + "/log/" + "bm.xml");
		targetFile.delete();
		FileOutputStream outStream = new FileOutputStream(targetFile);

		outStream.write(data.getBytes());
		outStream.close();
		return true;

	}// saveFile

	public List<HashMap<String, Object>> parseXMLWithPull(File file) {
		return null;
/*
		if (!file.exists()) {
			List<HashMap<String, Object>> sourceList = null;
			try {
				sourceList = initSourceList();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				saxCreateXML(sourceList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			List<HashMap<String, Object>> sourceList;
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
					if ("date".equals(nodename)) {
						date = xmlPullParser.nextText();
						item.put("date", date);
					} else if ("foodname".equals(nodename)) {
						foodname = xmlPullParser.nextText();
						item.put("foodname", foodname);
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
			return sourceList;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;*/
	}

}
