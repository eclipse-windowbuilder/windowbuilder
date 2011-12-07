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
package org.eclipse.wb.internal.layout.group.model;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.generation.GenerationUtils;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGenerator;
import org.eclipse.wb.internal.core.model.layout.absolute.IImageProvider;
import org.eclipse.wb.internal.layout.group.model.assistant.LayoutAssistantSupport;

import org.eclipse.jface.action.IMenuManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.layoutdesign.VisualMapper;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 * GroupLayout support based on Netbeans engine.
 * 
 * @author mitin_aa
 */
public abstract class GroupLayoutSupport implements IGroupLayoutInfo, LayoutConstants {
  private final GroupLayoutCodeSupport m_codeSupport;
  // netbeans model
  private LayoutModel m_layoutModel;
  private LayoutDesigner m_layoutDesigner;
  private LayoutComponent m_root;
  private JavaInfo m_layout;
  private final VisualMapper m_visualMapper;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutSupport(JavaInfo layout,
      GroupLayoutCodeSupport codeSupport,
      VisualMapper visualMapper) {
    m_layout = layout;
    m_codeSupport = codeSupport;
    m_visualMapper = visualMapper;
    m_layout.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent instanceof JavaInfo) {
          JavaInfo parentJava = (JavaInfo) parent;
          // don't do any processing if parent is deleting.
          if (!parentJava.isDeleting()) {
            if (isOurChild(parent, child) && child instanceof AbstractComponentInfo) {
              command_delete(child);
            }
          }
        }
      }
    });
    m_layout.addBroadcastListener(new JavaEventListener() {
      @Override
      public void bindComponents(List<JavaInfo> components) throws Exception {
        initializeLayout();
        parse();
      }

      private ObjectInfo m_childToDelete;

      @Override
      public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        if (oldParent != newParent && isOurChild(oldParent, child)) {
          m_childToDelete = child;
        }
      }

      @Override
      public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        if (m_childToDelete != null
            && child == m_childToDelete
            && oldParent == getLayoutContainer()
            && newParent != oldParent) {
          try {
            command_delete(child);
          } finally {
            m_childToDelete = null;
          }
        }
      }

      @Override
      public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        if (oldChild == m_layout) {
          // support changing 'main' layout JavaInfo instance, used by Swing while converting from old-style layout.
          m_layout = newChild;
        } else {
          LayoutComponent comp = m_layoutModel.getLayoutComponent(ObjectInfoUtils.getId(oldChild));
          if (comp != null) {
            m_layoutModel.changeComponentId(comp, ObjectInfoUtils.getId(newChild));
          }
        }
      }
    });
    m_layout.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (isRelatedComponent(object)) {
          contributeComponentContextMenu(manager, (AbstractComponentInfo) object);
        }
      }

      @Override
      public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
          throws Exception {
        AbstractAlignmentActionsSupport<?> alignmentSupport = getAlignmentActionsSupport();
        alignmentSupport.addAlignmentActions(objects, actions);
      }

      @Override
      public void refreshed() throws Exception {
        // don't refresh if on 'live' component.
        if (m_layout.getParent() != null && getLayoutDesigner() != null) {
          getLayoutDesigner().updateCurrentState();
        }
      }
    });
    // LA
    new LayoutAssistantSupport(this);
  }

  private boolean isOurChild(ObjectInfo parent, ObjectInfo child) {
    return getLayoutContainer() == parent && parent != null && parent.getChildren().contains(child);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  private void initializeLayout() {
    m_layoutModel = new LayoutModel();
    m_layoutDesigner = new LayoutDesigner(getLayoutModel(), m_visualMapper);
    m_root = new LayoutComponent(ObjectInfoUtils.getId(getLayoutContainer()), true);
    getLayoutModel().addRootComponent(m_root);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse/Save
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void parse() throws Exception {
    m_codeSupport.parse();
    // update associations if needed
    for (JavaInfo javaInfo : getLayoutChildren()) {
      AssociationObject associationObject = getAssociationObject();
      if (associationObject != null) {
        javaInfo.setAssociation(associationObject.getAssociation());
      }
    }
  }

  public void saveLayout() throws Exception {
    List<AbstractComponentInfo> components = getLayoutChildren();
    // set new association if needed
    for (AbstractComponentInfo component : components) {
      AssociationObject associationObject = getAssociationObject();
      if (associationObject != null) {
        Association association = component.getAssociation();
        if (association != null
            && !associationObject.getAssociation().getClass().isAssignableFrom(
                association.getClass())) {
          association.remove();
          component.setAssociation(associationObject.getAssociation());
        }
      }
    }
    m_codeSupport.saveLayout(components);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // New layout setup
  //
  ////////////////////////////////////////////////////////////////////////////
  public void onSet() throws Exception {
    initializeLayout();
    // prepare components
    List<AbstractComponentInfo> components = getLayoutChildren();
    Map<String, Rectangle> boundsMap = Maps.newHashMap();
    for (AbstractComponentInfo component : components) {
      // remove association and set new if needed
      AssociationObject associationObject = getAssociationObject();
      if (associationObject != null) {
        Association association = component.getAssociation();
        if (association != null) {
          association.remove();
        }
        component.setAssociation(associationObject.getAssociation());
      }
      // store bounds by id
      boundsMap.put(
          ObjectInfoUtils.getId(component),
          GroupLayoutUtils.getBoundsInLayout(this, component));
    }
    // do create layout
    getLayoutDesigner().copyLayoutFromOutside(
        boundsMap,
        ObjectInfoUtils.getId(getLayoutContainer()),
        false);
    // update and save
    getLayoutDesigner().updateCurrentState();
    saveLayout();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutComponent createLayoutComponent(AbstractComponentInfo component) {
    return new LayoutComponent(ObjectInfoUtils.getId(component), false);
  }

  /**
   * @return the list of non-deleted children of this container.
   */
  @SuppressWarnings("unchecked")
  public final List<AbstractComponentInfo> getLayoutChildren() {
    return (List<AbstractComponentInfo>) CollectionUtils.select(getComponents(), new Predicate() {
      public boolean evaluate(Object object) {
        JavaInfo javaInfo = (JavaInfo) object;
        return !javaInfo.isDeleted() && !javaInfo.isDeleting();
      }
    });
  }

  protected abstract List<?> getComponents();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands support
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_create(AbstractComponentInfo newObject) throws Exception {
    getLayoutDesigner().endMoving(true);
    addComponentImpl(newObject);
    saveLayout();
  }

  public void command_add(List<AbstractComponentInfo> models) throws Exception {
    getLayoutDesigner().endMoving(true);
    for (AbstractComponentInfo newChild : models) {
      JavaInfoUtils.move(newChild, getAssociationObject(), getLayoutContainer(), null);
    }
    saveLayout();
  }

  public void command_paste(List<JavaInfoMemento> mementos) throws Exception {
    getLayoutDesigner().endMoving(true);
    for (JavaInfoMemento memento : mementos) {
      addComponentImpl((AbstractComponentInfo) memento.create(m_layout));
      memento.apply();
    }
    saveLayout();
  }

  /**
   * Adds new component into this layout, removes {@link BlockStatementGenerator} if needed.
   */
  public final void addComponentImpl(AbstractComponentInfo newComponent) throws Exception {
    StatementGenerator statementGenerator = GenerationUtils.getStatementGenerator(newComponent);
    if (statementGenerator instanceof BlockStatementGenerator) {
      statementGenerator = PureFlatStatementGenerator.INSTANCE;
    }
    JavaInfoUtils.add(
        newComponent,
        GenerationUtils.getVariableSupport(newComponent),
        statementGenerator,
        getAssociationObject(),
        getLayoutContainer(),
        m_layout);
  }

  public void command_commit() throws Exception {
    getLayoutDesigner().endMoving(true);
    saveLayout();
  }

  protected final void command_delete(ObjectInfo child) throws Exception {
    getLayoutModel().removeComponent(ObjectInfoUtils.getId(child), true);
    saveLayout();
  }

  protected abstract AssociationObject getAssociationObject();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup menu/Selection actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeComponentContextMenu(IMenuManager manager, AbstractComponentInfo component) {
    new AnchorsSupport(this).fillContextMenu(component, manager);
    MiscActions.fillContextMenu(this, component, manager);
  }

  private AbstractAlignmentActionsSupport<?> m_alignmentsSupport;

  private AbstractAlignmentActionsSupport<?> getAlignmentActionsSupport() {
    if (m_alignmentsSupport == null) {
      m_alignmentsSupport = new AlignmentsSupport<AbstractComponentInfo>(this);
    }
    return m_alignmentsSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final LayoutDesigner getLayoutDesigner() {
    return m_layoutDesigner;
  }

  public final LayoutModel getLayoutModel() {
    return m_layoutModel;
  }

  protected final LayoutComponent getLayoutRoot() {
    return m_root;
  }

  protected final JavaInfo getLayout() {
    return m_layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  public <T> T getAdapter(Class<T> adapter) {
    if (JavaInfo.class.isAssignableFrom(adapter)) {
      return adapter.cast(m_layout);
    } else if (VisualMapper.class.isAssignableFrom(adapter)) {
      return adapter.cast(m_visualMapper);
    } else if (IImageProvider.class.isAssignableFrom(adapter)) {
      return adapter.cast(getImageProvider());
    }
    return null;
  }

  protected abstract IImageProvider getImageProvider();
}
