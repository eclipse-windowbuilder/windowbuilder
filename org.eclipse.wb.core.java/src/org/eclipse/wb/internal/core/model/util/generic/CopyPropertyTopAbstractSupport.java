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
package org.eclipse.wb.internal.core.model.util.generic;

import com.google.common.base.Predicate;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * This helper allows to create top-level {@link Property} as copy of other {@link Property}
 * (usually part of complex property).
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public abstract class CopyPropertyTopAbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Installation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link JavaInfo} to copy properties according parameters in description.
   */
  protected final void install(JavaInfo javaInfo, String prefix) {
    for (String parameter : javaInfo.getDescription().getParameters().keySet()) {
      String sourcePath = null;
      String copyTitle = null;
      PropertyCategory category = PropertyCategory.NORMAL;
      if (parameter.startsWith(prefix)) {
        String[] parts = StringUtils.split(parameter);
        for (String part : parts) {
          if (part.startsWith("from=")) {
            sourcePath = StringUtils.removeStart(part, "from=");
          }
          if (part.startsWith("to=")) {
            copyTitle = StringUtils.removeStart(part, "to=");
          }
          if (part.startsWith("category=")) {
            String categoryText = StringUtils.removeStart(part, "category=");
            category = PropertyCategory.get(categoryText, category);
          }
        }
        // validate
        if (sourcePath == null || copyTitle == null) {
          String message = "No 'from' or 'to' attributes: " + parameter;
          JavaInfoUtils.getState(javaInfo).addWarning(new EditorWarning(message, null));
          continue;
        }
        // OK, create copy processor
        Predicate<JavaInfo> targetPredicate = createTargetPredicate(javaInfo);
        new CopyProcessor(javaInfo, targetPredicate, sourcePath, copyTitle, category);
      }
    }
  }

  /**
   * @param javaInfo
   *          the {@link JavaInfo} passed into {@link #install(JavaInfo, String)}.
   *
   * @return the {@link Predicate} to check if property of some {@link JavaInfo} should be copied to
   *         its top properties.
   */
  protected abstract Predicate<JavaInfo> createTargetPredicate(JavaInfo javaInfo);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy processor
  //
  ////////////////////////////////////////////////////////////////////////////
  static class CopyProcessor {
    private final Predicate<JavaInfo> m_targetPredicate;
    private final String m_sourcePath;
    private final String m_copyTitle;
    private final PropertyCategory m_category;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    CopyProcessor(JavaInfo someJavaInfo,
        Predicate<JavaInfo> targetPredicate,
        String sourcePath,
        String copyTitle,
        PropertyCategory category) {
      m_targetPredicate = targetPredicate;
      m_sourcePath = sourcePath;
      m_copyTitle = copyTitle;
      m_category = category;
      someJavaInfo.addBroadcastListener(new JavaInfoAddProperties() {
        public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
          if (m_targetPredicate.apply(javaInfo)) {
            Property source = PropertyUtils.getByPath(properties, m_sourcePath);
            Property copy = getCopy(source);
            if (copy != null) {
              properties.add(copy);
            }
          }
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    private Property m_oldSource;
    private Property m_oldCopy;

    private Property getCopy(Property source) {
      if (m_oldSource != source) {
        m_oldSource = source;
        m_oldCopy = createCopy(source);
      }
      return m_oldCopy;
    }

    private Property createCopy(Property source) {
      if (source instanceof GenericPropertyImpl) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) source;
        Property copy = new GenericPropertyImpl(genericProperty, m_copyTitle);
        copy.setCategory(m_category);
        return copy;
      }
      return null;
    }
  }
}
