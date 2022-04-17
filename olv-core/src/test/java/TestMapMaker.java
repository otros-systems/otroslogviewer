/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class TestMapMaker {

  /**
   * @param args
   */
  public static void main(String[] args) {
    Cache<String, String> makeMap = CacheBuilder.newBuilder().weakKeys().maximumSize(10).build();

    for (int i = 0; i < 7; i++) {
      makeMap.put("a" + i, "V" + i);
    }
    System.out.println(Joiner.on(", ").withKeyValueSeparator("=").join(makeMap.asMap()));
    for (int i = 0; i < 1; i++) {
      makeMap.put("b" + i, "V" + i);
    }
    System.out.println(Joiner.on(", ").withKeyValueSeparator("=").join(makeMap.asMap()));
    System.out.println(makeMap.asMap().containsKey("a1"));
    System.out.println(makeMap.asMap().containsKey("a4"));
    System.out.println(makeMap.asMap().containsKey("a5"));
    System.out.println(makeMap.asMap().get("a1"));
    System.out.println(makeMap.asMap().get("a4"));
    System.out.println(makeMap.asMap().get("a5"));

  }
}
