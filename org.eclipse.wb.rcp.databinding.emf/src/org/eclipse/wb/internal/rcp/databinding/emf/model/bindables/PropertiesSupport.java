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
package org.eclipse.wb.internal.rcp.databinding.emf.model.bindables;

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.rcp.databinding.emf.Activator;
import org.eclipse.wb.internal.rcp.databinding.emf.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.apache.commons.collections.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Properties provider for EMF objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public class PropertiesSupport {
	private final Map<String, PackageInfo> m_packages = new HashMap<>();
	private final IJavaProject m_javaProject;
	private final ClassLoader m_classLoader;
	private final Class<?> m_EPackage;
	private final Class<?> m_EObject;
	private final Class<?> m_EClass;
	private final Class<?> m_EAttribute;
	private final Class<?> m_EReference;
	private final Class<?> m_EStructuralFeature;
	private Class<?> m_EditingDomain;
	private String m_editingDomainReference;
	private boolean m_isEMFProperties;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertiesSupport(IJavaProject javaProject,
			ClassLoader classLoader,
			List<VariableDeclarationFragment> fragments) throws Exception {
		m_javaProject = javaProject;
		m_classLoader = classLoader;
		// EMF classes
		m_EPackage = m_classLoader.loadClass("org.eclipse.emf.ecore.EPackage");
		m_EObject = m_classLoader.loadClass("org.eclipse.emf.ecore.EObject");
		m_EClass = m_classLoader.loadClass("org.eclipse.emf.ecore.EClass");
		m_EAttribute = m_classLoader.loadClass("org.eclipse.emf.ecore.EAttribute");
		m_EReference = m_classLoader.loadClass("org.eclipse.emf.ecore.EReference");
		m_EStructuralFeature = m_classLoader.loadClass("org.eclipse.emf.ecore.EStructuralFeature");
		// EMF Edit classes
		try {
			m_EditingDomain = m_classLoader.loadClass("org.eclipse.emf.edit.domain.EditingDomain");
			for (VariableDeclarationFragment fragment : fragments) {
				Type type = CoreUtils.getType(fragment, false);
				Class<?> eObjectClass =
						classLoader.loadClass(AstNodeUtils.getFullyQualifiedName(type, true));
				if (m_EditingDomain.isAssignableFrom(eObjectClass)) {
					Assert.isNull(m_editingDomainReference);
					m_editingDomainReference = fragment.getName().getIdentifier();
				}
			}
		} catch (Throwable e) {
		}
		try {
			m_classLoader.loadClass("org.eclipse.emf.databinding.EMFProperties");
			m_isEMFProperties = true;
		} catch (Throwable e) {
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public Class<?> getEObjectClass() {
		return m_EObject;
	}

	public Class<?> getEStructuralFeature() {
		return m_EStructuralFeature;
	}

	public Class<?> getEditingDomainClass() {
		return m_EditingDomain;
	}

	public Class<?> getIObservableSetClass() {
		try {
			return m_classLoader.loadClass("org.eclipse.core.databinding.observable.set.IObservableSet");
		} catch (Throwable e) {
			return null;
		}
	}

	public boolean availableEMFProperties() {
		return m_isEMFProperties;
	}

	public boolean isEMFProperties() {
		return m_isEMFProperties
				&& Activator.getStore().getBoolean(IPreferenceConstants.GENERATE_CODE_FOR_VERSION_2_5);
	}

	public Class<?> getIObservableValue() {
		try {
			return m_classLoader.loadClass("org.eclipse.core.databinding.observable.value.IObservableValue");
		} catch (Throwable e) {
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditingDomain
	//
	////////////////////////////////////////////////////////////////////////////
	public void checkEditingDomain(Expression expression) throws Exception {
		Assert.isNotNull(m_editingDomainReference);
		Assert.isTrue(m_editingDomainReference.equals(CoreUtils.getNodeReference(expression)));
	}

	public boolean isEditingDomainMode() {
		return m_editingDomainReference != null;
	}

	public String getEditingDomainReference() {
		return m_editingDomainReference;
	}

	public String getEMFObservablesCode(String methodStart) {
		if (m_editingDomainReference != null) {
			return " = org.eclipse.emf.databinding.edit.EMFEditObservables."
					+ methodStart
					+ m_editingDomainReference
					+ ", ";
		}
		return " = org.eclipse.emf.databinding.EMFObservables." + methodStart;
	}

	public String getEMFPropertiesCode(String methodStart) {
		if (m_editingDomainReference != null) {
			return " = org.eclipse.emf.databinding.edit.EMFEditProperties."
					+ methodStart
					+ m_editingDomainReference
					+ ", ";
		}
		return " = org.eclipse.emf.databinding.EMFProperties." + methodStart;
	}

	public static String getEMFObservablesCode(BindableInfo bindableObject, String methodStart)
			throws Exception {
		EObjectBindableInfo eObject = (EObjectBindableInfo) bindableObject;
		return eObject.getPropertiesSupport().getEMFObservablesCode(methodStart);
	}

	public static String getEMFPropertiesCode(BindableInfo bindableObject, String methodStart)
			throws Exception {
		EObjectBindableInfo eObject = (EObjectBindableInfo) bindableObject;
		return eObject.getPropertiesSupport().getEMFPropertiesCode(methodStart);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	public List<PropertyInfo> getProperties(Class<?> eObjectClass) throws Exception {
		ClassInfo classInfo = getClassInfo(eObjectClass);
		return classInfo == null ? Collections.<PropertyInfo>emptyList() : classInfo.properties;
	}

	public PropertyInfo getProperty(Class<?> eObjectClass, String propertyName) throws Exception {
		List<PropertyInfo> emfProperties = getProperties(eObjectClass);
		Assert.isTrue(!CollectionUtils.isEmpty(emfProperties));
		//
		for (PropertyInfo property : emfProperties) {
			if (propertyName.equals(property.name)) {
				return property;
			}
		}
		//
		return null;
	}

	/**
	 * @return {@code [ClassInfo, PropertyInfo, String:packagePrefix]}
	 */
	public Object[] getClassInfoForProperty(String emfProperty) throws Exception {
		for (Map.Entry<String, PackageInfo> packageEntry : m_packages.entrySet()) {
			if (emfProperty.startsWith(packageEntry.getKey())) {
				for (Map.Entry<String, ClassInfo> classEntry : packageEntry.getValue().classes.entrySet()) {
					ClassInfo classInfo = classEntry.getValue();
					for (PropertyInfo property : classInfo.properties) {
						if (emfProperty.equals(property.reference)) {
							if (classInfo.thisClass == null) {
								try {
									classInfo.thisClass =
											m_classLoader.loadClass(packageEntry.getKey()
													+ "."
													+ EmfCodeGenUtil.unformat(classInfo.className));
								} catch (Throwable e) {
								}
							}
							return new Object[]{property.parent, property, packageEntry.getKey()};
						}
					}
				}
			}
		}
		return null;
	}

	public ClassInfo getClassInfo(Class<?> eObjectClass) throws Exception {
		PackageInfo packageInfo = getPackageInfo(eObjectClass);
		String emfClassName =
				EmfCodeGenUtil.format(eObjectClass.getSimpleName(), '_', null, true, true).toUpperCase(
						Locale.ENGLISH);
		ClassInfo classInfo = packageInfo.classes.get(emfClassName);
		if (classInfo == null) {
			for (Map.Entry<String, ClassInfo> entry : packageInfo.classes.entrySet()) {
				if (entry.getKey().endsWith(emfClassName)) {
					emfClassName = entry.getKey();
					classInfo = entry.getValue();
					break;
				}
			}
		}
		return classInfo;
	}

	public void addPackage(String packageAnyClass) throws Exception {
		getPackageInfo(packageAnyClass);
	}

	private HierarchySupport m_hierarchySupport = null;

	private PackageInfo getPackageInfo(Class<?> eObjectClass) throws Exception {
		return getPackageInfo(eObjectClass.getName());
	}

	private PackageInfo getPackageInfo(String packageAnyClass) throws Exception {
		String packageName = CodeUtils.getPackage(packageAnyClass);
		PackageInfo packageInfo = m_packages.get(packageName);
		if (packageInfo == null) {
			packageInfo = loadPackage(packageAnyClass);
			if (packageInfo == null) {
				packageInfo = new PackageInfo(packageName);
			} else {
				m_packages.put(packageName, packageInfo);
				// build hierarchy
				boolean processJoin = false;
				if (m_hierarchySupport == null) {
					m_hierarchySupport = new HierarchySupport(this, true);
					processJoin = true;
				}
				for (Map.Entry<String, ClassInfo> entry : packageInfo.classes.entrySet()) {
					ClassInfo classInfo = entry.getValue();
					m_hierarchySupport.addClass(classInfo);
				}
				if (processJoin) {
					m_hierarchySupport.joinClasses();
					m_hierarchySupport = null;
				}
				//packageInfo.status = InfoStatus.LOADED;
			}
		}
		return packageInfo;
	}

	private PackageInfo loadPackage(String packageAnyClass) throws Exception {
		IType packageAnyType = m_javaProject.findType(packageAnyClass);
		if (packageAnyType == null) {
			return null;
		}
		Set<String> allClasses = new HashSet<>();
		IPackageFragment packageFragment = packageAnyType.getPackageFragment();
		// collect all class names
		{
			for (IClassFile classFile : packageFragment.getClassFiles()) {
				IType type = classFile.getType();
				if (type != null) {
					allClasses.add(type.getFullyQualifiedName());
				}
			}
			for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
				IType type = compilationUnit.findPrimaryType();
				if (type != null) {
					allClasses.add(type.getFullyQualifiedName());
				}
			}
		}
		//
		for (IClassFile classFile : packageFragment.getClassFiles()) {
			Map<String, ClassInfo> packageClasses = getPackageClassInfos(classFile.getType(), allClasses);
			if (packageClasses != null) {
				return new PackageInfo(packageFragment.getElementName(), packageClasses);
			}
		}
		for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
			Map<String, ClassInfo> packageClasses =
					getPackageClassInfos(compilationUnit.findPrimaryType(), allClasses);
			if (packageClasses != null) {
				return new PackageInfo(packageFragment.getElementName(), packageClasses);
			}
		}
		return null;
	}

	private Map<String, ClassInfo> getPackageClassInfos(IType packageAnyType, Set<String> allClasses)
			throws JavaModelException, Exception {
		Map<String, ClassInfo> packageClasses = null;
		if (packageAnyType != null) {
			try {
				Class<?> packageClass = m_classLoader.loadClass(packageAnyType.getFullyQualifiedName());
				if (m_EPackage != packageClass && m_EPackage.isAssignableFrom(packageClass)) {
					IType literalsType = packageAnyType.getType("Literals");
					if (literalsType != null) {
						packageClasses = loadEmfClassInfos(literalsType, allClasses);
					}
				}
			} catch (ClassNotFoundException e) {
				// nothing to do
			}
		}
		return packageClasses;
	}

	private Map<String, ClassInfo> loadEmfClassInfos(IType literalsType, Set<String> allClasses)
			throws Exception {
		Class<?> literalsClass = m_classLoader.loadClass(literalsType.getFullyQualifiedName());
		String packageName = literalsClass.getPackage().getName();
		Map<String, ClassInfo> packageClasses = new HashMap<>();
		String literalsReference = literalsType.getFullyQualifiedName('.') + ".";
		List<Field> eClasses = new ArrayList<>();
		List<Field> eProperties = new ArrayList<>();
		// collect literals
		for (Field field : literalsClass.getFields()) {
			Class<?> fieldType = field.getType();
			if (m_EClass.isAssignableFrom(fieldType)) {
				eClasses.add(field);
			} else if (m_EAttribute.isAssignableFrom(fieldType)
					|| m_EReference.isAssignableFrom(fieldType)
					|| m_EStructuralFeature.isAssignableFrom(fieldType)) {
				eProperties.add(field);
			}
		}
		// process EClass & its properties
		for (Field eClassField : eClasses) {
			String fieldName = eClassField.getName();
			ClassInfo classInfo = new ClassInfo(fieldName);
			packageClasses.put(fieldName, classInfo);
			//
			String unformatClassName = packageName + "." + EmfCodeGenUtil.unformat(classInfo.className);
			//
			try {
				classInfo.thisClass = m_classLoader.loadClass(unformatClassName);
			} catch (Throwable e) {
				for (String className : allClasses) {
					if (unformatClassName.equalsIgnoreCase(className)) {
						try {
							classInfo.thisClass = m_classLoader.loadClass(className);
						} catch (Throwable t) {
						}
					}
				}
			}
			//
			String propertyPrefix = fieldName + "__";
			for (Field ePropertyField : eProperties) {
				String propertyName = ePropertyField.getName();
				if (propertyName.startsWith(propertyPrefix)) {
					classInfo.properties.add(new PropertyInfo(classInfo,
							literalsReference + propertyName,
							propertyName.substring(propertyPrefix.length())));
				}
			}
		}
		//
		for (ClassInfo classInfo : packageClasses.values()) {
			linkClassProperties(classInfo);
		}
		//
		return packageClasses;
	}

	private static void linkClassProperties(ClassInfo classInfo) throws Exception {
		if (classInfo.status != InfoStatus.LOADED && classInfo.thisClass != null) {
			classInfo.status = InfoStatus.LOADING;
			List<PropertyInfo> newProperties = new ArrayList<>();
			for (PropertyDescriptor descriptor : BeanSupport.getPropertyDescriptors(classInfo.thisClass)) {
				String propertyName =
						EmfCodeGenUtil.format(descriptor.getName(), '_', null, false, false).toUpperCase(
								Locale.ENGLISH);
				for (PropertyInfo propertyInfo : classInfo.properties) {
					if (propertyName.equals(propertyInfo.internalName)) {
						propertyInfo.name = descriptor.getName();
						propertyInfo.type = descriptor.getPropertyType();
						newProperties.add(propertyInfo);
						classInfo.properties.remove(propertyInfo);
						break;
					}
				}
			}
			Collections.sort(newProperties, new Comparator<PropertyInfo>() {
				@Override
				public int compare(PropertyInfo property1, PropertyInfo property2) {
					return property1.name.compareTo(property2.name);
				}
			});
			//
			classInfo.properties = newProperties;
			classInfo.status = InfoStatus.LOADED;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Classes
	//
	////////////////////////////////////////////////////////////////////////////
	private static enum InfoStatus {
		NEW, LOADING, LOADED
	}
	private static class PackageInfo {
		//InfoStatus status = InfoStatus.NEW;
		//final String packageName;
		final Map<String, ClassInfo> classes;

		PackageInfo(String packageName) {
			this(packageName, new HashMap<>());
		}

		PackageInfo(String packageName, Map<String, ClassInfo> classes) {
			//this.packageName = packageName;
			this.classes = classes;
		}
	}
	public static class ClassInfo {
		InfoStatus status = InfoStatus.NEW;
		public final String className;
		public Class<?> thisClass;
		public List<PropertyInfo> properties = new ArrayList<>();

		ClassInfo(String className) {
			this.className = className;
		}
	}
	public static class PropertyInfo {
		public ClassInfo parent;
		public String name;
		public Class<?> type;
		public String reference;
		public String internalName;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		PropertyInfo(ClassInfo parent, String reference, String internalName) {
			this.parent = parent;
			this.reference = reference;
			this.internalName = internalName;
		}
	}
}