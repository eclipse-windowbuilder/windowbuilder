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
package org.eclipse.wb.internal.core.xml.model.description.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.property.IConfigurablePropertyObject;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Abstract description for {@link IConfigurablePropertyObject}.
 * 
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public abstract class AbstractConfigurableDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, Object> m_parameters = Maps.newHashMap();

  /**
   * Adds new parameter.
   */
  public final void addParameter(String name, String value) {
    Assert.isTrue(
        !m_parameters.containsKey(name),
        MessageFormat.format("Duplicate declaration of parameter ''{0}''.", name));
    m_parameters.put(name, value);
  }

  /**
   * Adds new list parameter.
   */
  @SuppressWarnings("unchecked")
  public final void addListParameter(String name, String value) {
    List<String> list = (List<String>) m_parameters.get(name);
    if (list == null) {
      list = Lists.newArrayList();
      m_parameters.put(name, list);
    }
    list.add(value);
  }

  /**
   * Configures given {@link IConfigurablePropertyObject} with current parameters.
   */
  public final void configure(EditorContext context, IConfigurablePropertyObject configurableObject)
      throws Exception {
    configurableObject.configure(context, m_parameters);
  }
}
