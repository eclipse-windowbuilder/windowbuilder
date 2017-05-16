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
package org.eclipse.wb.internal.core.model.property;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.base64.Base64Utils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * {@link PropertyManager} is used to get/set attributes of {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public final class PropertyManager {
  private static final String P_CATEGORIES = "org.eclipse.wb.core.model.property.PropertyManager";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link PropertyCategory} of given Property. Usually this is just
   *         {@link Property#getCategory()}, but some properties may be configured by user to have
   *         different category.
   */
  public static PropertyCategory getCategory(Property property) {
    PropertyCategory category = getCategoryForced(property);
    if (category != null) {
      return category;
    }
    return property.getCategory();
  }
  /**
   * @return the forced {@link PropertyCategory} of given Property, may be <code>null</code>.
   */
  public static PropertyCategory getCategoryForced(Property property) {
    ToolkitDescription toolkit = getToolkit(property);
    if (toolkit != null) {
      String title = property.getTitle();
      return getCategories(toolkit).get(title);
    }
    return null;
  }
  /**
   * Sets the forced {@link PropertyCategory} of given Property. If <code>null</code> then default
   * category should be used.
   */
  public static void setCategory(Property property, PropertyCategory category) {
    ToolkitDescription toolkit = getToolkit(property);
    if (toolkit != null) {
      String title = property.getTitle();
      Map<String, PropertyCategory> categories = getCategories(toolkit);
      categories.put(title, category);
      saveCategories(toolkit, categories);
    }
  }
  /**
   * Sets the forced {@link PropertyCategory} for all properties with given title.
   */
  public static void setCategory(ToolkitDescription toolkit, String title, PropertyCategory category) {
    if (toolkit != null) {
      Map<String, PropertyCategory> categories = getCategories(toolkit);
      categories.put(title, category);
      saveCategories(toolkit, categories);
    }
  }
  /**
   * @return the {@link ToolkitDescription} for component of given {@link Property}, may be
   *         <code>null</code> if no corresponding component.
   */
  private static ToolkitDescription getToolkit(Property property) {
    return GlobalState.getToolkit();
    // TODO(scheglov)
//    if (property instanceof JavaProperty) {
//      JavaProperty javaProperty = (JavaProperty) property;
//      return javaProperty.getJavaInfo().getDescription().getToolkit();
//    }
//    return null;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Toolkit specific categories
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<ToolkitDescription, Map<String, PropertyCategory>> m_toolkitCategories =
      Maps.newHashMap();
  public static void flushCache() {
    m_toolkitCategories.clear();
  }
  private static Map<String, PropertyCategory> getCategories(ToolkitDescription toolkit) {
    Map<String, PropertyCategory> categories = m_toolkitCategories.get(toolkit);
    if (categories == null) {
      categories = loadCategories(toolkit);
      m_toolkitCategories.put(toolkit, categories);
    }
    return categories;
  }
  private static Map<String, PropertyCategory> loadCategories(final ToolkitDescription toolkit) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Map<String, PropertyCategory>>() {
      public Map<String, PropertyCategory> runObject() throws Exception {
        return loadCategories0(toolkit);
      }
    }, Maps.<String, PropertyCategory>newTreeMap());
  }
  private static void saveCategories(final ToolkitDescription toolkit,
      final Map<String, PropertyCategory> categories) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        saveCategories0(toolkit, categories);
      }
    });
  }
  @SuppressWarnings("unchecked")
  private static Map<String, PropertyCategory> loadCategories0(ToolkitDescription toolkit)
      throws Exception {
    String encoded = toolkit.getPreferences().getString(P_CATEGORIES);
    byte[] bytes = Base64Utils.decodeToBytes(encoded);
    ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
    Map<String, Integer> categoriesIndex = (Map<String, Integer>) stream.readObject();
    return toCategoriesObject(categoriesIndex);
  }
  private static void saveCategories0(ToolkitDescription toolkit,
      Map<String, PropertyCategory> categories) throws Exception {
    byte[] bytes;
    {
      Map<String, Integer> categoriesIndex = toCategoriesIndex(categories);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream stream = new ObjectOutputStream(baos);
      stream.writeObject(categoriesIndex);
      stream.close();
      bytes = baos.toByteArray();
    }
    // encode into preferences
    String encoded = Base64Utils.encode(bytes);
    toolkit.getPreferences().setValue(P_CATEGORIES, encoded);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion for PropertyCategory
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<String, Integer> toCategoriesIndex(Map<String, PropertyCategory> source) {
    Map<String, Integer> target = Maps.newTreeMap();
    for (Map.Entry<String, PropertyCategory> entry : source.entrySet()) {
      target.put(entry.getKey(), getCategoryIndex(entry.getValue()));
    }
    return target;
  }
  private static Map<String, PropertyCategory> toCategoriesObject(Map<String, Integer> source) {
    Map<String, PropertyCategory> target = Maps.newTreeMap();
    for (Map.Entry<String, Integer> entry : source.entrySet()) {
      target.put(entry.getKey(), getCategoryByIndex(entry.getValue()));
    }
    return target;
  }
  private static int getCategoryIndex(PropertyCategory category) {
    if (category == PropertyCategory.PREFERRED) {
      return 0;
    }
    if (category == PropertyCategory.NORMAL) {
      return 1;
    }
    /*if (category == PropertyCategory.ADVANCED)*/{
      return 2;
    }
  }
  private static PropertyCategory getCategoryByIndex(int index) {
    if (index == 0) {
      return PropertyCategory.PREFERRED;
    }
    if (index == 1) {
      return PropertyCategory.NORMAL;
    }
    /*if (index == 2)*/{
      return PropertyCategory.ADVANCED;
    }
  }
}
