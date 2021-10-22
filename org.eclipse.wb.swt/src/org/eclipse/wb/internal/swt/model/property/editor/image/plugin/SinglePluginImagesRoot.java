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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageRoot;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;

/**
 * Implementation of {@link IImageRoot} for browsing plugin image resources.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public class SinglePluginImagesRoot implements IImageRoot {
  private final String m_symbolicName;
  private final ImageContainer[] m_containers = new ImageContainer[1];

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SinglePluginImagesRoot(IProject project) {
    IPluginModelBase pluginModel = PluginRegistry.findModel(project);
    m_symbolicName = pluginModel.getBundleDescription().getSymbolicName();
    m_containers[0] = new ProjectImageContainer(project, m_symbolicName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageRoot
  //
  ////////////////////////////////////////////////////////////////////////////
  public IImageElement[] elements() {
    return m_containers;
  }

  public void dispose() {
    ImageContainer container = m_containers[0];
    if (container != null) {
      container.dispose();
      m_containers[0] = null;
    }
  }

  public Object[] getSelectionPath(Object data) {
    String imagePath = (String) data;
    Object[] resource = m_containers[0].findResource(m_symbolicName, imagePath);
    if (resource != null) {
      return resource;
    }
    return null;
  }
}