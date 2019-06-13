package com.springframework.handler.resolver;

import java.io.IOException;
import java.io.InputStream;
 
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
 
public class XMLConfiguration {
	private static final String CONFIG_PATH = "ApplicationContext.xml";
	
	public String getScanBasePackage(){
		String basePackageString = null;
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(CONFIG_PATH);
		SAXReader saxReader = new SAXReader();
		
		try {
			Document document = saxReader.read(is);
			
			if(null != document){
				Element rootElement = document.getRootElement();
				Element element = rootElement.element("component-scan");
				//判断是否配置扫描
				if(null != element){
					basePackageString = element.attributeValue("base-package");
				}
			}else{
				System.out.println("获取配置文件失败！");
			}
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return basePackageString;
	}
}
