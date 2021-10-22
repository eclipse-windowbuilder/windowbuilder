/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.EnvironmentUtils;

import org.apache.commons.lang.SystemUtils;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Helper for providing expected values in tests, it can return different values for different
 * platforms and computers.
 *
 * @author scheglov_ke
 */
public final class Expectations {
  private static String m_hostName;
  private static String m_OSName;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  static {
    try {
      m_hostName = EnvironmentUtils.HOST_NAME;
      m_OSName = SystemUtils.OS_NAME;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public static <V> V get(V default_value, Map<String, V> values) {
    List<String> keys = getKeys();
    for (String key : keys) {
      V value = values.get(key);
      if (value != null) {
        return value;
      }
    }
    if (default_value != null) {
      return default_value;
    } else {
      throw new IllegalArgumentException(MessageFormat.format(
          "Unable to find value in {0}, using keys {1}",
          values,
          keys));
    }
  }

  public static <V> V get(Map<String, V> values) {
    return get((V) null, values);
  }

  public static <V> V get(String key_1, V value_1, String key_2, V value_2) {
    Map<String, V> map = ImmutableMap.of(key_1, value_1, key_2, value_2);
    return get(map);
  }

  public static <V> V get(String key_1, V value_1, String key_2, V value_2, String key_3, V value_3) {
    Map<String, V> map = ImmutableMap.of(key_1, value_1, key_2, value_2, key_3, value_3);
    return get(map);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // int
  //
  ////////////////////////////////////////////////////////////////////////////
  public static int get(String key_1, int value_1, String key_2, int value_2) {
    Map<String, Integer> map = ImmutableMap.of(key_1, value_1, key_2, value_2);
    return get(map);
  }

  public static int get(String key_1,
      int value_1,
      String key_2,
      int value_2,
      String key_3,
      int value_3) {
    Map<String, Integer> map = ImmutableMap.of(key_1, value_1, key_2, value_2, key_3, value_3);
    return get(map);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // double
  //
  ////////////////////////////////////////////////////////////////////////////
  public static double get(String key_1, double value_1, String key_2, double value_2) {
    Map<String, Double> map = ImmutableMap.of(key_1, value_1, key_2, value_2);
    return get(map);
  }

  public static double get(String key_1,
      double value_1,
      String key_2,
      double value_2,
      String key_3,
      double value_3) {
    Map<String, Double> map = ImmutableMap.of(key_1, value_1, key_2, value_2, key_3, value_3);
    return get(map);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static List<String> getKeys() {
    String keyHostOS = m_hostName + " " + m_OSName;
    String keyHost = m_hostName;
    String keyOS = m_OSName;
    String timeZone = Calendar.getInstance().getTimeZone().getID();
    return ImmutableList.of(
        keyHostOS,
        keyHost,
        keyHost.toLowerCase(Locale.ENGLISH),
        keyHost.toUpperCase(Locale.ENGLISH),
        keyOS,
        timeZone);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // via Arrays
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class KeyValue<V> {
    public String key;
    public V value;

    public KeyValue(String _key, V _value) {
      key = _key;
      value = _value;
    }
  }

  public static <V> V get(V default_value, KeyValue<V>... values) {
    Map<String, V> map = Maps.newHashMap();
    for (KeyValue<V> value : values) {
      map.put(value.key, value.value);
    }
    return get(default_value, map);
  }

  public static <V> V get(KeyValue<V> values[]) {
    return get((V) null, values);
  }
  public static class IntValue extends KeyValue<Integer> {
    public IntValue(String _key, Integer _value) {
      super(_key, _value);
    }
  }
  public static class DblValue extends KeyValue<Double> {
    public DblValue(String _key, Double _value) {
      super(_key, _value);
    }
  }
  public static class BoolValue extends KeyValue<Boolean> {
    public BoolValue(String _key, Boolean _value) {
      super(_key, _value);
    }
  }
  public static class InsValue extends KeyValue<Insets> {
    public InsValue(String _key, Insets _value) {
      super(_key, _value);
    }
  }
  public static class DimValue extends KeyValue<Dimension> {
    public DimValue(String _key, Dimension _value) {
      super(_key, _value);
    }
  }
  public static class RectValue extends KeyValue<Rectangle> {
    public RectValue(String _key, Rectangle _value) {
      super(_key, _value);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // String
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class StrValue extends KeyValue<String> {
    public StrValue(String _key, String _value) {
      super(_key, _value);
    }
  }

  public static String get(String default_value, StrValue... values) {
    Map<String, String> map = Maps.newHashMap();
    for (StrValue value : values) {
      map.put(value.key, value.value);
    }
    return get(default_value, map);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Fluent interface
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class State<V> {
    private final V def;
    private final Map<String, V> map;

    public State(V def, Map<String, V> map) {
      this.def = def;
      this.map = map;
    }

    public State<V> or(String key, V value) {
      map.put(key, value);
      return this;
    }

    public V get() {
      return Expectations.get(def, map);
    }
  }

  public static <V> State<V> get(V def) {
    Map<String, V> map = Maps.newHashMap();
    return new State<V>(def, map);
  }
}
