package pl.otros.logview.loader;

import org.junit.Test;
import pl.otros.logview.gui.Icons;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;

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
