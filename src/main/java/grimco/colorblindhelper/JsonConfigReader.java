package grimco.colorblindhelper;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonConfigReader<T> implements Iterable<T>
{
	public static class ModToken
	{
		private Class<?> mainClass;
		private String assetPath;
		
		public ModToken(Class<?> mainClass, String assetPath) {
			this.mainClass = mainClass;
			this.assetPath = assetPath;
		}
		
		public Class<?> getMainClass() {
			return mainClass;
		}
		
		public String getAssetPath() {
			return assetPath;
		}
	}

	public static final String DEFAULT_KEY = "data";
	private static final JsonParser parser = new JsonParser();
	
	private final GsonBuilder builder = new GsonBuilder();
	
	private File file;
	private JsonObject root;
	
	private Class<T> type = null;
	private TypeToken<T> typeToken = null;
	
	public JsonConfigReader(ModToken mod, String fullFileName, Class<T> objClass)
	{
		this(mod, new File(fullFileName), objClass);
	}
	
	public JsonConfigReader(ModToken mod, File file, Class<T> objClass)
	{
		this.type = objClass;
		initialize(mod, file);
	}
	
	public JsonConfigReader(ModToken mod, String fullFileName, TypeToken<T> objType)
	{
		this(mod, new File(fullFileName), objType);
	}

	public JsonConfigReader(ModToken mod, File file, TypeToken<T> objType)
	{
		this.typeToken = objType;
		initialize(mod, file);
	}
	
	private void initialize(ModToken mod, File fileIn)
	{
		this.file = fileIn;
		
		if (!fileIn.exists())
		{
			fileIn.getParentFile().mkdirs();
			String assetPath = mod.getAssetPath();
			if (!assetPath.endsWith("/"))
			{
				assetPath = assetPath + "/";
			}
			
			FileUtil.copyFromJar(mod.getMainClass(), assetPath + fileIn.getName(), fileIn);
		}
		
		refresh();
	}

	public JsonObject parseFile()
	{
		try (final FileReader reader = new FileReader(file)) {
			return parser.parse(reader).getAsJsonObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void refresh()
	{
		this.root = parseFile();
	}

	public boolean hasKey(String key)
	{
		return root.get(key) != null;
	}

	public GsonBuilder getBuilder()
	{
		return builder;
	}

	@SuppressWarnings("unchecked")
	public List<T> getElements(String key)
	{
		if (!hasKey(key)) {
			return Lists.newArrayList();
		}
		
		Gson gson = builder.create();
		JsonArray elements = root.get(key).getAsJsonArray();
		List<T> list = new ArrayList<T>();
		for (int i = 0; i < elements.size(); i++) {
			if (type == null) {
				list.add((T) gson.fromJson(elements.get(i), typeToken.getType()));
			} else {
				list.add(gson.fromJson(elements.get(i), type));
			}
		}
		return list;
	}
	
	public List<T> getElements()
	{
		return getElements(DEFAULT_KEY);
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return getElements().iterator();
	}
}