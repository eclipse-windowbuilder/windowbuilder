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
package org.eclipse.wb.internal.core.model.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.order.ComponentOrder;
import org.eclipse.wb.internal.core.model.order.ComponentOrderDefault;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.order.MethodOrderAfterCreation;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description of any {@link JavaInfo}, its constructors, methods, etc.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public class ComponentDescription extends AbstractDescription implements IComponentDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentDescription(ComponentDescriptionKey key) {
    m_key = key;
    if (m_key != null) {
      m_componentClass = m_key.getComponentClass();
      m_description = m_componentClass.getName();
    } else {
      m_componentClass = null;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Key
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ComponentDescriptionKey m_key;

  /**
   * @return the {@link ComponentDescriptionKey} of this {@link ComponentDescription}.
   */
  public ComponentDescriptionKey getKey() {
    return m_key;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    String methods_toString = ", m=" + m_methods;
    String parameters_toString = ", p=" + m_parameters;
    return m_modelClass.getName()
        + "("
        + m_componentClass.getName()
        + methods_toString
        + parameters_toString
        + ")";
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Cached
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_cached;

  /**
   * @return <code>true</code> if this {@link ComponentDescription} is statically cached.
   */
  public boolean isCached() {
    return m_cached;
  }

  /**
   * Marks this {@link ComponentDescription} as statically cached.
   */
  public void setCached(boolean cached) {
    m_cached = cached;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Caching presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_presentationCached;

  /**
   * @return <code>true</code> if {@link ComponentPresentation} for this
   *         {@link ComponentDescription} can be cached.
   */
  public boolean isPresentationCached() {
    return m_presentationCached;
  }

  /**
   * Specifies if {@link ComponentPresentation} for this {@link ComponentDescription} can be cached.
   */
  public void setPresentationCached(boolean presentationCached) {
    m_presentationCached = presentationCached;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI toolkit
  //
  ////////////////////////////////////////////////////////////////////////////
  private ToolkitDescriptionJava m_toolkit;

  public ToolkitDescriptionJava getToolkit() {
    return m_toolkit;
  }

  /**
   * Sets the {@link ToolkitDescription} for this component.
   */
  public void setToolkit(ToolkitDescription toolkit) {
    m_toolkit = (ToolkitDescriptionJava) toolkit;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Component class
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Class<?> m_componentClass;

  public Class<?> getComponentClass() {
    return m_componentClass;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model class
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> m_modelClass;

  public Class<?> getModelClass() {
    return m_modelClass;
  }

  /**
   * Sets the {@link Class} of {@link JavaInfo} successor class that should be used for this
   * component.
   */
  public void setModelClass(Class<?> modelClass) {
    m_modelClass = modelClass;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // BeanInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  private BeanInfo m_beanInfo;

  /**
   * @return the {@link BeanInfo} of this component.
   */
  public BeanInfo getBeanInfo() {
    return m_beanInfo;
  }

  /**
   * Sets the {@link BeanInfo} of this component.
   */
  public void setBeanInfo(BeanInfo beanInfo) {
    Assert.isNull(m_beanInfo);
    m_beanInfo = beanInfo;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // BeanDescriptor
  //
  ////////////////////////////////////////////////////////////////////////////
  private BeanDescriptor m_beanDescriptor;

  /**
   * @return the consolidated {@link BeanDescriptor} of this component.
   */
  public BeanDescriptor getBeanDescriptor() {
    return m_beanDescriptor;
  }

  /**
   * Sets the consolidated {@link BeanDescriptor} of this component.
   */
  public void setBeanDescriptor(BeanDescriptor beanDescriptor) {
    m_beanDescriptor = beanDescriptor;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyDescriptors
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<PropertyDescriptor> m_propertyDescriptors;

  /**
   * @return all {@link PropertyDescriptor}'s, including protected.
   */
  public List<PropertyDescriptor> getPropertyDescriptors() {
    return m_propertyDescriptors;
  }

  /**
   * Sets all {@link PropertyDescriptor}'s, including protected.
   */
  public void setPropertyDescriptors(List<PropertyDescriptor> propertyDescriptors) {
    m_propertyDescriptors = propertyDescriptors;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ConstructorDescription> m_constructors = Lists.newArrayList();

  /**
   * @return the list of {@link ConstructorDescription}'s of this component.
   */
  public List<ConstructorDescription> getConstructors() {
    return m_constructors;
  }

  /**
   * Adds new {@link ConstructorDescription}.
   */
  public void addConstructor(ConstructorDescription constructor) {
    int index = m_constructors.indexOf(constructor);
    if (index != -1) {
      ConstructorDescription existingConstructor = m_constructors.get(index);
      existingConstructor.join(constructor);
    } else {
      {
        Constructor<?> reflectionConstructor =
            ReflectionUtils.getConstructorBySignature(m_componentClass, constructor.getSignature());
        Assert.isNotNull2(
            reflectionConstructor,
            "{0} has no constructor with signature {1}",
            m_componentClass,
            constructor.getSignature());
      }
      m_constructors.add(constructor);
    }
  }

  /**
   * Note: supported generics.
   *
   * @return the {@link ConstructorDescription} for given {@link IMethodBinding}.
   */
  public ConstructorDescription getConstructor(IMethodBinding methodBinding) {
    if (!methodBinding.isConstructor()) {
      return null;
    }
    ConstructorDescription constructorDescription;
    {
      String signature = AstNodeUtils.getMethodSignature(methodBinding);
      constructorDescription = getConstructor(signature);
    }
    if (constructorDescription == null) {
      String signature = AstNodeUtils.getMethodDeclarationSignature(methodBinding);
      constructorDescription = getConstructor(signature);
    }
    return constructorDescription;
  }

  /**
   * Note: not supported generics.
   *
   * @return the {@link ConstructorDescription} for given signature.
   */
  @Deprecated
  public ConstructorDescription getConstructor(String signature) {
    // try to find method with same signature
    for (ConstructorDescription constructor : m_constructors) {
      if (constructor.getSignature().equals(signature)) {
        return constructor;
      }
    }
    // not found
    return null;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<MethodDescription> m_methods = Lists.newArrayList();
  private final Map<String, MethodDescription> m_methodsMap = Maps.newTreeMap();

  /**
   * @return the list of {@link MethodDescription}'s of this component.
   */
  public List<MethodDescription> getMethods() {
    return m_methods;
  }

  /**
   * Adds new {@link MethodDescription} based on given reflection {@link Method}.
   */
  public MethodDescription addMethod(Method method) throws Exception {
    String methodSignature = ReflectionUtils.getMethodSignature(method);
    MethodDescription methodDescription = getMethod(methodSignature);
    if (methodDescription == null) {
      methodDescription = new MethodDescription(method);
      addMethod(methodDescription);
    }
    return methodDescription;
  }

  /**
   * Adds new {@link MethodDescription}.
   */
  public void addMethod(MethodDescription method) {
    int index = m_methods.indexOf(method);
    if (index != -1) {
      m_methods.get(index).join(method);
    } else {
      m_methods.add(method);
    }
    m_methodsMap.clear();
  }

  /**
   * @return the {@link MethodDescription} from given list corresponding to given
   *         {@link IMethodBinding}.
   */
  public MethodDescription getMethod(IMethodBinding methodBinding) {
    String signature = AstNodeUtils.getMethodSignature(methodBinding);
    return getMethod(signature);
  }

  /**
   * @return the {@link MethodDescription} for method with given signature.
   */
  public MethodDescription getMethod(String signature) {
    // fill cache
    if (m_methodsMap.isEmpty()) {
      for (MethodDescription method : m_methods) {
        String methodSignature = method.getSignature();
        m_methodsMap.put(methodSignature, method);
      }
    }
    // use cache
    return m_methodsMap.get(signature);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Method invocation order
  //
  ////////////////////////////////////////////////////////////////////////////
  private MethodOrder m_defaultMethodOrder = new MethodOrderAfterCreation();

  /**
   * @return the default {@link MethodOrder} for all methods of this component.
   */
  public MethodOrder getDefaultMethodOrder() {
    return m_defaultMethodOrder;
  }

  /**
   * Sets the default {@link MethodOrder} for all methods of this component.
   */
  public void setDefaultMethodOrder(MethodOrder defaultMethodOrder) {
    m_defaultMethodOrder = defaultMethodOrder;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Order
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComponentOrder m_order = ComponentOrderDefault.INSTANCE;

  /**
   * @return current {@link ComponentOrder}. Can not return <code>null</code>.
   */
  public ComponentOrder getOrder() {
    return m_order;
  }

  /**
   * Sets new {@link ComponentOrder} using specification.
   */
  public void setOrder(String specification) {
    m_order = ComponentOrder.parse(specification);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Generic properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<GenericPropertyDescription> m_properties = Lists.newArrayList();
  private final Map<String, GenericPropertyDescription> m_idToProperty = Maps.newHashMap();

  /**
   * @return the {@link GenericPropertyDescription}'s of this component.
   */
  public List<GenericPropertyDescription> getProperties() {
    return Collections.unmodifiableList(m_properties);
  }

  /**
   * @return the {@link GenericPropertyDescription} with given id, can not return <code>null</code>.
   *
   * @throws RuntimeException
   *           if no {@link GenericPropertyDescription} with such id is registered.
   */
  public GenericPropertyDescription getProperty(String id) {
    GenericPropertyDescription property = m_idToProperty.get(id);
    if (property == null && !EnvironmentUtils.isTestingTime()) {
      // fix for descriptions of non-existing properties
      property = new GenericPropertyDescription(id, "NO_SUCH_PROPERTY");
    }
    Assert.isNotNull(property, "Can not find property with id '" + id + "'.");
    return property;
  }

  /**
   * Adds (and registers) new {@link GenericPropertyDescription}.
   */
  public void addProperty(GenericPropertyDescription property) {
    String id = property.getId();
    GenericPropertyDescription existingProperty = m_idToProperty.get(id);
    if (existingProperty == null) {
      registerProperty(property);
      m_properties.add(property);
    } else {
      // TODO add separate test
      existingProperty.join(property);
    }
  }

  /**
   * Registers {@link GenericPropertyDescription} so that it can be accessed using
   * {@link #getProperty(String)}, but don't added them to the list of top level of component
   * properties.
   */
  public void registerProperty(GenericPropertyDescription property) {
    String id = property.getId();
    m_idToProperty.put(id, property);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Configurable properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, ConfigurablePropertyDescription> m_idToConfigurableProperty =
      Maps.newHashMap();

  /**
   * @return the {@link GenericPropertyDescription}'s of this component.
   */
  public Collection<ConfigurablePropertyDescription> getConfigurableProperties() {
    return m_idToConfigurableProperty.values();
  }

  /**
   * Adds new {@link ConfigurablePropertyDescription}.
   */
  public void addConfigurableProperty(ConfigurablePropertyDescription property) {
    m_idToConfigurableProperty.put(property.getId(), property);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, String> m_parameters = Maps.newTreeMap();

  /**
   * Adds new parameter.
   */
  public void addParameter(String name, String value) {
    m_parameters.put(name, value);
  }

  /**
   * @return the read only {@link Map} of parameters.
   */
  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(m_parameters);
  }

  /**
   * @return the value of parameter with given name.
   */
  public String getParameter(String name) {
    return m_parameters.get(name);
  }

  /**
   * @return <code>true</code> description has parameter with value "true".
   */
  public boolean hasTrueParameter(String name) {
    String parameter = getParameter(name);
    return "true".equals(parameter);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  private CreationDescription m_creationDefault;
  private final Map<String, CreationDescription> m_creations = Maps.newHashMap();

  /**
   * @return all {@link CreationDescription}'s.
   */
  public List<CreationDescription> getCreations() {
    List<CreationDescription> creations = Lists.newArrayList(m_creations.values());
    creations.add(m_creationDefault);
    return creations;
  }

  /**
   * @return the {@link CreationDescription} for this component with given <code>creationId</code>.
   */
  public CreationDescription getCreation(String creationId) {
    CreationDescription creation = m_creations.get(creationId);
    if (creation != null) {
      return creation;
    }
    return m_creationDefault;
  }

  /**
   * Sets default, inheritable {@link CreationDescription}.
   */
  public void setCreationDefault(CreationDescription creationDefault) {
    m_creationDefault = creationDefault;
  }

  /**
   * Adds the {@link CreationDescription} for component with exactly same class.
   */
  public void addCreation(CreationDescription creation) {
    m_creations.put(creation.getId(), creation);
  }

  /**
   * Removes all existing {@link CreationDescription}'s.
   */
  public void clearCreations() {
    m_creations.clear();
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed children
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ExposingRule> m_exposingRules = Lists.newArrayList();

  /**
   * @return the {@link List} of {@link ExposingRule}'s.
   */
  public List<ExposingRule> getExposingRules() {
    return m_exposingRules;
  }

  /**
   * Adds new {@link ExposingRule} into beginning of rules {@link List}, so that it will be checked
   * first. Such order gives ability to fine-tune exposing rules in sub-classes.
   *
   * @param rule
   *          the {@link ExposingRule} to add.
   */
  public void addExposingRule(ExposingRule rule) {
    m_exposingRules.add(0, rule);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<MorphingTargetDescription> m_morphingTargets = Lists.newArrayList();

  /**
   * @return the {@link MorphingTargetDescription}'s registered for this component.
   */
  public List<MorphingTargetDescription> getMorphingTargets() {
    return m_morphingTargets;
  }

  /**
   * Registers new {@link MorphingTargetDescription}.
   */
  public void addMorphingTarget(MorphingTargetDescription morphingTarget) {
    m_morphingTargets.add(morphingTarget);
  }

  /**
   * Clear registered {@link MorphingTargetDescription}'s list.
   */
  public void clearMorphingTargets() {
    m_morphingTargets.clear();
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Description
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_description;

  /**
   * @return the description text for this component.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Sets the description text for this component.
   */
  public void setDescription(String description) {
    if (description != null) {
      m_description = StringUtilities.normalizeWhitespaces(description);
      m_description = StringUtils.replace(m_description, "\\n", "\n");
    } else {
      m_description = m_componentClass.getName();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Post processing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Post process {@link GenericPropertyDescription}'s.
   */
  public void joinProperties() throws Exception {
    // update constructors
    for (ConstructorDescription constructor : getConstructors()) {
      for (ParameterDescription parameter : constructor.getParameters()) {
        // try to set editor from mapped property
        {
          String propertyId = parameter.getProperty();
          if (propertyId != null) {
            GenericPropertyDescription property = getProperty(propertyId);
            PropertyEditor editor = property.getEditor();
            if (editor != null) {
              parameter.setEditor(editor);
            }
          }
        }
      }
    }
    // configure "Object" properties
    useObjectPropertyEditor();
    // remove properties without editors
    for (Iterator<GenericPropertyDescription> I = m_properties.iterator(); I.hasNext();) {
      GenericPropertyDescription property = I.next();
      if (property.getEditor() == null) {
        I.remove();
        m_idToProperty.remove(property.getId());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void useObjectPropertyEditor() {
    for (GenericPropertyDescription property : m_properties) {
      // may be has some special editor
      if (property.getEditor() != null) {
        continue;
      }
      // use Object editor
      if (shouldUseObjectPropertyEditor(property)) {
        property.setEditor(ObjectPropertyEditor.INSTANCE);
        if (property.getCategory() == PropertyCategory.NORMAL) {
          property.setCategory(PropertyCategory.ADVANCED);
        }
      }
    }
  }

  private static boolean shouldUseObjectPropertyEditor(GenericPropertyDescription property) {
    if (property.hasTrueTag("useObjectEditor")) {
      return true;
    }
    Method setter = property.getSetter();
    return setter != null && !isStandardMethod(setter);
  }

  /**
   * @return <code>true</code> if given {@link Method} is declared in standard toolkit component.
   */
  private static boolean isStandardMethod(Method setter) {
    String declaringClassName = setter.getDeclaringClass().getName();
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(
            "org.eclipse.wb.core.standardToolkitPackages",
            "package");
    for (IConfigurationElement element : elements) {
      String prefix = ExternalFactoriesHelper.getRequiredAttribute(element, "prefix");
      if (declaringClassName.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Current class
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> m_currentClass;

  /**
   * @return the currently parsing class.
   */
  public Class<?> getCurrentClass() {
    return m_currentClass;
  }

  /**
   * Sets the currently parsing class.
   */
  public void setCurrentClass(Class<?> currentClass) {
    m_currentClass = currentClass;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Icon
  //
  ////////////////////////////////////////////////////////////////////////////
  private Image m_icon;

  public Image getIcon() {
    return m_icon;
  }

  /**
   * Sets the icon for this component.
   */
  public void setIcon(Image icon) {
    m_icon = icon;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void visit(JavaInfo javaInfo, int state) throws Exception {
    super.visit(javaInfo, state);
    for (GenericPropertyDescription property : m_idToProperty.values()) {
      property.visit(javaInfo, state);
    }
  }
}
