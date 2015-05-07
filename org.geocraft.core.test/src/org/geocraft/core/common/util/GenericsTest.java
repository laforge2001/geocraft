/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;


public class GenericsTest extends TestCase {

  public void testList() {
    List<String> list = Generics.asList("foo", "bar", "baz");
    assertEquals(3, list.size());
    assertEquals(list.get(0), "foo");
  }

  public void testUnderstandingOfArraysAsList() {

    String[] init = new String[] { "foo", "bar", "baz" };
    List<String> other = Arrays.asList(init);
    try {
      other.add("more");
      fail();
    } catch (UnsupportedOperationException ex) {
      // can't add "more" to a fixed size list.
    }

    other.set(0, "new foo");
    assertEquals("new foo", init[0]);

  }

}
