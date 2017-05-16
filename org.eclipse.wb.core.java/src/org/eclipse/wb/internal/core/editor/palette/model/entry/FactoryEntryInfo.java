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
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ComponentPresentation;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentPresentationHelper;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract {@link EntryInfo} for static/instance "factory-method" contribution.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class FactoryEntryInfo extends ToolEntryInfo {
  protected String m_factoryClassName;
  protected String m_methodSignature;
  private Image m_icon;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryEntryInfo() {
  }

  public FactoryEntryInfo(CategoryInfo categoryInfo,
      String factoryClassName,
      AttributesProvider attributes) {
    // factory class
    {
      m_factoryClassName = factoryClassName;
      Assert.isNotNull(m_factoryClassName);
    }
    // signature
    {
      m_methodSignature = attributes.getAttribute("signature");
      Assert.isNotNull(m_methodSignature, "Factory method must have 'signature' attribute.");
    }
    // id
    {
      String id = attributes.getAttribute("id");
      if (id == null) {
        id = categoryInfo.getId() + " " + m_factoryClassName + " " + m_methodSignature;
      }
      setId(id);
    }
    // name
    {
      String name = attributes.getAttribute("name");
      setName(name);
    }
    // other
    setDescription(attributes.getAttribute("description"));
    setVisible(getBoolean(attributes, "visible", true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name of factory class.
   */
  public final String getFactoryClassName() {
    return m_factoryClassName;
  }

  /**
   * Sets the name of factory class.
   */
  public final void setFactoryClassName(String factoryClassName) {
    m_factoryClassName = factoryClassName;
  }

  /**
   * @return the signature of method.
   */
  public final String getMethodSignature() {
    return m_methodSignature;
  }

  /**
   * Sets the signature of method.
   */
  public final void setMethodSignature(String methodSignature) {
    m_methodSignature = methodSignature;
  }

  /**
   * This method exists mostly for tests.
   *
   * @return the {@link FactoryMethodDescription} or this {@link FactoryEntryInfo}.
   */
  public FactoryMethodDescription getMethodDescription() {
    return m_methodDescription;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  protected Class<?> m_factoryClass;
  protected FactoryMethodDescription m_methodDescription;
  private ComponentPresentation m_presentation;
  private ComponentDescription m_componentDescription;

  /**
   * @return <code>true</code> if static factory methods should be used.
   */
  protected abstract boolean isStaticFactory();

  @Override
  public final boolean initialize(IEditPartViewer editPartViewer, JavaInfo rootJavaInfo) {
    super.initialize(editPartViewer, rootJavaInfo);
    // check for factory Class in classpath
    if (!ProjectUtils.hasType(m_javaProject, m_factoryClassName)) {
      return false;
    }
    // prepare descriptions
    try {
      {
        m_factoryClass = m_state.getEditorLoader().loadClass(m_factoryClassName);
        m_methodDescription =
            FactoryDescriptionHelper.getDescription(
                m_editor,
                m_factoryClass,
                m_methodSignature,
                isStaticFactory());
        if (m_methodDescription == null) {
          return false;
        }
      }
      {
        Class<?> componentClass = m_methodDescription.getReturnClass();
        m_presentation =
            ComponentPresentationHelper.getPresentation(
                m_editor,
                componentClass.getCanonicalName(),
                null);
      }
    } catch (ClassNotFoundException e) {
      // not initialized because no Class, don't add warning
      return false;
    } catch (Throwable e) {
      String message = "Palette: can not load factory method " + toString();
      m_state.addWarning(new EditorWarning(message, e));
      return false;
    }
    // updates
    {
      // update entry icon
      if (m_methodDescription.getIcon() != null) {
        m_icon = m_methodDescription.getIcon();
      } else {
        m_icon = m_presentation.getIcon();
      }
      // update entry name
      {
        String name = getNameRaw();
        // if empty, try "name" from description
        if (StringUtils.isEmpty(name) || name.equals(m_methodSignature)) {
          String presentationName = m_methodDescription.getPresentationName();
          if (presentationName != null) {
            setName(presentationName);
          }
        }
        // default
        if (getNameRaw() == null) {
          setName(m_methodSignature);
        }
      }
      // update entry description text
      {
        String description = getDescription();
        if (StringUtils.isEmpty(description) || description.startsWith("Class: ")) {
          setDescription(m_methodDescription.getDescription());
        }
        if (getDescription() == null) {
          setDescription("Class: " + m_factoryClassName + "<br/>Method: " + m_methodSignature);
        }
      }
    }
    // OK, can activate
    return true;
  }

  protected final boolean ensureComponentDescription() {
    if (m_componentDescription == null) {
      try {
        m_componentDescription =
            ComponentDescriptionHelper.getDescription(m_editor, m_methodDescription);
      } catch (Throwable e) {
        String message = "Palette: can not load factory method " + toString();
        m_state.addWarning(new EditorWarning(message, e));
        return false;
      }
    }
    // OK, can activate
    return true;
  }

  @Override
  public final Image getIcon() {
    return m_icon;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new {@link JavaInfo} for this component.
   */
  protected final JavaInfo createJavaInfo(CreationSupport creationSupport) throws Exception {
    JavaInfo javaInfo =
        JavaInfoUtils.createJavaInfo(m_editor, m_componentDescription, creationSupport);
    return JavaInfoUtils.getWrapped(javaInfo);
  }
}
