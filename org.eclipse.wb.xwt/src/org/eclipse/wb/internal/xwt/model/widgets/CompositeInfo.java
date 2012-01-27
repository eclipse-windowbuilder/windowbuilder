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
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.widgets.ICompositeInfo;
import org.eclipse.wb.internal.xwt.IExceptionConstants;
import org.eclipse.wb.internal.xwt.model.layout.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.CompositeClipboardCommand;
import org.eclipse.wb.internal.xwt.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.support.ControlSupport;
import org.eclipse.wb.internal.xwt.support.CoordinateUtils;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for any {@link Composite} in XWT.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class CompositeInfo extends ScrollableInfo implements ICompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeToContextMenu();
    contributeToClipboardCopy();
    createExplicitAbsoluteLayout();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createExposedChildren() throws Exception {
    super.createExposedChildren();
    createImplicitLayout();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeToContextMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == CompositeInfo.this) {
          fillContextMenu(manager);
        }
      }
    });
  }

  /**
   * Fill context menu {@link IMenuManager}.
   */
  private void fillContextMenu(IMenuManager manager) throws Exception {
    if (hasLayout()) {
      IMenuManager layoutsManager = new MenuManager("Set layout");
      manager.appendToGroup(DesignContextMenuProvider.GROUP_LAYOUT, layoutsManager);
      fillLayoutsManager(layoutsManager);
    }
  }

  /**
   * Fills given {@link IMenuManager} with {@link IAction}s for setting new {@link LayoutInfo} on
   * this {@link CompositeInfo}.
   */
  public void fillLayoutsManager(IMenuManager layoutsManager) throws Exception {
    final EditorContext context = getContext();
    // add "absolute"
    {
      ObjectInfoAction action = new ObjectInfoAction(this) {
        @Override
        protected void runEx() throws Exception {
          LayoutInfo layout = AbsoluteLayoutInfo.createExplicitModel(context);
          setLayout(layout);
        }
      };
      action.setText("Absolute layout");
      action.setImageDescriptor(Activator.getImageDescriptor("info/layout/absolute/layout.gif"));
      layoutsManager.add(action);
    }
    // add layout items
    List<LayoutDescription> descriptions =
        LayoutDescriptionHelper.get(RcpToolkitDescription.INSTANCE);
    for (final LayoutDescription description : descriptions) {
      final String className = description.getLayoutClassName();
      final String creationId = description.getCreationId();
      final ComponentDescription layoutComponentDescription =
          ComponentDescriptionHelper.getDescription(context, className);
      // no GroupLayout
      if (className.equals("org.eclipse.wb.swt.layout.grouplayout.GroupLayout")) {
        continue;
      }
      // add action
      ObjectInfoAction action = new ObjectInfoAction(this) {
        @Override
        protected void runEx() throws Exception {
          description.ensureLibraries(context.getJavaProject());
          LayoutInfo layout =
              (LayoutInfo) XmlObjectUtils.createObject(
                  context,
                  layoutComponentDescription,
                  new ElementCreationSupport(creationId));
          setLayout(layout);
        }
      };
      action.setText(description.getName());
      action.setImageDescriptor(new ImageImageDescriptor(layoutComponentDescription.getIcon()));
      layoutsManager.add(action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private Insets m_clientAreaInsets2;

  /**
   * @return the {@link ControlInfo} children.
   */
  public final List<ControlInfo> getChildrenControls() {
    return getChildren(ControlInfo.class);
  }

  public Insets getClientAreaInsets2() {
    return m_clientAreaInsets2;
  }

  /**
   * @return the current {@link Composite} object.
   */
  public final Composite getComposite() {
    return (Composite) getObject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    m_clientAreaInsets2 = CoordinateUtils.getClientAreaInsets2(getComposite());
    super.refresh_fetch();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LAYOUT_HAS = "layout.has";
  /**
   * We set this key during {@link #setLayout(LayoutInfo)} to prevent implicit {@link LayoutInfo}
   * activation during layout replacement.
   */
  public static final String KEY_LAYOUT_REPLACING = "We replace Layout, don't set implicit now.";

  /**
   * @return <code>true</code> if this {@link CompositeInfo} can have {@link LayoutInfo}.
   */
  public final boolean hasLayout() {
    return XmlObjectUtils.hasTrueParameter(this, KEY_LAYOUT_HAS);
  }

  /**
   * @return the current {@link LayoutInfo} for this composite. Can not return <code>null</code>.
   */
  public final LayoutInfo getLayout() {
    Assert.isTrueException(hasLayout(), IExceptionConstants.NO_LAYOUT_EXPECTED, this);
    // try to find layout
    for (ObjectInfo child : getChildren()) {
      if (child instanceof LayoutInfo) {
        return (LayoutInfo) child;
      }
    }
    // composite that has layout, should always have some layout model
    throw new IllegalStateException("Composite should always have layout");
  }

  /**
   * Creates and adds implicit {@link LayoutInfo}.
   */
  private void createImplicitLayout() throws Exception {
    // may be no layout at all
    if (!hasLayout()) {
      return;
    }
    // create layout model
    LayoutInfo implicitLayout;
    CreationSupport creationSupport = new ImplicitLayoutCreationSupport(this);
    Layout layout = ((Composite) getObject()).getLayout();
    if (layout == null) {
      implicitLayout = new AbsoluteLayoutInfo(getContext(), creationSupport);
    } else {
      Class<?> layoutClass = layout.getClass();
      implicitLayout =
          (LayoutInfo) XmlObjectUtils.createObject(getContext(), layoutClass, creationSupport);
    }
    // add as child
    addChildFirst(implicitLayout);
  }

  /**
   * Waits for parsing complete, and then check for using "null" layout.
   */
  private void createExplicitAbsoluteLayout() {
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        if (getCreationSupport() instanceof ElementCreationSupport
            && getComposite().getLayout() == null
            && getAttribute("layout") != null) {
          LayoutInfo layout = AbsoluteLayoutInfo.createExplicitModel(getContext());
          addChildFirst(layout);
        }
      }
    });
  }

  /**
   * Sets new {@link LayoutInfo}.
   */
  public void setLayout(final LayoutInfo newLayout) throws Exception {
    ExecutionUtils.run(this, new RunnableEx() {
      public void run() throws Exception {
        setLayoutEx(newLayout);
      }
    });
  }

  /**
   * Implementation of {@link #setLayout(LayoutInfo)}.
   */
  private void setLayoutEx(final LayoutInfo newLayout) throws Exception {
    // remove old layout
    {
      LayoutInfo oldLayout = getLayout();
      oldLayout.delete();
    }
    // add model
    if (newLayout instanceof AbsoluteLayoutInfo) {
      addChildFirst(newLayout);
      newLayout.getCreationSupport().addElement(getElement(), 0);
    } else {
      XmlObjectUtils.addFirst(newLayout, Associations.property("layout"), this);
    }
    newLayout.onSet();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean shouldDrawDotsBorder() throws Exception {
    // if has native border, no need to custom one
    {
      Composite composite = getComposite();
      if (ControlSupport.hasStyle(composite, SWT.BORDER)) {
        return false;
      }
    }
    // use script
    String script = XmlObjectUtils.getParameter(this, "shouldDrawBorder");
    if (StringUtils.isEmpty(script)) {
      return false;
    }
    return (Boolean) XmlObjectUtils.executeScript(this, script);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeToClipboardCopy() {
    addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (object == CompositeInfo.this) {
          clipboardCopy_addCommands(commands);
        }
      }
    });
  }

  /**
   * Adds commands for coping this {@link CompositeInfo}.
   */
  protected void clipboardCopy_addCommands(List<ClipboardCommand> commands) throws Exception {
    if (hasLayout()) {
      LayoutInfo layout = getLayout();
      if (XmlObjectUtils.isImplicit(layout)) {
        // no need to set implicit layout
      } else {
        final XmlObjectMemento layoutMemento = XmlObjectMemento.createMemento(layout);
        commands.add(new CompositeClipboardCommand() {
          private static final long serialVersionUID = 0L;

          @Override
          public void execute(CompositeInfo composite) throws Exception {
            LayoutInfo newLayout = (LayoutInfo) layoutMemento.create(composite);
            composite.setLayout(newLayout);
            layoutMemento.apply();
          }
        });
      }
    }
  }
}
