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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.core.model.util.ExposeComponentSupport;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;
import org.eclipse.wb.internal.core.model.util.factory.FactoryActionsSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;
import org.eclipse.wb.os.OSSupportError;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Model for any SWT {@link org.eclipse.swt.widgets.Control}.
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage swt.model.widgets
 */
public class ControlInfo extends WidgetInfo implements IControlInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ControlInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == ControlInfo.this) {
          ExposeComponentSupport.contribute(
              ControlInfo.this,
              manager,
              ModelMessages.ControlInfo_exposeText);
          MorphingSupport.contribute("org.eclipse.swt.widgets.Control", ControlInfo.this, manager);
          FactoryActionsSupport.contribute(ControlInfo.this, manager);
          RenameConvertSupport.contribute(objects, manager);
        }
      }
    });
    StylePropertyEditor.configureContributeActions(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the result of {@link #getParent()} casted to the {@link AbstractComponentInfo}.
   */
  protected final AbstractComponentInfo getParentComponent() {
    return (AbstractComponentInfo) getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    if (isRoot()) {
      ControlSupport.dispose(getObject());
    }
    // call "super"
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    // preferred size, should be here, because "super" applies "top bounds"
    setPreferredSize(ControlSupport.getPreferredSize(getObject()));
    // call "super"
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    refresh_fetch(this, new RunnableEx() {
      @Override
      public void run() throws Exception {
        ControlInfo.super.refresh_fetch();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Image getLiveImage() {
    return getLiveComponentsManager().getImage();
  }

  @Override
  protected final int getLiveBaseline() {
    return getLiveComponentsManager().getBaseline();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @see #refresh_fetch(AbstractComponentInfo, Object, RunnableEx).
   */
  public static void refresh_fetch(AbstractComponentInfo component, RunnableEx superRefreshFetch)
      throws Exception {
    Object control = component.getComponentObject();
    refresh_fetch(component, control, superRefreshFetch);
  }

  /**
   * Performs {@link ControlInfo#refresh_fetch()} operation, but separated from {@link ControlInfo}
   * class.<br>
   * We need such separate method because there {@link AbstractComponentInfo}'s that <em>have</em>
   * <code>Control</code> , but are not <code>Control</code> themselves. For example - Dialog,
   * PreferncePage, ViewPart, etc.
   *
   * @param component
   *          the {@link AbstractComponentInfo} that has <code>Control</code> as object.
   * @param control
   *          the component {@link Object} of given {@link AbstractComponentInfo}.
   * @param superRefreshFetch
   *          the {@link RunnableEx} to invoke "super" of refresh_fetch(), so process children.
   */
  public static void refresh_fetch(AbstractComponentInfo component,
      Object control,
      RunnableEx superRefreshFetch) throws Exception {
    // create shot's for all controls
    boolean wasOSSupportError = false;
    try {
      if (component.isRoot()) {
        ToolkitSupport.beginShot(control);
        createShotImages(component, control);
      }
      // prepare model bounds
      Rectangle modelBounds;
      {
        modelBounds = ControlSupport.getBounds(control);
        component.setModelBounds(modelBounds);
      }
      // prepare shot bounds
      {
        Rectangle bounds = modelBounds.getCopy();
        // convert into "shot"
        if (component.getParent() instanceof AbstractComponentInfo) {
          AbstractComponentInfo parent = (AbstractComponentInfo) component.getParent();
          Object parentControl = parent.getComponentObject();
          if (control != parentControl) {
            Point controlLocation = CoordinateUtils.getDisplayLocation(control);
            Point parentLocation = CoordinateUtils.getDisplayLocation(parentControl);
            bounds.x = controlLocation.x - parentLocation.x;
            bounds.y = controlLocation.y - parentLocation.y;
            // tweak for RIGHT_TO_LEFT
            if (ContainerSupport.isComposite(parentControl)
                && ContainerSupport.isRTL(parentControl)) {
              if (ContainerSupport.isRTL(ControlSupport.getParent(parentControl))) {
                bounds.x += ControlSupport.getBounds(parentControl).width;
              }
              bounds.x -= bounds.width;
            }
          } else {
            bounds.x = 0;
            bounds.y = 0;
          }
        }
        // remember
        component.setBounds(bounds);
      }
      // prepare insets
      if (ContainerSupport.isComposite(control)) {
        component.setClientAreaInsets(CoordinateUtils.getClientAreaInsets(control));
      }
      // continue, process children
      if (superRefreshFetch != null) {
        superRefreshFetch.run();
      }
    } catch (OSSupportError e) {
      // prevent further invoking of 'endShot()'.
      wasOSSupportError = true;
      throw e;
    } finally {
      // finalize screen shot process
      if (component.isRoot() && !wasOSSupportError) {
        ToolkitSupport.endShot(control);
      }
    }
  }

  /**
   * Creates shot {@link Image}'s for all {@link org.eclipse.swt.widgets.Control}'s.
   */
  private static void createShotImages(AbstractComponentInfo root, Object control) throws Exception {
    // mark Control's with models as needed images
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof AbstractComponentInfo) {
          AbstractComponentInfo componentInfo = (AbstractComponentInfo) objectInfo;
          Object componentObject = componentInfo.getComponentObject();
          if (ControlSupport.isControl(componentObject)) {
            ToolkitSupport.markAsNeededImage(componentObject);
          }
        }
      }
    });
    // prepare images
    ToolkitSupport.makeShots(control);
    // get images
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof AbstractComponentInfo) {
          AbstractComponentInfo componentInfo = (AbstractComponentInfo) objectInfo;
          Object componentObject = componentInfo.getComponentObject();
          if (ControlSupport.isControl(componentObject)) {
            Image image = ToolkitSupport.getShotImage(componentObject);
            componentInfo.setImage(image);
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    StylePropertyEditor.addStyleProperties(properties);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(MenuInfo menu) throws Exception {
    menu.command_CREATE(this);
  }

  public void command_ADD(MenuInfo menu) throws Exception {
    menu.command_ADD(this);
  }
}