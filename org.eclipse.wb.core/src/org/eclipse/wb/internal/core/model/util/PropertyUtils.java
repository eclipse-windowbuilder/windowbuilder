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
package org.eclipse.wb.internal.core.model.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Utils for {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class PropertyUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private PropertyUtils() {
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the titles of given {@link Property}'s.
   */
  public static String[] getTitles(Property... properties) {
    String[] titles = new String[properties.length];
    for (int i = 0; i < properties.length; i++) {
      Property property = properties[i];
      titles[i] = property.getTitle();
    }
    return titles;
  }
  /**
   * @return the titles of given {@link Property}'s.
   */
  public static List<String> getTitles(List<Property> properties) {
    List<String> titles = Lists.newArrayList();
    for (Property property : properties) {
      titles.add(property.getTitle());
    }
    return titles;
  }
  /**
   * @return the text presentation of {@link Property} value, may be <code>null</code>.
   */
  public static String getText(final Property property) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return (String) ReflectionUtils.invokeMethod2(
            property.getEditor(),
            "getText",
            Property.class,
            property);
      }
    }, null);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // getPropertyByTitle()
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Property getByTitle(Property[] properties, String title) {
    for (Property property : properties) {
      if (title.equals(property.getTitle())) {
        return property;
      }
    }
    return null;
  }
  public static Property getByTitle(List<Property> properties, String title) {
    for (Property property : properties) {
      if (StringUtils.equals(title, property.getTitle())) {
        return property;
      }
    }
    return null;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // getPropertyByPath()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Property} using "/" separated path.
   */
  public static Property getByPath(ObjectInfo object, String path) throws Exception {
    return getByPath(object.getProperties(), path);
  }
  /**
   * @return the {@link Property} using "/" separated path.
   */
  public static Property getByPath(List<Property> properties, String path) throws Exception {
    Property[] propertiesArray = properties.toArray(new Property[properties.size()]);
    return getByPath(propertiesArray, path);
  }
  /**
   * @return the {@link Property} using "/" separated path.
   */
  public static Property getByPath(Property[] properties, String path) throws Exception {
    Property property = null;
    for (String pathElement : StringUtils.split(path, '/')) {
      property = getByTitle(properties, pathElement);
      properties = getChildren(property);
    }
    return property;
  }
  /**
   * @return the {@link Property} using "/" separated path.
   */
  public static Property getByPath(Property property, String path) throws Exception {
    return getByPath(getChildren(property), path);
  }
  /**
   * @return sub-properties of given {@link Property}, may be empty array, but not <code>null</code>
   *         .
   */
  public static Property[] getChildren(Property property) throws Exception {
    if (property != null) {
      if (property.getEditor() instanceof IComplexPropertyEditor) {
        return ((IComplexPropertyEditor) property.getEditor()).getProperties(property);
      }
    }
    return new Property[0];
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Filter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Property}'s that satisfy given {@link Predicate}.
   */
  public static List<Property> getProperties(ObjectInfo objectInfo, Predicate<Property> predicate)
      throws Exception {
    Property[] properties = objectInfo.getProperties();
    List<Property> filteredProperties = Lists.newArrayList();
    for (Property property : properties) {
      if (predicate.apply(property)) {
        filteredProperties.add(property);
      }
    }
    return filteredProperties;
  }
  /**
   * @return the {@link Property}'s of given {@link JavaInfo} that are not listed in parameter with
   *         given name. We use this to filter out some properties of layout/layoutData.
   */
  public static List<Property> getProperties_excludeByParameter(ObjectInfo objectInfo,
      String parameterName) throws Exception {
    Predicate<Property> predicate = getExcludeByTitlePredicate(objectInfo, parameterName);
    return getProperties(objectInfo, predicate);
  }
  /**
   * Keeps in {@link List} only {@link Property}'s that satisfy given {@link Predicate}.
   */
  public static void filterProperties(List<Property> properties, Predicate<Property> predicate)
      throws Exception {
    for (Iterator<Property> I = properties.iterator(); I.hasNext();) {
      Property property = I.next();
      if (!predicate.apply(property)) {
        I.remove();
      }
    }
  }
  /**
   * @return the {@link Predicate} for {@link Property} that does not accept properties with titles
   *         listed in parameter value.
   */
  public static Predicate<Property> getExcludeByTitlePredicate(ObjectInfo objectInfo,
      String parameterName) {
    Predicate<Property> predicate = Predicates.alwaysTrue();
    String propertiesExcludeString =
        GlobalState.getParametersProvider().getParameter(objectInfo, parameterName);
    if (propertiesExcludeString != null) {
      String[] propertiesExclude = StringUtils.split(propertiesExcludeString);
      predicate = getExcludeByTitlePredicate(propertiesExclude);
    }
    return predicate;
  }
  /**
   * @return the {@link Predicate} for {@link Property} that does not accept properties given
   *         titles.
   */
  public static Predicate<Property> getExcludeByTitlePredicate(final String... excludeTitles) {
    return new Predicate<Property>() {
      public boolean apply(Property t) {
        return !ArrayUtils.contains(excludeTitles, t.getTitle());
      }
    };
  }
  /**
   * @return the {@link Predicate} for {@link Property} that does accept properties given titles.
   */
  public static Predicate<Property> getIncludeByTitlePredicate(final String... includeTitles) {
    return new Predicate<Property>() {
      public boolean apply(Property t) {
        return ArrayUtils.contains(includeTitles, t.getTitle());
      }
    };
  }
}
