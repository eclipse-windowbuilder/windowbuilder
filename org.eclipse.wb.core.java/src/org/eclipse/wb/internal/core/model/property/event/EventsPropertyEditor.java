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
package org.eclipse.wb.internal.core.model.property.event;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoEventListeners;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.util.GenericTypeResolverJavaInfo;
import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of {@link PropertyEditor} for {@link EventsProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
final class EventsPropertyEditor extends AbstractComplexEventPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final EventsPropertyEditor INSTANCE = new EventsPropertyEditor();

  private EventsPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractListenerProperty[] getProperties(Property property) throws Exception {
    EventsProperty eventsProperty = (EventsProperty) property;
    JavaInfo javaInfo = eventsProperty.getJavaInfo();
    // get from cache or create
    AbstractListenerProperty[] properties =
        (AbstractListenerProperty[]) javaInfo.getArbitraryValue(eventsProperty);
    if (properties == null) {
      properties = createProperties(javaInfo);
      javaInfo.putArbitraryValue(eventsProperty, properties);
    }
    return properties;
  }

  private AbstractListenerProperty[] createProperties(JavaInfo javaInfo) throws Exception {
    List<AbstractListenerProperty> properties = Lists.newArrayList();
    // standard: add*[Listener,Handler]
    for (ListenerInfo listener : getListeners(javaInfo)) {
      properties.add(new ListenerProperty(javaInfo, listener));
    }
    // use broadcast
    javaInfo.getBroadcast(JavaInfoEventListeners.class).invoke(javaInfo, properties);
    // return as array
    return properties.toArray(new AbstractListenerProperty[properties.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ClassMap<List<ListenerInfo>> m_listenersCache = ClassMap.create();

  /**
   * @return the {@link ListenerInfo} for each listener in component class.
   */
  private static List<ListenerInfo> getListeners(JavaInfo javaInfo) throws Exception {
    Class<?> componentClass = javaInfo.getDescription().getComponentClass();
    List<ListenerInfo> listeners = m_listenersCache.get(componentClass);
    if (listeners == null) {
      GenericTypeResolver externalResolver = new GenericTypeResolverJavaInfo(javaInfo);
      listeners = getListeners(componentClass, externalResolver);
      m_listenersCache.put(componentClass, listeners);
    }
    return listeners;
  }

  private static List<ListenerInfo> getListeners(Class<?> componentClass,
      GenericTypeResolver externalResolver) {
    // prepare methods
    List<Method> methods = get_addListener_methods(componentClass);
    // fill list of listeners
    List<ListenerInfo> listeners = Lists.newArrayList();
    for (Method method : methods) {
      listeners.add(new ListenerInfo(method, componentClass, externalResolver));
    }
    // use simple names
    ListenerInfo.useSimpleNamesWherePossible(listeners);
    // sort listeners by name
    Collections.sort(listeners, new Comparator<ListenerInfo>() {
      public int compare(ListenerInfo listener_1, ListenerInfo listener_2) {
        return listener_1.getName().compareTo(listener_2.getName());
      }
    });
    return listeners;
  }

  /**
   * @return the <code>addXXXListener(listener)</code> {@link Method}'s.
   */
  private static List<Method> get_addListener_methods(Class<?> clazz) {
    List<Method> listenerMethods = Lists.newArrayList();
    for (Method method : clazz.getMethods()) {
      if (ListenerInfo.isAddListenerMethod(method)) {
        listenerMethods.add(method);
      }
    }
    return listenerMethods;
  }
}
