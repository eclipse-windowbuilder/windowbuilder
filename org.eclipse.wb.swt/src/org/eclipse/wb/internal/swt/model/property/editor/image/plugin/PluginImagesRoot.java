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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IHasChildren;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageRoot;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;

import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link IImageRoot} for browsing plugin image resources.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public class PluginImagesRoot implements IImageRoot {
  private final IProject m_project;
  private final FilterConfigurer m_filterConfigurer;
  private final IPluginModelBase m_pluginModel;
  private String m_currentValueBundle;
  private ImageContainer[] m_containers;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PluginImagesRoot(IProject project, FilterConfigurer filterConfigurer) {
    m_project = project;
    m_filterConfigurer = filterConfigurer;
    m_pluginModel = PluginRegistry.findModel(project);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageRoot
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IImageElement[] elements() {
    if (m_filterConfigurer.isDirty()) {
      try {
        dispose();
        //
        Set<IPluginModelBase> includeModels = new HashSet<IPluginModelBase>();
        Map<IPluginModelBase, IProject> workspaceModelToProject =
            new HashMap<IPluginModelBase, IProject>();
        //
        List<ImageContainer> containers = new ArrayList<ImageContainer>();
        //
        String thisPluginId = getPluginId(m_pluginModel);
        if (thisPluginId != null) {
          addToContainer(containers, new ProjectImageContainer(m_project, thisPluginId));
        }
        //
        boolean findCurrentValueBundle = m_currentValueBundle != null;
        IPluginModelBase currentValuePluginModel = null;
        if (findCurrentValueBundle) {
          currentValuePluginModel = PluginRegistry.findModel(m_currentValueBundle);
          if (currentValuePluginModel == null || currentValuePluginModel == m_pluginModel) {
            findCurrentValueBundle = false;
            currentValuePluginModel = null;
          }
        }
        //
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        if (m_filterConfigurer.showWorkspacePlugins()) {
          for (IProject project : projects) {
            if (m_project != project && isPluginProject(project)) {
              IPluginModelBase pluginModel = PluginRegistry.findModel(project);
              if (pluginModel != null) {
                String pluginId = getPluginId(pluginModel);
                if (pluginId != null) {
                  includeModels.add(pluginModel);
                  addToContainer(containers, new ProjectImageContainer(project, pluginId));
                }
              }
            }
          }
        } else {
          for (IProject project : projects) {
            if (m_project != project && isPluginProject(project)) {
              IPluginModelBase pluginModel = PluginRegistry.findModel(project);
              if (pluginModel != null) {
                workspaceModelToProject.put(pluginModel, project);
              }
            }
          }
        }
        if (m_filterConfigurer.showRequiredPlugins()) {
          List<String> requiredPlugins = WorkspacePluginInfo.getRequiredPlugins(m_project);
          for (String symbolicName : requiredPlugins) {
            ModelEntry pluginEntry = PluginRegistry.findEntry(symbolicName);
            if (pluginEntry != null) {
              IPluginModelBase pluginModel = pluginEntry.getModel();
              if (pluginEntry.hasWorkspaceModels()) {
                if (!m_filterConfigurer.showWorkspacePlugins()) {
                  IProject project = workspaceModelToProject.get(pluginModel);
                  if (project != null) {
                    String pluginId = getPluginId(pluginModel);
                    if (pluginId != null) {
                      includeModels.add(pluginModel);
                      addToContainer(containers, new ProjectImageContainer(project, pluginId));
                    }
                  }
                }
              } else {
                includeModels.add(pluginModel);
                Bundle bundle = Platform.getBundle(symbolicName);
                addToContainer(containers, new PluginBundleContainer(symbolicName,
                    bundle,
                    symbolicName));
              }
            }
          }
        }
        if (findCurrentValueBundle) {
          if (!includeModels.contains(currentValuePluginModel)) {
            ModelEntry pluginEntry = PluginRegistry.findEntry(m_currentValueBundle);
            if (pluginEntry != null) {
              if (pluginEntry.hasWorkspaceModels()) {
                if (!m_filterConfigurer.showWorkspacePlugins()) {
                  IProject project = workspaceModelToProject.get(currentValuePluginModel);
                  if (project != null) {
                    includeModels.add(currentValuePluginModel);
                    addToContainer(containers, new ProjectImageContainer(project,
                        m_currentValueBundle));
                  }
                }
              } else {
                includeModels.add(currentValuePluginModel);
                Bundle bundle = Platform.getBundle(m_currentValueBundle);
                containers.add(0, new PluginBundleContainer(m_currentValueBundle,
                    bundle,
                    m_currentValueBundle));
              }
            }
          }
          findCurrentValueBundle = false;
          currentValuePluginModel = null;
        }
        IPluginModelBase[] allModels = PluginRegistry.getAllModels();
        if (m_filterConfigurer.showUIPlugins()) {
          for (IPluginModelBase pluginModel : allModels) {
            String pluginId = getPluginId(pluginModel);
            if (pluginId != null
                && pluginId.endsWith(".ui")
                && !includeModels.contains(pluginModel)) {
              Bundle bundle = Platform.getBundle(pluginId);
              addToContainer(containers, new PluginBundleContainer(pluginId, bundle, pluginId));
            }
          }
        } else if (m_filterConfigurer.showAllPlugins()) {
          for (IPluginModelBase pluginModel : allModels) {
            if (!includeModels.contains(pluginModel)) {
              String pluginId = getPluginId(pluginModel);
              if (pluginId != null) {
                Bundle bundle = Platform.getBundle(pluginId);
                addToContainer(containers, new PluginBundleContainer(pluginId, bundle, pluginId));
              }
            }
          }
        }
        //
        Collections.sort(containers, new Comparator<ImageContainer>() {
          @Override
          public int compare(ImageContainer container1, ImageContainer container2) {
            return container1.getName().compareTo(container2.getName());
          }
        });
        m_containers = containers.toArray(new ImageContainer[containers.size()]);
        //
        m_filterConfigurer.resetState();
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
    return m_containers;
  }

  private static void addToContainer(List<ImageContainer> containers, ImageContainer container) {
    IHasChildren tester = (IHasChildren) container;
    if (tester.hasChildren()) {
      containers.add(container);
    }
  }

  public void init(Object data) {
    if (data != null) {
      String[] parts = (String[]) data;
      m_currentValueBundle = parts[0];
    }
  }

  @Override
  public void dispose() {
    if (m_containers != null) {
      for (ImageContainer container : m_containers) {
        container.dispose();
      }
      m_containers = null;
    }
  }

  @Override
  public Object[] getSelectionPath(Object data) {
    String[] parts = (String[]) data;
    String symbolicName = parts[0];
    String imagePath = parts[1];
    //
    elements();
    for (ImageContainer container : m_containers) {
      Object[] resource = container.findResource(symbolicName, imagePath);
      if (resource != null) {
        return resource;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the ID of plugin, may be <code>null</code>.
   */
  private static String getPluginId(IPluginModelBase pluginModel) {
    BundleDescription bundleDescription = pluginModel.getBundleDescription();
    return bundleDescription == null ? null : bundleDescription.getSymbolicName();
  }

  /**
   * @return <code>true</code> if given {@link IProject} is plugin project and <code>false</code>
   *         otherwise.
   */
  private static boolean isPluginProject(IProject project) throws Exception {
    return project.exists()
        && project.isOpen()
        && project.hasNature("org.eclipse.pde.PluginNature")
        && (project.getFile(WorkspacePluginInfo.MANIFEST_PATH).exists() || project.getFile(
            WorkspacePluginInfo.PLUGIN_PATH).exists());
  }

  /**
   * @return <code>true</code> if given {@link IProject} is plugin project and <code>false</code>
   *         otherwise.
   */
  public static boolean testPluginProject(IProject project) {
    try {
      return isPluginProject(project);
    } catch (Throwable e) {
      return false;
    }
  }
}