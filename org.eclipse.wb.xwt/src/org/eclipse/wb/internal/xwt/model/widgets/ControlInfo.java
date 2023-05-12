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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;
import org.eclipse.wb.internal.xwt.parser.XwtRenderer;
import org.eclipse.wb.internal.xwt.support.CoordinateUtils;
import org.eclipse.wb.internal.xwt.support.ToolkitSupport;
import org.eclipse.wb.os.OSSupportError;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Model for any {@link Control} in XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class ControlInfo extends WidgetInfo implements IControlInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ControlInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Control} instance.
   */
  public final Control getControl() {
    return (Control) getObject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new CompositeTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    if (isRoot()) {
      Control control = getControl();
      if (control != null && !control.isDisposed()) {
        control.getShell().dispose();
      }
    }
    // call "super"
    super.refresh_dispose();
  }

  @Override
  protected void refresh_create() throws Exception {
    XwtRenderer renderer = new XwtRenderer(this);
    try {
      renderer.render();
    } finally {
      renderer.dispose();
    }
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    // preferred size, should be here, because "super" applies "top bounds"
    setPreferredSize(CoordinateUtils.getPreferredSize(getControl()));
    // call "super"
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    refresh_fetch(this, new RunnableEx() {
      public void run() throws Exception {
        ControlInfo.super.refresh_fetch();
      }
    });
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
    Control control = (Control) component.getComponentObject();
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
      Control control,
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
        modelBounds = CoordinateUtils.getBounds(control);
        component.setModelBounds(modelBounds);
      }
      // prepare shot bounds
      {
        Rectangle bounds = modelBounds.getCopy();
        // convert into "shot"
        Control parentControl = getParentControl(component);
        if (parentControl != null) {
          Point controlLocation = CoordinateUtils.getDisplayLocation(control);
          Point parentLocation = CoordinateUtils.getDisplayLocation(parentControl);
          bounds.x = controlLocation.x - parentLocation.x;
          bounds.y = controlLocation.y - parentLocation.y;
        }
        // remember
        component.setBounds(bounds);
      }
      // prepare insets
      if (control instanceof Composite) {
        Composite composite = (Composite) control;
        component.setClientAreaInsets(CoordinateUtils.getClientAreaInsets(composite));
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

  private static Control getParentControl(AbstractComponentInfo component) {
    ObjectInfo parent = component.getParent();
    while (parent instanceof AbstractComponentInfo) {
      Object parentObject = ((AbstractComponentInfo) parent).getComponentObject();
      if (parentObject instanceof Control) {
        return (Control) parentObject;
      }
      parent = parent.getParent();
    }
    return null;
  }

  /**
   * Creates shot {@link Image}'s for all {@link org.eclipse.swt.widgets.Control}'s.
   */
  private static void createShotImages(AbstractComponentInfo root, Control rootControl)
      throws Exception {
    // mark Control's with models as needed images
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof AbstractComponentInfo) {
          AbstractComponentInfo componentInfo = (AbstractComponentInfo) objectInfo;
          Object componentObject = componentInfo.getComponentObject();
          if (componentObject instanceof Control) {
            ToolkitSupport.markAsNeededImage((Control) componentObject);
          }
        }
      }
    });
    // prepare images
    ToolkitSupport.makeShots(rootControl);
    // get images
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof AbstractComponentInfo) {
          AbstractComponentInfo componentInfo = (AbstractComponentInfo) objectInfo;
          Object componentObject = componentInfo.getComponentObject();
          if (componentObject instanceof Control) {
            Image image = ToolkitSupport.getShotImage((Control) componentObject);
            componentInfo.setImage(image);
          }
        }
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
}
