package com.web.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class JsonUtils {
	public static final String JSON_EXTENSION = ".json";
	protected static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	public static void writeObjectToJsonFile(Object object, String outputPath, String objectName) {
		String filename = outputPath + File.separator + objectName + JSON_EXTENSION;

		if (object != null) {
			String jsonObject = gson.toJson(object);
			try (Writer writer = new FileWriter(filename)) {
				writer.write(jsonObject);
			} catch (IOException e) {
				throw new RuntimeException("Issue with the json file writer and the file: " + filename, e);
			}
		} else {
			System.out.println("ERROR:Object to be written there: " + filename + " is empty.");
		}
	}

	public static <T> T getObject(String completeFilePath, Class<T> type) throws Exception {
		JsonReader reader = getReaderFromFilepath(completeFilePath);

		T object = getObjectWithJsonReader(reader, type, completeFilePath);

		if (object == null)
			throw new Exception("Object from " + completeFilePath + " is null");

		return object;
	}

	private static JsonReader getReaderFromFilepath(String filePath) {
		File file = JsonUtils.getRessourcesPath(filePath);
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(file.getPath()));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File: " + filePath + " not found.", e);
		}

		return reader;
	}

	private static <T> T getObjectWithJsonReader(JsonReader reader, Class<T> type, String filepath) {
		T object = null;
		try {
			object = gson.fromJson(reader, type);
		} catch (Exception e) {
			throw new RuntimeException("Issue while reading json from: " + filepath, e);
		}

		return object;
	}

	/**
	 * @param path
	 * @return a file object even though the path is relative to the resources
	 * @throws URISyntaxException
	 */
	private static File getRessourcesPath(String path) {
		Path originalPath = Paths.get(path);
		File workingPath = null;

		if (Files.exists(originalPath)) {
			workingPath = originalPath.toFile();
		} else {
			URL resource = JsonUtils.class.getClassLoader().getResource(path);
			if (resource != null) {
				try {
					workingPath = Paths.get(resource.toURI()).toFile();
				} catch (URISyntaxException e) {
					throw new RuntimeException("Issue with resource: " + resource, e);
				}
			} else {
				throw new RuntimeException("Trying to get URL from path:" + path + " but resource path is null.");
			}
		}
		return workingPath;
	}

}
