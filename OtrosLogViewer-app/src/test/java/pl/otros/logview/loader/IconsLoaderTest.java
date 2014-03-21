package pl.otros.logview.loader;

import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import pl.otros.logview.gui.Icons;

import java.lang.reflect.Field;

public class IconsLoaderTest {

	@Test
	public void testLoadIcons() throws IllegalArgumentException, IllegalAccessException {
		IconsLoader.loadIcons();
		Field[] fields = Icons.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getAnnotation(Path.class) !=null){
				assertNotNull( "Icon " + fields[i].getName() + " not loaded" ,fields[i].get(null));
			}
		}
	}

}
