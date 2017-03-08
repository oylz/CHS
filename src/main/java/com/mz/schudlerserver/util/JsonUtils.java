package com.mz.schudlerserver.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;



public class JsonUtils {
	private static ObjectMapper objectMapper = null;
	static {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		objectMapper.getDeserializationConfig().withDateFormat(new SimpleDateFormat("yyyyMMddHHmmssS"));
	}
	
	/**
	 * json串转为Java对象列表。
	 * @param json json串
	 * @param clazz Java类
	 * @return Java对象列表
	 * @throws Exception
	 */
	public static <T> List<T> toJavaList(String json, Class<T> clazz) throws Exception {
		return objectMapper.readValue(json, objectMapper.getTypeFactory()
				.constructCollectionType(List.class, clazz));
	}
	
	/**
	 * 从文件中读取json串，并转为Java对象列表。
	 * @param file 文件
	 * @param clazz Java类
	 * @return Java对象列表
	 * @throws Exception
	 */
	public static <T> List<T> toJavaList(File file, Class<T> clazz) throws Exception {
		return objectMapper.readValue(file, objectMapper.getTypeFactory()
				.constructCollectionType(List.class, clazz));
	}
	
	/**
	 * json串转为Java对象。
	 * @param json json串
	 * @param clazz Java类
	 * @return Java对象
	 * @throws Exception
	 */
	public static <T> T toJava(String json, Class<T> clazz) throws Exception {
		return objectMapper.readValue(json, clazz);
	}
	
	public static <T, V> Map<T, V> toJavaMap(String jsonString, Class<T> keyClass, Class<V> valueClass) throws Exception {
		return objectMapper.readValue(jsonString, objectMapper.getTypeFactory()
				.constructMapLikeType(Map.class, keyClass, valueClass));
	}
	
	/**
	 * 对象转为Map<String, Object>
	 * 
	 * @param
	 * @return
	 */
	public static Map<String, Object> toMap(Object object) {
		return objectMapper.convertValue(object,
				new TypeReference<Map<String, Object>>() {
				});

	}
	
	/**
	 * Java对象转为json串。
	 * @param obj Java对象
	 * @return json串
	 * @throws Exception
	 */
	public static String toJsonString(Object obj) throws Exception {
		return objectMapper.writeValueAsString(obj);
	}
}
