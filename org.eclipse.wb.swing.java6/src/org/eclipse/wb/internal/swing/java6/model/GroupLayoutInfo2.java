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
package org.eclipse.wb.internal.swing.java6.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IImageProvider;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutClipboardCommand;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutCodeSupport;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutSupport;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;
import org.eclipse.wb.internal.swing.java6.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.swt.graphics.Image;

import org.netbeans.modules.form.layoutdesign.VisualMapper;

import java.util.List;

/**
 * Swing GroupLayout support.
 * 
 * @author mitin_aa
 */
public final class GroupLayoutInfo2 extends LayoutInfo implements IAdaptable {
  public static final String FLAG_IS_MORPHING = "FLAG_IS_MORPHING";
  private GroupLayoutSupport m_layoutSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutInfo2(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        if (oldChild == GroupLayoutInfo2.this) {
          GroupLayoutInfo2 newLayout = (GroupLayoutInfo2) newChild;
          newLayout.m_layoutSupport = m_layoutSupport;
        }
      }
    });
    if (!description.hasTrueTag(FLAG_IS_MORPHING)) {
      m_layoutSupport =
          new SwingGroupLayoutSupport(this,
              new SwingGroupLayoutCodeSupport(this),
              new SwingVisualMapper(this));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy/Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addContainerCommands(List<ClipboardCommand> commands)
      throws Exception {
    super.clipboardCopy_addContainerCommands(commands);
    commands.add(new GroupLayoutClipboardCommand(m_layoutSupport) {
      private static final long serialVersionUID = 0L;

      @Override
      protected GroupLayoutSupport getLayoutSupport(JavaInfo container) {
        ContainerInfo host = (ContainerInfo) container;
        GroupLayoutInfo2 layout = (GroupLayoutInfo2) host.getLayout();
        return layout.m_layoutSupport;
      }
    });
  }

  @Override
  protected void clipboardCopy_addComponentCommands(ComponentInfo component,
      List<ClipboardCommand> commands) throws Exception {
    commands.add(new LayoutClipboardCommand<GroupLayoutInfo2>(component) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(GroupLayoutInfo2 layout, ComponentInfo component) throws Exception {
        layout.m_layoutSupport.addComponentImpl(component);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // New layout setup
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    m_layoutSupport.onSet();
  }

  @Override
  protected void onDelete() throws Exception {
    List<AbstractComponentInfo> components = m_layoutSupport.getLayoutChildren();
    for (AbstractComponentInfo component : components) {
      // add default Swing association back
      InvocationChildAssociation association =
          new InvocationChildAssociation("%parent%.add(%child%)");
      association.add(component, JavaInfoUtils.getTarget(getContainer()), null);
    }
    super.onDelete();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (JavaInfo.class.isAssignableFrom(adapter)) {
      return adapter.cast(this);
    } else if (GroupLayoutSupport.class.isAssignableFrom(adapter)
        || IGroupLayoutInfo.class.isAssignableFrom(adapter)) {
      return adapter.cast(m_layoutSupport);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class ImageProvider implements IImageProvider {
    static final IImageProvider INSTANCE = new ImageProvider();

    @Override
    public Image getImage(String path) {
      return Activator.getImage(path);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Swing GroupLayout Support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class SwingGroupLayoutSupport extends GroupLayoutSupport {
    private SwingGroupLayoutSupport(final GroupLayoutInfo2 layout,
        GroupLayoutCodeSupport codeSupport,
        VisualMapper visualMapper) {
      super(layout, codeSupport, visualMapper);
    }

    @Override
    protected List<?> getComponents() {
      return getGroupLayout().getComponents();
    }

    @Override
    public Insets getContainerInsets() {
      return getGroupLayout().getContainer().getInsets();
    }

    @Override
    public AbstractComponentInfo getLayoutContainer() {
      return getGroupLayout().getContainer();
    }

    @Override
    public boolean isRelatedComponent(ObjectInfo component) {
      return getGroupLayout().isManagedObject(component);
    }

    @Override
    protected AssociationObject getAssociationObject() {
      return new AssociationObject("GroupLayout Empty", new GroupLayoutAssociation(), false);
    }

    @Override
    protected IImageProvider getImageProvider() {
      return ImageProvider.INSTANCE;
    }

    private GroupLayoutInfo2 getGroupLayout() {
      return (GroupLayoutInfo2) getLayout();
    }
  }
}
