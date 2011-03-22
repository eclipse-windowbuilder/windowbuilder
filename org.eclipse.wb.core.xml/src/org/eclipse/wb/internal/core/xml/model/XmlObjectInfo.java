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
package org.eclipse.wb.internal.core.xml.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.hierarchy.ComponentClassProperty;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.AbstractDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.event.EventsProperty;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jdt.core.IJavaProject;

import java.util.List;
import java.util.Map;

/**
 * Model for any object in XML.
 * 
 * @author scheglov_ke
 * @coverage XML.model
 */
public class XmlObjectInfo extends ObjectInfo {
  /**
   * We mark components that user drops from palette with this flag to be able distinguish them from
   * objects that created as consequence of user operation.
   */
  public static final String FLAG_MANUAL_COMPONENT = "manuallyCreatedComponent";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final EditorContext m_context;
  private final ComponentDescription m_description;
  private CreationSupport m_creationSupport;
  private Object m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlObjectInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    m_context = context;
    m_description = description;
    m_creationSupport = creationSupport;
    setBroadcastSupport(context.getBroadcastSupport());
    m_creationSupport.setObject(this);
    // properties
    {
      Class<?> componentClass = description.getComponentClass();
      if (componentClass == null) {
        m_componentClassProperty = null;
        m_eventsProperty = null;
      } else {
        IJavaProject javaProject = context.getJavaProject();
        m_componentClassProperty = new ComponentClassProperty(javaProject, componentClass);
        m_eventsProperty = new EventsProperty(this);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObjectPresentation getPresentation() {
    return new XmlObjectPresentation(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link EditorState}.
   */
  public EditorContext getContext() {
    return m_context;
  }

  /**
   * @return the {@link ComponentDescription}.
   */
  public final ComponentDescription getDescription() {
    return m_description;
  }

  /**
   * @return the {@link CreationSupport}.
   */
  public final CreationSupport getCreationSupport() {
    return m_creationSupport;
  }

  /**
   * Sets new {@link CreationSupport}.
   * <p>
   * This rare operation, for example we use it to "materialize" implicit object.
   */
  public final void setCreationSupport(CreationSupport creationSupport) throws Exception {
    m_creationSupport = creationSupport;
    m_creationSupport.setObject(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XML hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link XmlObjectInfo} parent.
   */
  public final XmlObjectInfo getParentXML() {
    return (XmlObjectInfo) getParent();
  }

  /**
   * @return the {@link XmlObjectInfo} root.
   */
  public final XmlObjectInfo getRootXML() {
    return (XmlObjectInfo) getRoot();
  }

  /**
   * @return the list of {@link XmlObjectInfo} children.
   */
  public final List<XmlObjectInfo> getChildrenXML() {
    return getChildren(XmlObjectInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<GenericProperty> m_descriptionBasedProperties;
  private final EventsProperty m_eventsProperty;
  private final ComponentClassProperty m_componentClassProperty;

  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = Lists.newArrayList();
    // add description based properties
    if (m_descriptionBasedProperties == null) {
      m_descriptionBasedProperties = Lists.newArrayList();
      for (GenericPropertyDescription description : getDescription().getProperties()) {
        GenericProperty property = new GenericPropertyImpl(this, description);
        m_descriptionBasedProperties.add(property);
      }
    }
    properties.addAll(m_descriptionBasedProperties);
    // add class property
    if (m_componentClassProperty != null) {
      properties.add(m_componentClassProperty);
    }
    // add events (only if there are events)
    if (PropertyUtils.getChildren(m_eventsProperty).length != 0) {
      properties.add(m_eventsProperty);
    }
    // add hierarchy properties
    getBroadcast(ObjectInfoAddProperties.class).invoke(this, properties);
    getBroadcast(XmlObjectAddProperties.class).invoke(this, properties);
    // return properties
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void saveEdit() throws Exception {
    m_context.commit();
  }

  /**
   * This method is invoked as response on {@link ObjectEventListener#dispose()}, i.e. when we close
   * editor and dispose this model hierarchy.
   */
  protected void onHierarchyDispose() throws Exception {
    if (isRoot()) {
      m_context.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_objectReadySent = false;

  /**
   * @return the {@link Object} created for this {@link XmlObjectInfo}.
   */
  public final Object getObject() {
    return m_object;
  }

  /**
   * Sets the {@link Object} created for this {@link XmlObjectInfo}.
   */
  public final void setObject(Object object) throws Exception {
    m_object = object;
    // send "ready" to description
    if (!isObjectReadySent()) {
      setObjectReadySent(true);
      m_description.visit(this, AbstractDescription.STATE_OBJECT_READY);
    }
    // initialize
    if (!m_initialized) {
      initialize();
    }
    // notify
    getBroadcast(XmlObjectSetObjectAfter.class).invoke(this, m_object);
  }

  /**
   * @return the {@link XmlObjectInfo} with same object as given.
   */
  public final XmlObjectInfo getChildByObject(final Object o) {
    if (o == null) {
      return null;
    }
    if (o == m_object) {
      return this;
    }
    for (ObjectInfo child : getChildren()) {
      if (child instanceof XmlObjectInfo) {
        XmlObjectInfo xmlChild = (XmlObjectInfo) child;
        XmlObjectInfo result = xmlChild.getChildByObject(o);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Is <code>true</code> if this {@link XmlObjectInfo} already initialized. We check this flag in
   * {@link #setObject(Object)} and do initialization if needed.
   */
  private boolean m_initialized;

  /**
   * Initializes newly created {@link XmlObjectInfo} (it should have "live" object). This is good
   * place to create any exposed or implicit components.
   */
  protected void initialize() throws Exception {
    m_initialized = true;
    createExposedChildren();
    // external participators
    {
      List<IXMLObjectInitializationParticipator> participators =
          ExternalFactoriesHelper.getElementsInstances(
              IXMLObjectInitializationParticipator.class,
              "org.eclipse.wb.core.xml.initializationParticipators",
              "participator");
      for (IXMLObjectInitializationParticipator participator : participators) {
        participator.process(this);
      }
    }
  }

  /**
   * Adds any exposed components as direct or indirect children of this {@link XmlObjectInfo}.
   */
  protected void createExposedChildren() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, Object> m_attributeValues = Maps.newHashMap();

  /**
   * Registers value for attribute during rendering. Resolves the attribute (adds namespace
   * qualifier if applicable).
   */
  public final void registerAttributeValue(String attribute, Object value) {
    m_attributeValues.put(attribute, value);
  }

  /**
   * @return the attribute value, remembered earlier during rendering. Value <code>null</code> is
   *         just value, not flag that there are no value. If no value for attribute, then
   *         {@link Property#UNKNOWN_VALUE} will be returned.
   */
  public final Object getAttributeValue(String attribute) {
    if (m_attributeValues.containsKey(attribute)) {
      return m_attributeValues.get(attribute);
    }
    return Property.UNKNOWN_VALUE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes raw
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the underlying {@link DocumentElement}.
   */
  public final DocumentElement getElement() {
    return getCreationSupport().getElement();
  }

  /**
   * @return the raw attribute value, same as {@link DocumentElement#getAttribute(String)}.
   */
  public String getAttribute(String name) {
    return getElement().getAttribute(name);
  }

  /**
   * Sets the raw attribute value, same as {@link DocumentElement#setAttribute(String, String)}.
   */
  public void setAttribute(String name, String value) {
    getElement().setAttribute(name, value);
  }

  /**
   * Removes raw attribute from {@link DocumentElement}.
   */
  public void removeAttribute(String name) {
    getElement().setAttribute(name, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    m_object = null;
    m_attributeValues.clear();
    setObjectReadySent(false);
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    XmlObjectUtils.executeScriptParameter(this, "refresh_afterCreate");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Print
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return getCreationSupport().toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    // check if CreationSupport can delete
    if (!m_creationSupport.canDelete()) {
      return false;
    }
    // OK
    return true;
  }

  @Override
  public void delete() throws Exception {
    final ObjectInfo parent = getParent();
    ObjectInfo hierarchyObject = parent != null ? parent : this;
    ExecutionUtils.run(hierarchyObject, new RunnableEx() {
      public void run() throws Exception {
        putArbitraryValue(FLAG_DELETING, Boolean.TRUE);
        try {
          // broadcast "before"
          getBroadcast(ObjectInfoDelete.class).before(parent, XmlObjectInfo.this);
          // delete creation
          m_creationSupport.delete();
          // broadcast "after"
          getBroadcast(ObjectInfoDelete.class).after(parent, XmlObjectInfo.this);
        } finally {
          removeArbitraryValue(FLAG_DELETING);
        }
      }
    });
  }

  public final void setObjectReadySent(boolean value) {
    m_objectReadySent = value;
  }

  public final boolean isObjectReadySent() {
    return m_objectReadySent;
  }
}
