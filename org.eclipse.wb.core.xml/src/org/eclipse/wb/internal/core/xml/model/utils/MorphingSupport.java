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
package org.eclipse.wb.internal.core.xml.model.utils;

import org.eclipse.wb.internal.core.model.description.ComponentPresentation;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.AbstractMorphingSupport;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.description.ComponentPresentationHelper;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Helper for morphing {@link XmlObjectInfo} for one component class to another.
 * 
 * @author sablin_aa
 * @coverage core.model.util
 */
public abstract class MorphingSupport<T extends XmlObjectInfo> extends AbstractMorphingSupport<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final EditorContext m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected MorphingSupport(String toolkitClassName, T component) {
    super(toolkitClassName, component);
    m_editor = m_component.getContext();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IJavaProject getJavaProject() {
    return m_editor.getJavaProject();
  }

  @Override
  protected ClassLoader getClassLoader() {
    return m_editor.getClassLoader();
  }

  @Override
  protected Class<?> getComponentClass() {
    return m_component.getDescription().getComponentClass();
  }

  @Override
  protected List<MorphingTargetDescription> getMorphingTargets() {
    return m_component.getDescription().getMorphingTargets();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTargetText(MorphingTargetDescription target) throws Exception {
    return getComponentPresentation(target).getName();
  }

  @Override
  protected ImageDescriptor getTargetImageDescriptor(MorphingTargetDescription target)
      throws Exception {
    ComponentPresentation presentation = getComponentPresentation(target);
    return new ImageImageDescriptor(presentation.getIcon());
  }

  private ComponentPresentation getComponentPresentation(MorphingTargetDescription target)
      throws Exception {
    return ComponentPresentationHelper.getPresentation(
        m_editor,
        target.getComponentClass().getName(),
        target.getCreationId());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If possible, contributes "morph" actions.
   * 
   * @param toolkitClassName
   *          the name of base class for "Other..." action, for example
   *          <code>"org.eclipse.swt.widgets.Control"</code> as SWT.
   * @param component
   *          the {@link XmlObjectInfo} that should be morphed.
   * @param manager
   *          the {@link IContributionManager} to add action to.
   */
  public static void contribute(String toolkitClassName,
      XmlObjectInfo component,
      IContributionManager manager) throws Exception {
    // add "morph" actions
    MorphingSupport<XmlObjectInfo> morphingSupport =
        new MorphingSupport<XmlObjectInfo>(toolkitClassName, component) {
        };
    contribute(morphingSupport, manager);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void morph(MorphingTargetDescription target) throws Exception {
    if (m_component.getCreationSupport() instanceof ElementCreationSupport) {
      super.morph(target);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T morph_create(MorphingTargetDescription target) throws Exception {
    Class<?> componentClass = target.getComponentClass();
    ComponentDescription newDescription =
        ComponentDescriptionHelper.getDescription(m_editor, componentClass);
    // prepare new component
    CreationSupport creationSupport =
        new ElementCreationSupport(m_component.getCreationSupport().getElement());
    return (T) XmlObjectUtils.createObject(m_editor, newDescription, creationSupport);
  }

  @Override
  protected void morph_replace(T newComponent) throws Exception {
    /* TODO m_component.getBroadcastJava().replaceChildBefore(
    m_component.getParentJava(),
    m_component,
    newComponent);*/
    // replace component in parent (following operations may require parent)
    m_component.getParent().replaceChild(m_component, newComponent);
  }

  @Override
  protected void morph_properties(T newComponent) throws Exception {
    for (Property property : m_component.getProperties()) {
      if (newComponent.getPropertyByTitle(property.getTitle()) != null) {
        // keep property value
        continue;
      }
      if (property.isModified()) {
        // reset to default
        property.setValue(Property.UNKNOWN_VALUE);
      }
    }
  }

  @Override
  protected void morph_children(T newComponent) throws Exception {
    for (XmlObjectInfo child : m_component.getChildrenXML()) {
      newComponent.addChild(child);
    }
  }

  @Override
  protected void morph_finish(T newComponent) throws Exception {
    // replace element type
    CreationSupport creationSupport = newComponent.getCreationSupport();
    Class<?> componentClass = newComponent.getDescription().getComponentClass();
    creationSupport.getElement().setTag(CodeUtils.getShortClass(componentClass.getName()));
    //
    /* TODO m_component.getBroadcastJava().replaceChildAfter(
        m_component.getParentJava(),
        m_component,
        newComponent);*/
  }
}
