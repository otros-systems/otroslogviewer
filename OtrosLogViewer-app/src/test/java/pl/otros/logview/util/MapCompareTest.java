/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package pl.otros.logview.util;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.util.HashMap;

public class MapCompareTest {

	private HashMap<String, String> m1 = null;
	private HashMap<String, String> m2 = null;

	@BeforeMethod
	public void prepare() {
		m1 = new HashMap<>();
		m2 = new HashMap<>();

	}

	
	@Test
	public void testAreMapsEqualsBothEmpty() {
		assertTrue(MapCompare.areMapsEquals(m1, m2));
	}
	
	@Test
	public void testAreMapsEqualsBothNull() {
		assertTrue(MapCompare.areMapsEquals(null, null));
	}
	
	@Test
	public void testAreMapsEqualsFirstNull() {
		assertFalse(MapCompare.areMapsEquals(null, m2));
	}
	
	@Test
	public void testAreMapsEqualsSecondNull() {
		assertFalse(MapCompare.areMapsEquals(m1, null));
	}
	
	@Test
	public void testAreMapsEqualsSizeNotEqual() {
		m1.put("A", "B");
		m1.put("B", "B");
		
		m2.put("A", "B");
		assertFalse(MapCompare.areMapsEquals(m1, m2));
	}
	
	@Test
	public void testAreMapsEqualTheSame() {
		m1.put("A", "B");
		m1.put("B", "B");
		
		m2.put("A", "B");
		m2.put("B", "B");
		assertTrue(MapCompare.areMapsEquals(m1, m2));
	}
	
	@Test
	public void testAreMapsEqualDifferentValues() {
		m1.put("A", "B");
		m1.put("B", "C");
		
		m2.put("A", "B");
		m2.put("B", "B");
		assertFalse(MapCompare.areMapsEquals(m1, m2));
	}
	
	@Test
	public void testAreMapsEqualDifferentKeys() {
		m1.put("A", "B");
		m1.put("B", "C");
		
		m2.put("A", "B");
		m2.put("C", "C");
		assertFalse(MapCompare.areMapsEquals(m1, m2));
	}
	
	@Test
	public void testAreMapsEqualNulltKeys() {
		m1.put("A", "B");
		m1.put(null, "C");
		
		m2.put("A", "B");
		m2.put(null, "C");
		assertTrue(MapCompare.areMapsEquals(m1, m2));
	}

	
}
