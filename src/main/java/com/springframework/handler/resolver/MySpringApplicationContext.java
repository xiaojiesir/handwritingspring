package com.springframework.handler.resolver;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import com.springframework.annotation.MyAutowired;
import com.springframework.annotation.MyController;
import com.springframework.annotation.MyRepository;
import com.springframework.annotation.MyService;
import com.springframework.handler.resolver.XMLConfiguration;
 
 
public class MySpringApplicationContext {
	//定义一个线程安全的集合，存放类对象
	public static List<Class<?>> basePackageMappingToClass = Collections.synchronizedList(new ArrayList<>());
	//定义一个线程安全的集合，存储别名和类的实例映射
	public static Map<String, Object> aliasMappingInstance = Collections.synchronizedMap(new HashMap<>());
	
	private XMLConfiguration xmlConfiguration = new XMLConfiguration();
	
	public MySpringApplicationContext(){
		//扫描对应包下的被标记的类，并将被标记的类放在basePackageMappingToClass集合中
		scanBasePackage(xmlConfiguration.getScanBasePackage());
		//类的实例化，分有别名和没别名
		initAliasMappingToInstance();
		//完成对象之间的依赖注入操作
		initInstanceInjectionObject();
	}
	
	/**
	 * 完成对象之间的依赖对象装配
	 */
	private void initInstanceInjectionObject() {
		if(basePackageMappingToClass.size() == 0){
			return;
		}
		for(int i = 0;i < basePackageMappingToClass.size();i++){
			Class<?> clazz =basePackageMappingToClass.get(i);
			String instanceAlias = getBeanAlias(clazz);
			//获取需要依赖注入的对象
			Object needInjectionObj = aliasMappingInstance.get(instanceAlias);
			
			//获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
			Field[] fields = clazz.getDeclaredFields();
			
			if(null != fields && fields.length > 0){
				for(int j = 0;j < fields.length;j++){
					//定义存储需要装配的依赖对象
					Object injectObj = null;
					Field field = fields[j];
					//判断是否有@MyAutowired注解
					if(field.isAnnotationPresent(MyAutowired.class)){
						MyAutowired myAutowired = field.getAnnotation(MyAutowired.class);
						//判断value是否为空字符串
						if(!"".equals(myAutowired.value())){
							//装配别名名称
							String alias = myAutowired.value();
							//获取到装配的依赖对象
							injectObj = aliasMappingInstance.get(alias);
						}else{
							//按照默认方式装配依赖对象
							Class<?> fieldType = field.getType();
							//获取容器中所有实例的类型
							Collection<Object> values = aliasMappingInstance.values();
							Iterator<Object> iterator = values.iterator();
							while(iterator.hasNext()){
								Object object = iterator.next();
								//判断是否为同一个类型
								if(fieldType.isAssignableFrom(object.getClass())){
									//找到需要装配依赖对象的实例
									injectObj = object;
									break;
								}
							}
						}
					}
					//设置字段访问权限
					field.setAccessible(true);
					try {
						//把依赖对象装配到响应的实例中
						field.set(needInjectionObj, injectObj);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	//完成别名与对象实例映射对应关系
	private void initAliasMappingToInstance() {
		if(basePackageMappingToClass.size() == 0){
			return;
		}
		for(int i = 0;i < basePackageMappingToClass.size();i++){
			Class<?> clazz = basePackageMappingToClass.get(i);
			String alias = getBeanAlias(clazz);
			
			try {
				aliasMappingInstance.put(alias,clazz.newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取一个首字母小写的类名称
	 * @param className
	 * @return
	 */
	private String getLowerClassName(String className){
		String start = className.substring(0, 1).toLowerCase();
		String end  = className.substring(1);
		return start + end;
	}
	
	/**
	 * 根据类对象获取类的别名
	 */
	public String getBeanAlias(Class<?> clazz){
		//定义类的别名，默认类的别名为类的名称首字母小写
		String alias = clazz.getSimpleName();
		alias = getLowerClassName(alias);
		//获取当前类上注解
		MyController myController = clazz.getAnnotation(MyController.class);
		MyRepository myRepository = clazz.getAnnotation(MyRepository.class);
		MyService myService = clazz.getAnnotation(MyService.class);
		
		if(myController != null && !"".equals(myController.value())){
			alias = myController.value();
		}else if(myRepository != null && !"".equals(myRepository.value())){
			alias = myRepository.value();
		}else if(myService != null && !"".equals(myService.value())){
			alias = myService.value();
		}
		
		return alias;
	}
	private void scanBasePackage(String scanBasePackage) {
		// 接收一个表示路径的参数，返回一个URL对象，该URL对象表示name对应的资源（文件）。
		//该方法只能接收一个相对路径，不能接收绝对路径如/xxx/xxx。并且，接收的相对路径是相对于项目的包的根目录来说的。
		URL url = this.getClass().getClassLoader().getResource(scanBasePackage.replaceAll("\\.", "/"));
		//   url ---> file:/D:/workspace/myspring/target/classes/com/xiaojiesir/demo
		//获取当前目录下所有文件
		
		try {
			File file = new File(url.toURI());//将url转为文件路径格式
			file.listFiles(new FileFilter() { //File类的文件过滤器
				
				//accept()方法接收到了参数pathname后参数是listFiles()传来的 在accept()的方法中进行判断.
				@Override
				public boolean accept(File pathname) {
					//判断是否为目录
					if(pathname.isDirectory()){
						scanBasePackage(scanBasePackage + "."+ pathname.getName());
					}else{
						//获取当前类的类路径
						String classPath = scanBasePackage + "." + pathname.getName().replaceAll("\\.class", "");
						//通过类的路径获取类的对象
						try {
							Class<?> clazz = this.getClass().getClassLoader().loadClass(classPath);
							//判断类上面是否有@MyController,@MyRepository,@MyService注解
							if(clazz.isAnnotationPresent(MyController.class)
									|| clazz.isAnnotationPresent(MyRepository.class)
									|| clazz.isAnnotationPresent(MyService.class)){
								//把MySpring管理的类放在集合中
								basePackageMappingToClass.add(clazz);
							}
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						
					}
					return false;
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据对象实例别名获取实例
	 * @param args 
	 */
	public Object getBean(String name){
		return aliasMappingInstance.get(name);
	}
	
}

