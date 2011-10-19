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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.util.ExposeComponentSupport;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;
import org.eclipse.wb.internal.core.model.util.factory.FactoryActionsSupport;
import org.eclipse.wb.internal.core.model.variable.AbstractNamedVariableSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.live.SwingLiveManager;
import org.eclipse.wb.internal.swing.model.component.top.SwingTopBoundsSupport;
import org.eclipse.wb.internal.swing.utils.SwingImageUtils;
import org.eclipse.wb.internal.swing.utils.SwingScreenshotMaker;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.awt.Component;
import java.awt.Window;
import java.util.List;

/**
 * Model for any AWT {@link Component}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public class ComponentInfo extends AbstractComponentInfo {
  private static final Class<?>[] EXPOSED_TYPES = {java.awt.Component.class};

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds listeners to the {@link BroadcastSupport}.
   */
  private void addBroadcastListeners() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == ComponentInfo.this) {
          ExposeComponentSupport.contribute(
              ComponentInfo.this,
              manager,
              ModelMessages.ComponentInfo_exposeComponent);
          MorphingSupport.contribute("java.awt.Component", ComponentInfo.this, manager);
          FactoryActionsSupport.contribute(ComponentInfo.this, manager);
          RenameConvertSupport.contribute(objects, manager);
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void variable_setName(AbstractNamedVariableSupport variableSupport,
          String oldName,
          String newName) throws Exception {
        if (variableSupport.getJavaInfo() == ComponentInfo.this) {
          setVariableNameAs_setName(newName);
        }
      }
    });
  }

  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    JavaInfoUtils.addExposedChildren(this, EXPOSED_TYPES);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void execRefreshOperation(final RunnableEx runnableEx) throws Exception {
    SwingUtils.runLaterAndWait(runnableEx);
  }

  @Override
  public void refresh_dispose() throws Exception {
    if (getObject() instanceof Window) {
      SwingImageUtils.disposeWindow((Window) getObject());
    }
    super.refresh_dispose();
  }

  /**
   * Invokes {@link #refresh_fetch()} inherited from {@link ObjectInfo}, to process all children.
   */
  protected final void refresh_fetch_super() throws Exception {
    super.refresh_fetch();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    Component component = getComponent();
    refresh_fetch(this, component, new RunnableEx() {
      public void run() throws Exception {
        ComponentInfo.super.refresh_fetch();
      }
    });
  }

  public static void refresh_fetch(AbstractComponentInfo model,
      Component component,
      RunnableEx superRefreshFetch) throws Exception {
    // create shot for top component
    SwingScreenshotMaker screenshotMaker = null;
    try {
      if (!(model instanceof ComponentInfo) || ((ComponentInfo) model).isSwingRoot()) {
        screenshotMaker = new SwingScreenshotMaker(model, component);
        screenshotMaker.makeShots();
      }
      // fetch bounds
      {
        java.awt.Rectangle bounds = component.getBounds();
        // store model bounds
        model.setModelBounds(CoordinateUtils.get(bounds));
        // convert location in parent "bounds" coordinates
        if (model.getParent() instanceof AbstractComponentInfo) {
          AbstractComponentInfo parentModel = (AbstractComponentInfo) model.getParent();
          Object parentObject = parentModel.getComponentObject();
          if (parentObject instanceof Component) {
            Component parentComponent = (Component) parentObject;
            java.awt.Point p_component = SwingUtils.getScreenLocation(component);
            java.awt.Point p_parent = SwingUtils.getScreenLocation(parentComponent);
            bounds.x = p_component.x - p_parent.x;
            bounds.y = p_component.y - p_parent.y;
          }
        }
        // remember bounds
        model.setBounds(CoordinateUtils.get(bounds));
      }
      // fetch preferred size
      model.setPreferredSize(CoordinateUtils.get(component.getPreferredSize()));
      // continue, process children
      if (superRefreshFetch != null) {
        superRefreshFetch.run();
      }
    } finally {
      if (screenshotMaker != null) {
        screenshotMaker.dispose();
      }
    }
  }

  /**
   * @return <code>true</code> if this {@link ComponentInfo} is root of Swing hierarchy. Usually
   *         this means that it is just root {@link ObjectInfo}, but SWT_AWT Frame overrides this.
   */
  protected boolean isSwingRoot() {
    return isRoot();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the AWT {@link Component} object for this model.
   */
  public final Component getComponent() {
    if (getObject() == null && getParent() == null) {
      return getLiveObject();
    }
    return (Component) getObject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new SwingTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rename
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds/updates {@link Component#setName(String)} with new variable name.
   * 
   * @param newName
   *          the new variable name
   */
  private void setVariableNameAs_setName(String newName) throws Exception {
    if (getDescription().getToolkit().getPreferences().getBoolean(
        IPreferenceConstants.P_VARIABLE_IN_COMPONENT)) {
      String valueSource = StringConverter.INSTANCE.toJavaSource(this, newName);
      // try to find existing setName()
      {
        MethodInvocation invocation = getMethodInvocation("setName(java.lang.String)");
        if (invocation != null) {
          Expression valueExpression = (Expression) invocation.arguments().get(0);
          getEditor().replaceExpression(valueExpression, valueSource);
          return;
        }
      }
      // add new setName()
      addMethodInvocation("setName(java.lang.String)", valueSource);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Image getLiveImage() {
    return getLiveComponentsManager().getImage();
  }

  @Override
  protected int getLiveBaseline() {
    return getLiveComponentsManager().getBaseline();
  }

  /**
   * @return the component instance of this component info which is used during creation.
   */
  protected final Component getLiveObject() {
    return getLiveComponentsManager().getComponent();
  }

  /**
   * @return the {@link SwingLiveManager} instance.
   */
  protected SwingLiveManager getLiveComponentsManager() {
    return new SwingLiveManager(this);
  }
}
