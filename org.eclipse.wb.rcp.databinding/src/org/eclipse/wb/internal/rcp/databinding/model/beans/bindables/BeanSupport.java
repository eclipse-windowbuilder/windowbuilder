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
package org.eclipse.wb.internal.rcp.databinding.model.beans.bindables;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveComparator;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectFieldModelSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectModelSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.ViewerObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ClassUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Properties provider for <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class BeanSupport {
  private final Map<Class<?>, Image> m_classToImage = Maps.newHashMap();
  private final IModelResolver m_resolver;
  private final Class<?> m_IObservable;
  private final Class<?> m_IObservableValue;
  private final Class<?> m_IObservableList;
  private final Class<?> m_IObservableSet;
  private final Class<?> m_ISelectionProvider;
  private final Class<?> m_ICheckable;
  private final Class<?> m_Viewer;
  private DatabindingsProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanSupport(ClassLoader classLoader, IModelResolver resolver) {
    m_resolver = resolver;
    m_IObservable =
        CoreUtils.loadClass(classLoader, "org.eclipse.core.databinding.observable.IObservable");
    m_IObservableValue = CoreUtils.loadClass(
        classLoader,
        "org.eclipse.core.databinding.observable.value.IObservableValue");
    m_IObservableList = CoreUtils.loadClass(
        classLoader,
        "org.eclipse.core.databinding.observable.list.IObservableList");
    m_IObservableSet = CoreUtils.loadClass(
        classLoader,
        "org.eclipse.core.databinding.observable.set.IObservableSet");
    m_ISelectionProvider =
        CoreUtils.loadClass(classLoader, "org.eclipse.jface.viewers.ISelectionProvider");
    m_ICheckable = CoreUtils.loadClass(classLoader, "org.eclipse.jface.viewers.ICheckable");
    m_Viewer = CoreUtils.loadClass(classLoader, "org.eclipse.jface.viewers.Viewer");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public DatabindingsProvider getProvider() {
    return m_provider;
  }

  public void setProvider(DatabindingsProvider provider) {
    m_provider = provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link PropertyBindableInfo} properties for given bean {@link Class}.
   */
  public List<PropertyBindableInfo> getProperties(BeanBindableInfo beanObjectInfo) {
    try {
      boolean topLevel = beanObjectInfo instanceof FieldBeanBindableInfo
          || beanObjectInfo instanceof MethodBeanBindableInfo
          || beanObjectInfo instanceof LocalVariableBindableInfo;
      IObserveInfo parent = topLevel ? null : beanObjectInfo;
      Class<?> beanClass = beanObjectInfo.getObjectType();
      // load properties
      List<PropertyBindableInfo> properties = new ArrayList<>();
      boolean version_1_3 =
          Activator.getStore().getBoolean(IPreferenceConstants.GENERATE_CODE_FOR_VERSION_1_3);
      // load properties
      for (PropertyDescriptor descriptor : getPropertyDescriptors(beanClass)) {
        Class<?> propertyType = descriptor.getPropertyType();
        //
        if (topLevel
            && propertyType != null
            && CoreUtils.isAssignableFrom(m_IObservable, propertyType)
            && isGetter(descriptor)) {
          // property with observable (direct) type
          //
          // calculate type
          IObservableFactory.Type directType = getDirectType(propertyType);
          //
          if (directType == null) {
            // unknown observable type
            properties.add(new DirectPropertyBindableInfo(this, parent, descriptor));
          } else {
            // create direct factory
            IObservableFactory observableFactory = DirectObservableFactory.forProperty(directType);
            // create direct property
            DirectPropertyBindableInfo property =
                new DirectPropertyBindableInfo(this, parent, descriptor, observableFactory);
            properties.add(property);
            // add direct observable
            if (m_resolver != null) {
              ObservableInfo directObservable = observableFactory.createObservable(
                  beanObjectInfo,
                  property,
                  directType,
                  version_1_3);
              m_resolver.addModelSupport(new DirectModelSupport(directObservable));
            }
          }
        } else {
          // simple property
          properties.add(new BeanPropertyDescriptorBindableInfo(this, parent, descriptor));
        }
      }
      // sort properties
      Collections.sort(properties, ObserveComparator.INSTANCE);
      //
      // special cases for bean class
      //
      if (topLevel) {
        if (CoreUtils.isAssignableFrom(m_IObservable, beanClass)) {
          // bean class is observable
          //
          // calculate type
          IObservableFactory.Type directType = getDirectType(beanClass);
          //
          if (directType == null) {
            // unknown observable type
            properties.clear();
          } else {
            // create direct factory
            IObservableFactory observableFactory = DirectObservableFactory.forBean(directType);
            // create fake direct property
            DirectPropertyBindableInfo property = new DirectPropertyBindableInfo(this,
                parent,
                getDirectName(directType),
                beanClass,
                StringReferenceProvider.EMPTY,
                observableFactory);
            //
            properties.add(0, property);
            // add direct observable
            if (m_resolver != null) {
              ObservableInfo directObservable = observableFactory.createObservable(
                  beanObjectInfo,
                  property,
                  directType,
                  version_1_3);
              m_resolver.addModelSupport(new DirectFieldModelSupport(directObservable));
            }
            //
            if (directType == IObservableFactory.Type.OnlyValue) {
              // add fake direct detail property
              properties.add(
                  1,
                  new DirectPropertyBindableInfo(this,
                      parent,
                      DirectObservableInfo.DETAIL_PROPERTY_NAME,
                      beanClass,
                      StringReferenceProvider.EMPTY,
                      DirectObservableFactory.forDetailBean()));
            }
          }
        } else if (List.class.isAssignableFrom(beanClass)) {
          // bean class is List
          properties.add(
              0,
              new CollectionPropertyBindableInfo(this,
                  parent,
                  "Collection as WritableList/Properties.selfList()",
                  beanClass,
                  beanObjectInfo.getReferenceProvider()));
        } else if (Set.class.isAssignableFrom(beanClass)) {
          // bean class is Set
          properties.add(
              0,
              new CollectionPropertyBindableInfo(this,
                  parent,
                  "Collection as WritableSet/Properties.selfSet()",
                  beanClass,
                  beanObjectInfo.getReferenceProvider()));
        } else if (!CoreUtils.isAssignableFrom(m_Viewer, beanClass)) {
          boolean selection = false;
          if (CoreUtils.isAssignableFrom(m_ISelectionProvider, beanClass)) {
            selection = true;
            properties.add(
                0,
                new ViewerObservablePropertyBindableInfo(this,
                    parent,
                    "single selection",
                    TypeImageProvider.OBJECT_IMAGE,
                    Object.class,
                    "observeSingleSelection",
                    ViewerObservableFactory.SINGLE_SELECTION,
                    IObserveDecorator.BOLD));
            properties.add(
                1,
                new ViewerObservablePropertyBindableInfo(this,
                    parent,
                    PropertiesSupport.DETAIL_SINGLE_SELECTION_NAME,
                    TypeImageProvider.OBJECT_IMAGE,
                    Object.class,
                    "observeSingleSelection",
                    ViewerObservableFactory.DETAIL_SINGLE_SELECTION,
                    IObserveDecorator.BOLD));
            properties.add(
                2,
                new ViewerObservablePropertyBindableInfo(this,
                    parent,
                    "multi selection",
                    TypeImageProvider.COLLECTION_IMAGE,
                    Object.class,
                    "observeMultiSelection",
                    ViewerObservableFactory.MULTI_SELECTION,
                    IObserveDecorator.BOLD));
          }
          if (CoreUtils.isAssignableFrom(m_ICheckable, beanClass)) {
            properties.add(
                selection ? 3 : 0,
                new ViewerObservablePropertyBindableInfo(this,
                    parent,
                    "checked elements",
                    TypeImageProvider.COLLECTION_IMAGE,
                    Object.class,
                    "observeCheckedElements",
                    ViewerObservableFactory.CHECKED_ELEMENTS,
                    IObserveDecorator.BOLD));
          }
        }
      }
      //
      return properties;
    } catch (Throwable e) {
      DesignerPlugin.log(e);
      return Collections.emptyList();
    }
  }

  private IObservableFactory.Type getDirectType(Class<?> beanClass) {
    if (CoreUtils.isAssignableFrom(m_IObservableValue, beanClass)) {
      return IObservableFactory.Type.OnlyValue;
    }
    if (CoreUtils.isAssignableFrom(m_IObservableList, beanClass)) {
      return IObservableFactory.Type.OnlyList;
    }
    if (CoreUtils.isAssignableFrom(m_IObservableSet, beanClass)) {
      return IObservableFactory.Type.OnlySet;
    }
    return null;
  }

  private static String getDirectName(IObservableFactory.Type type) {
    switch (type) {
      case OnlyValue :
        return "Object as IObservableValue";
      case OnlyList :
        return "Object as IObservableList";
      case OnlySet :
        return "Object as IObservableSet";
    }
    return null;
  }

  /**
   * @return <code>true</code> if given {@link PropertyDescriptor} describe property with
   *         <code>getter</code> method and with not primitive and not array type.
   */
  public static boolean isGetter(PropertyDescriptor descriptor) {
    // check NPE
    if (descriptor == null) {
      return false;
    }
    // check type
    Class<?> type = descriptor.getPropertyType();
    //
    if (type == null || type.isArray() || type.isPrimitive()) {
      return false;
    }
    // check getter method
    return descriptor.getReadMethod() != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Descriptors
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<Class<?>, List<PropertyDescriptor>> m_classToDescriptors = Maps.newHashMap();

  /**
   * @return {@link PropertyDescriptor} properties for given bean {@link Class}.
   */
  public List<PropertyDescriptor> getLocalPropertyDescriptors(Class<?> beanClass) throws Exception {
    List<PropertyDescriptor> descriptors = m_classToDescriptors.get(beanClass);
    if (descriptors == null) {
      descriptors = getPropertyDescriptors(beanClass);
      m_classToDescriptors.put(beanClass, descriptors);
      Collections.sort(descriptors, new Comparator<PropertyDescriptor>() {
        public int compare(PropertyDescriptor descriptor1, PropertyDescriptor descriptor2) {
          return descriptor1.getName().compareTo(descriptor2.getName());
        }
      });
    }
    return descriptors;
  }

  /**
   * @return {@link PropertyDescriptor} properties for given bean {@link Class}.
   */
  public static List<PropertyDescriptor> getPropertyDescriptors(Class<?> beanClass)
      throws Exception {
    List<PropertyDescriptor> descriptors = new ArrayList<>();
    // handle interfaces
    if (beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers())) {
      List<Class<?>> interfaces = CoreUtils.cast(ClassUtils.getAllInterfaces(beanClass));
      for (Class<?> i : interfaces) {
        BeanInfo beanInfo = Introspector.getBeanInfo(i);
        addDescriptors(descriptors, beanInfo.getPropertyDescriptors());
      }
    }
    // handle bean
    BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
    addDescriptors(descriptors, beanInfo.getPropertyDescriptors());
    //
    return descriptors;
  }

  private static void addDescriptors(List<PropertyDescriptor> descriptors,
      PropertyDescriptor[] newDescriptors) {
    int count = descriptors.size();
    if (count > 0) {
      // filter equal properties
      for (int i = 0; i < newDescriptors.length; i++) {
        PropertyDescriptor newDescriptor = newDescriptors[i];
        if (newDescriptor.getPropertyType() == null) {
          continue;
        }
        //
        String name = newDescriptor.getName();
        boolean addDescriptor = true;
        //
        for (int j = 0; j < count; j++) {
          PropertyDescriptor descriptor = descriptors.get(j);
          if (name.equals(descriptor.getName())) {
            addDescriptor = false;
            break;
          }
        }
        if (addDescriptor) {
          descriptors.add(newDescriptor);
        }
      }
    } else {
      // add all properties
      for (PropertyDescriptor descriptor : newDescriptors) {
        if (descriptor.getPropertyType() != null) {
          descriptors.add(descriptor);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Image} represented given bean class.
   */
  public Image getBeanImage(Class<?> beanClass, ObjectInfo javaInfo) throws Exception {
    // check java info
    if (javaInfo != null) {
      return null;
    }
    // prepare cached image
    Image beanImage = m_classToImage.get(beanClass);
    // check load image
    if (beanImage == null) {
      try {
        // load AWT image
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        java.awt.Image awtBeanIcon = beanInfo.getIcon(BeanInfo.ICON_COLOR_16x16);
        if (awtBeanIcon == null) {
          // set default
          beanImage = Activator.getImage("javabean.gif");
        } else {
          // convert to SWT image
          // FIXME: memory leak
          beanImage = ImageUtils.convertToSWT(awtBeanIcon);
        }
      } catch (Throwable e) {
        // set default
        beanImage = Activator.getImage("javabean.gif");
      }
      m_classToImage.put(beanClass, beanImage);
    }
    return beanImage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static IObserveDecorator getDecorator(Class<?> beanClass) {
    if (beanClass == null || beanClass == Class.class || beanClass == Object.class) {
      return IObserveDecorator.HIDDEN;
    }
    if (beanClass == String.class || Collection.class.isAssignableFrom(beanClass)) {
      return IObserveDecorator.BOLD;
    }
    if (beanClass.isArray() || beanClass.getName().startsWith("org.eclipse.")) {
      return IObserveDecorator.ITALIC;
    }
    return IObserveDecorator.DEFAULT;
  }
}