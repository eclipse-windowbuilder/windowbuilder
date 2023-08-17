/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.databinding.model.beans;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.ObserveComparator;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.decorate.BeanDecorationInfo;
import org.eclipse.wb.internal.swing.databinding.model.decorate.DecorationUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.utils.SwingImageUtils;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang.ClassUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Properties provider for <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class BeanSupport {
	private final Map<Class<?>, ImageDescriptor> m_classToImage = Maps.newHashMap();
	private boolean m_addELProperty = true;
	private boolean m_addSelfProperty = true;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void doAddELProperty(boolean addElProperty) {
		m_addELProperty = addElProperty;
	}

	public void doAddSelfProperty(boolean addSelfProperty) {
		m_addSelfProperty = addSelfProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
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
				@Override
				public int compare(PropertyDescriptor descriptor1, PropertyDescriptor descriptor2) {
					return descriptor1.getName().compareTo(descriptor2.getName());
				}
			});
		}
		return descriptors;
	}

	public List<ObserveInfo> createProperties(ObserveInfo parent, IGenericType objectType) {
		try {
			Class<?> objectClass = objectType.getRawType();
			BeanDecorationInfo decorationInfo = DecorationUtils.getDecorationInfo(objectClass);
			IDecorationProvider decorationProvider =
					decorationInfo == null ? m_decorationProviderOverType : m_decorationProviderOverInfo;
			List<ObserveInfo> properties = Lists.newArrayList();
			// handle generic
			TypeVariable<?> superTypeParameter = null;
			Type superTypeParameterClass = null;
			if (objectClass.getTypeParameters().length == 1 && objectType.getSubTypes().size() == 1) {
				superTypeParameter = objectClass.getTypeParameters()[0];
				superTypeParameterClass = objectType.getSubTypes().get(0).getRawType();
			} else if (objectClass.getGenericSuperclass() instanceof ParameterizedType) {
				ParameterizedType superType = (ParameterizedType) objectClass.getGenericSuperclass();
				if (superType.getActualTypeArguments().length == 1
						&& superType.getActualTypeArguments()[0] instanceof Class<?>
				&& superType.getRawType() instanceof Class<?>) {
					Class<?> superClass = (Class<?>) superType.getRawType();
					if (superClass.getTypeParameters().length == 1) {
						superTypeParameter = superClass.getTypeParameters()[0];
						superTypeParameterClass = superType.getActualTypeArguments()[0];
					}
				}
			}
			// properties
			for (PropertyDescriptor descriptor : getLocalPropertyDescriptors(objectClass)) {
				String name = descriptor.getName();
				IGenericType propertyType =
						GenericUtils.getObjectType(superTypeParameter, superTypeParameterClass, descriptor);
				properties.add(new BeanPropertyObserveInfo(this,
						parent,
						name,
						propertyType,
						new StringReferenceProvider(name),
						decorationProvider.getDecorator(decorationInfo, propertyType, name, descriptor)));
			}
			// Swing properties
			if (javax.swing.text.JTextComponent.class.isAssignableFrom(objectClass)) {
				replaceProperty(properties, "text", new PropertiesObserveInfo(this,
						parent,
						"text",
						ClassGenericType.STRING_CLASS,
						new StringReferenceProvider("text"),
						IObserveDecorator.BOLD,
						new String[]{"text", "text_ON_ACTION_OR_FOCUS_LOST", "text_ON_FOCUS_LOST"}));
			} else if (javax.swing.JTable.class.isAssignableFrom(objectClass)) {
				addElementProperties(properties, parent);
				Collections.sort(properties, ObserveComparator.INSTANCE);
			} else if (javax.swing.JSlider.class.isAssignableFrom(objectClass)) {
				replaceProperty(properties, "value", new PropertiesObserveInfo(this,
						parent,
						"value",
						ClassGenericType.INT_CLASS,
						new StringReferenceProvider("value"),
						IObserveDecorator.BOLD,
						new String[]{"value", "value_IGNORE_ADJUSTING"}));
			} else if (javax.swing.JList.class.isAssignableFrom(objectClass)) {
				addElementProperties(properties, parent);
				Collections.sort(properties, ObserveComparator.INSTANCE);
			}
			// EL property
			if (m_addELProperty && !objectClass.isPrimitive()) {
				properties.add(0, new ElPropertyObserveInfo(parent, objectType));
			}
			// Object property
			if (m_addSelfProperty && (parent == null || !(parent instanceof BeanPropertyObserveInfo))) {
				properties.add(0, new ObjectPropertyObserveInfo(objectType));
			}
			//
			return properties;
		} catch (Throwable e) {
			DesignerPlugin.log(e);
			return Collections.emptyList();
		}
	}

	private void addElementProperties(List<ObserveInfo> properties, ObserveInfo parent)
			throws Exception {
		properties.add(new PropertiesObserveInfo(this,
				parent,
				"selectedElement",
				ClassGenericType.OBJECT_CLASS,
				new StringReferenceProvider("selectedElement"),
				IObserveDecorator.BOLD,
				new String[]{"selectedElement", "selectedElement_IGNORE_ADJUSTING"}));
		properties.add(new PropertiesObserveInfo(this,
				parent,
				"selectedElements",
				ClassGenericType.LIST_CLASS,
				new StringReferenceProvider("selectedElements"),
				IObserveDecorator.BOLD,
				new String[]{"selectedElements", "selectedElements_IGNORE_ADJUSTING"}));
	}

	private static void replaceProperty(List<ObserveInfo> properties,
			String reference,
			ObserveInfo property) throws Exception {
		int count = properties.size();
		for (int i = 0; i < count; i++) {
			if (reference.equals(properties.get(i).getReference())) {
				properties.set(i, property);
				return;
			}
		}
	}

	public static BeanPropertyObserveInfo getProperty(BindingInfo binding,
			boolean isTarget,
			String property) throws Exception {
		IObserveInfo component = isTarget ? binding.getTarget() : binding.getModel();
		for (IObserveInfo iobserve : component.getChildren(ChildrenContext.ChildrenForPropertiesTable)) {
			ObserveInfo observe = (ObserveInfo) iobserve;
			if (property.equals(observe.getReference())) {
				return (BeanPropertyObserveInfo) observe;
			}
		}
		return null;
	}

	/**
	 * @return {@link PropertyDescriptor} properties for given bean {@link Class}.
	 */
	public static List<PropertyDescriptor> getPropertyDescriptors(Class<?> beanClass)
			throws Exception {
		List<PropertyDescriptor> descriptors = Lists.newArrayList();
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
	 * @return {@link ImageDescriptor} represented given bean class.
	 */
	public ImageDescriptor getBeanImage(Class<?> beanClass, JavaInfo javaInfo, boolean useDefault)
			throws Exception {
		// check java info
		if (javaInfo != null) {
			return null;
		}
		// prepare cached image
		ImageDescriptor beanImage = m_classToImage.get(beanClass);
		// check load image
		if (beanImage == null) {
			try {
				// load AWT image
				BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
				java.awt.Image awtBeanIcon = beanInfo.getIcon(BeanInfo.ICON_COLOR_16x16);
				if (awtBeanIcon == null) {
					// set default
					beanImage = useDefault ? Activator.getImageDescriptor("javabean.gif") : null;
				} else {
					// convert to SWT image
					beanImage = SwingImageUtils.convertImage_AWT_to_SWT(awtBeanIcon);
				}
			} catch (Throwable e) {
				// set default
				beanImage = useDefault ? Activator.getImageDescriptor("javabean.gif") : null;
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
	private final IDecorationProvider m_decorationProviderOverInfo = new IDecorationProvider() {
		@Override
		public IObserveDecorator getDecorator(BeanDecorationInfo decorationInfo,
				IGenericType propertyType,
				String propertyName,
				PropertyDescriptor descriptor) {
			return decorationInfo.getDecorator(propertyName);
		}
	};
	private final IDecorationProvider m_decorationProviderOverType = new IDecorationProvider() {
		@Override
		public IObserveDecorator getDecorator(BeanDecorationInfo decorationInfo,
				IGenericType propertyType,
				String propertyName,
				PropertyDescriptor descriptor) {
			// over PropertyDescriptor
			if (descriptor.isPreferred()) {
				return IObserveDecorator.BOLD;
			}
			if (descriptor.isExpert()) {
				return IObserveDecorator.ITALIC;
			}
			if (descriptor.isHidden()) {
				return IObserveDecorator.HIDDEN;
			}
			// over Class
			Class<?> propertyClass = propertyType.getRawType();
			if (propertyClass == null || propertyClass == Class.class || propertyClass == Object.class) {
				return IObserveDecorator.HIDDEN;
			}
			if (propertyClass == String.class || Collection.class.isAssignableFrom(propertyClass)) {
				return IObserveDecorator.BOLD;
			}
			if (propertyClass.isArray()) {
				return IObserveDecorator.ITALIC;
			}
			//
			return IObserveDecorator.DEFAULT;
		}
	};

	private static interface IDecorationProvider {
		IObserveDecorator getDecorator(BeanDecorationInfo decorationInfo,
				IGenericType propertyType,
				String propertyName,
				PropertyDescriptor descriptor);
	}
}