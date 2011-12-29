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
package org.eclipse.wb.internal.swing.model.layout.absolute;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.BoundsProperty;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Model for abstract absolute layout.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swing.model.layout
 */
public abstract class AbstractAbsoluteLayoutInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractAbsoluteLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    // context menu
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (isManagedObject(object)) {
          ComponentInfo component = (ComponentInfo) object;
          contributeComponentContextMenu(manager, component);
        }
      }
    });
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo instanceof ComponentInfo && javaInfo.getParent() == getContainer()) {
          ComponentInfo component = (ComponentInfo) javaInfo;
          properties.add(getBoundsProperty(component));
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void canMove(JavaInfo javaInfo, boolean[] forceMoveEnable, boolean[] forceMoveDisable)
          throws Exception {
        if (javaInfo instanceof ComponentInfo && javaInfo.getParent() == getContainer()) {
          forceMoveEnable[0] = true;
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes {@link Action}'s into {@link ComponentInfo} context menu.
   */
  private void contributeComponentContextMenu(IMenuManager manager, final ComponentInfo component) {
    // order
    {
      List<ComponentInfo> components = getContainer().getChildrenComponents();
      new OrderingSupport(components, component).contributeActions(manager);
    }
    // auto-size
    {
      IAction action =
          new ObjectInfoAction(component, ModelMessages.AbstractAbsoluteLayoutInfo_autosize,
              DesignerPlugin.getImageDescriptor("info/layout/absolute/fit_to_size.png")) {
            @Override
            protected void runEx() throws Exception {
              command_BOUNDS(component, null, component.getPreferredSize());
            }
          };
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds Property
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void setBoundsX(ComponentInfo component, int value) throws Exception;

  protected abstract void setBoundsY(ComponentInfo component, int value) throws Exception;

  protected abstract void setBoundsWidth(ComponentInfo component, int value) throws Exception;

  protected abstract void setBoundsHeight(ComponentInfo component, int value) throws Exception;

  private Property getBoundsProperty(ComponentInfo component) {
    ComplexProperty boundsProperty = (ComplexProperty) component.getArbitraryValue(this);
    if (boundsProperty == null) {
      boundsProperty = new ComplexProperty("Bounds", null);
      boundsProperty.setCategory(PropertyCategory.system(5));
      boundsProperty.setModified(true);
      component.putArbitraryValue(this, boundsProperty);
      // x
      BoundsProperty<?> xProperty = new BoundsProperty<ComponentInfo>(component, "x") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          setBoundsX(m_component, value);
        }
      };
      // y
      BoundsProperty<?> yProperty = new BoundsProperty<ComponentInfo>(component, "y") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          setBoundsY(m_component, value);
        }
      };
      // width
      BoundsProperty<?> widthProperty = new BoundsProperty<ComponentInfo>(component, "width") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          setBoundsWidth(m_component, value);
        }
      };
      // height
      BoundsProperty<?> heightProperty = new BoundsProperty<ComponentInfo>(component, "height") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          setBoundsHeight(m_component, value);
        }
      };
      // set properties
      boundsProperty.setProperties(new Property[]{
          xProperty,
          yProperty,
          widthProperty,
          heightProperty});
    }
    Rectangle modelBounds = component.getModelBounds();
    if (modelBounds != null) {
      boundsProperty.setText(MessageFormat.format(
          "({0}, {1}, {2}, {3})",
          modelBounds.x,
          modelBounds.y,
          modelBounds.width,
          modelBounds.height));
    }
    return boundsProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    for (ComponentInfo component : getContainer().getChildrenComponents()) {
      Rectangle bounds = component.getBounds();
      command_BOUNDS(component, bounds.getLocation(), bounds.getSize());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Modifies location/size values by modifying appropriate "setLocation", "setSize", "setBounds"
   * arguments.
   * 
   * @param component
   *          the {@link ComponentInfo} which modifications applies to.
   * @param location
   *          the {@link Point} of new location of component. May be null.
   * @param size
   *          the {@link Dimension} of new size of component. May be null.
   */
  public abstract void command_BOUNDS(ComponentInfo component, Point location, Dimension size)
      throws Exception;

  /**
   * Performs create operation.
   */
  public void command_CREATE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    add(component, null, nextComponent);
  }

  /**
   * Performs reorder operation.
   */
  public void command_MOVE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    move(component, null, nextComponent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addComponentCommands(ComponentInfo component,
      List<ClipboardCommand> commands) throws Exception {
    final Rectangle bounds = component.getModelBounds();
    commands.add(new LayoutClipboardCommand<AbstractAbsoluteLayoutInfo>(component) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(AbstractAbsoluteLayoutInfo layout, ComponentInfo component)
          throws Exception {
        layout.command_CREATE(component, null);
        layout.command_BOUNDS(component, bounds.getLocation(), bounds.getSize());
      }
    });
  }
}
