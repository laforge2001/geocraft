/*
 * Copyright (C) ConocoPhillips 2008, 2009 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Another class (@see TextFile) to make Java look as nice as Python.
 * 
 * Inspired by a blog post by Mario Gleichmann. 
 */

public class Generics {

  /**
   *   
   * Easy way to create an initialized List.
   * 
   * This is different to Arrays.asList which has a fixed size
   * and where the initializing array is updated also. See 
   * unit test for example. 
   * 
   * import static org.geocraft.core.common.Generics.asList;
   * List<Integer> list = asList(1,2,3,4);
   * 
   * @param <T> the type of the collection. 
   * @param elements to populate collection. 
   * @return List<T>
   */
  public static <T> List<T> asList(T... elements) {
    List<T> list = new ArrayList<T>();
    for (T element : elements) {
      list.add(element);
    }
    return list;
  }

  /**
   * Easy way to create an initialized array.
   * 
   * import static org.geocraft.core.common.Generics.asArray;
   * Integer[] array = asArray(1,2,3,4);
   * 
   * @param <T> the type of the collection. 
   * @param elements to populate collection. 
   * @return T[] 
   */
  public static <T> T[] asArray(T... elements) {
    return elements;
  }

  /**
   * Easy way to create an initialized Set.
   * 
   * import static org.geocraft.core.common.Generics.asSet;
   * @param <T> the type of the collection. 
   * @param elements to populate collection. 
   * @return an initialized Set. 
   */
  public static <T> Set<T> asSet(T... elements) {
    return new HashSet<T>(asList(elements));
  }

  /**
   * Easy way to create an initialized Map.
   * 
   * import static org.geocraft.core.common.Generics.asMap;
   * @param <K> type of the keys
   * @param <V> type of the values
   * @param keys list
   * @param values list
   * @return an initialized Map. 
   */
  public static <K, V> Map<K, V> asMap(List<K> keys, List<V> values) {
    Map<K, V> map = new HashMap<K, V>();
    for (int i = 0; i < keys.size(); i++) {
      map.put(keys.get(i), values.get(i));
    }
    return map;
  }
}
