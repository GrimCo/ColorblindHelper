package grimco.colorblindhelper;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

public class FileUtil
{
	
	public static void copyFromJar(Class<?> jarClass, String filename, File to) {
		ColorblindHelper.logger.info("Copying file " + filename + " from jar");
		URL url = jarClass.getResource("/assets/" + filename);
		
		try {
			FileUtils.copyURLToFile(url, to);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Nonnull
	public static File writeToFile(String filepath, String json) {
		File file = new File(filepath);
		
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(json);
			fw.flush();
			fw.close();
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
