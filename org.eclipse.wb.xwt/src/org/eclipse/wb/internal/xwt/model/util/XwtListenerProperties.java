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
package org.eclipse.wb.internal.xwt.model.util;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectEventListeners;
import org.eclipse.wb.internal.core.xml.model.property.event.AbstractListenerProperty;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.property.event.XwtListenerProperty;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Support adding XWT specific {@link AbstractListenerProperty}s.
 *
 * @author scheglov_ke
 * @coverage XWT.model
 */
public final class XwtListenerProperties {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XwtListenerProperties(XmlObjectInfo rootObject) {
    rootObject.addBroadcastListener(new XmlObjectEventListeners() {
      public void invoke(XmlObjectInfo object, List<AbstractListenerProperty> properties)
          throws Exception {
        addListeners(object, properties);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ClassMap<List<String>> m_widgetEvents = ClassMap.create();

  /**
   * Adds listeners for events supported by given widget.
   */
  private void addListeners(XmlObjectInfo object, List<AbstractListenerProperty> properties)
      throws Exception {
    List<String> events = getWidgetEvents(object);
    for (String eventName : events) {
      properties.add(new XwtListenerProperty(object, eventName));
    }
  }

  /**
   * @return the {@link List} of SWT events which are supported by given widget.
   */
  private static List<String> getWidgetEvents(XmlObjectInfo widget) {
    Class<?> componentClass = widget.getDescription().getComponentClass();
    List<String> events = m_widgetEvents.get(componentClass);
    if (events == null) {
      events = Lists.newArrayList();
      m_widgetEvents.put(componentClass, events);
      while (componentClass != null) {
        {
          String parameterName = "RCP.untyped.events: " + componentClass.getName();
          String namesString = XmlObjectUtils.getParameter(widget, parameterName);
          if (namesString != null) {
            String[] names = StringUtils.split(namesString);
            Collections.addAll(events, names);
          }
        }
        componentClass = componentClass.getSuperclass();
      }
      Collections.sort(events);
    }
    return events;
  }
}
