package pl.otros.logview.loader;

import org.testng.annotations.Test;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.Path;

import java.lang.reflect.Field;

import static org.testng.AssertJUnit.assertNotNull;

public class IconsLoaderTest {

	@Test
	public void testLoadIcons() throws IllegalArgumentException, IllegalAccessException {
		IconsLoader.loadIcons();
		Field[] fields = Icons.class.getFields();
		for (Field field : fields) {
			if (field.getAnnotation(Path.class) != null) {
				assertNotNull("Icon " + field.getName() + " not loaded", field.get(null));
			}
		}
	}

}
