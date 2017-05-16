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
package org.eclipse.wb.internal.core.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ITopBoundsSupport;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;

/**
 * Implementations of this class support different modes for size of top level
 * {@link AbstractComponentInfo}. <li>From properties of file.</li> <li>From source - using existing
 * setSize()/setBounds() or may be pack().</li>
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public abstract class TopBoundsSupport implements ITopBoundsSupport {
  private static final QualifiedName KEY_DESIGN_BOUNDS =
      new QualifiedName(DesignerPlugin.PLUGIN_ID, "designBounds");
  protected final AbstractComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TopBoundsSupport(AbstractComponentInfo componentInfo) {
    m_component = componentInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Applies size to component.
   */
  public abstract void apply() throws Exception;

  public abstract void setSize(int width, int height) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows component for testing/preview.
   *
   * @return <code>true</code> if something really was shown, so reparse required.
   */
  public abstract boolean show() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IResource} for this component.
   */
  protected final IResource getUnderlyingResource() throws Exception {
    return m_component.getEditor().getModelUnit().getUnderlyingResource();
  }

  /**
   * @return <code>true</code> if component has invocation with one of the given signatures.
   */
  protected final boolean hasMethodInvocations(String[] signatures) throws Exception {
    for (int i = 0; i < signatures.length; i++) {
      String signature = signatures[i];
      if (m_component.getMethodInvocation(signature) != null) {
        return true;
      }
    }
    // not found
    return false;
  }

  /**
   * @return the size remembered in {@link IResource} properties for this component.
   */
  protected final Dimension getResourceSize() throws Exception {
    int width;
    int height;
    // set defaults
    {
      Dimension defaultSize = getDefaultSize();
      width = defaultSize.width;
      height = defaultSize.height;
    }
    // check for size from resource properties
    {
      String designBoundsString = getUnderlyingResource().getPersistentProperty(KEY_DESIGN_BOUNDS);
      if (designBoundsString != null) {
        String[] parts = StringUtils.split(designBoundsString);
        width = Integer.parseInt(parts[0]);
        height = Integer.parseInt(parts[1]);
      }
    }
    // ensure positive size
    width = Math.max(width, 1);
    height = Math.max(height, 1);
    // final size
    return new Dimension(width, height);
  }

  /**
   * Remembers the size of component into {@link IResource} properties for this component.
   */
  protected final void setResourceSize(int width, int height) throws Exception {
    String designBoundsString = width + " " + height;
    getUnderlyingResource().setPersistentProperty(KEY_DESIGN_BOUNDS, designBoundsString);
  }

  /**
   * @return the default size for this component.
   */
  protected Dimension getDefaultSize() {
    IPreferenceStore preferences = m_component.getDescription().getToolkit().getPreferences();
    int width = preferences.getInt(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_WIDTH);
    int height = preferences.getInt(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_HEIGHT);
    return new Dimension(width, height);
  }
}
