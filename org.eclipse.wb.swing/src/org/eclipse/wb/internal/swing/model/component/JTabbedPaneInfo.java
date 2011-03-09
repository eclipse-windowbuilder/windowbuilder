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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.InvocationChildAssociationAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.awt.Component;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * Model for {@link JTabbedPane}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JTabbedPaneInfo extends ContainerInfo {
  private final StackContainerSupport<ComponentInfo> m_stackContainer =
      new StackContainerSupport<ComponentInfo>(this) {
        @Override
        protected List<ComponentInfo> getChildren() {
          return getChildrenComponents();
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JTabbedPaneInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if orientation is horizontal.
   */
  public boolean isHorizontal() {
    JTabbedPane pane = (JTabbedPane) getObject();
    int tabPlacement = pane.getTabPlacement();
    return tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
  }

  /**
   * @return the array of {@link JTabbedPaneTabInfo} for tabs on this {@link JTabbedPane}.
   */
  public List<JTabbedPaneTabInfo> getTabs() {
    JTabbedPane pane = (JTabbedPane) getObject();
    int tabCount = pane.getTabCount();
    // fill tabs
    List<JTabbedPaneTabInfo> tabs = Lists.newArrayList();
    for (int i = 0; i < tabCount; i++) {
      Component componentObject = pane.getComponentAt(i);
      ComponentInfo component = (ComponentInfo) getChildByObject(componentObject);
      if (component != null) {
        Rectangle bounds = CoordinateUtils.get(pane.getBoundsAt(i));
        tabs.add(new JTabbedPaneTabInfo(this, component, bounds));
      }
    }
    //
    return tabs;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ComponentInfo} for active tab.
   */
  public ComponentInfo getActiveComponent() {
    return m_stackContainer.getActive();
  }

  /**
   * Sets the active {@link ComponentInfo}.
   */
  public void setActiveComponent(ComponentInfo component) {
    m_stackContainer.setActive(component);
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    JTabbedPane pane = (JTabbedPane) getObject();
    // if for some reason tab Component is "null", for example we were not able to evaluate it, remove tab 
    {
      int tabCount = pane.getTabCount();
      for (int i = tabCount - 1; i >= 0; i--) {
        if (pane.getComponentAt(i) == null) {
          pane.remove(i);
        }
      }
    }
    // apply active component
    {
      ComponentInfo activeComponent = getActiveComponent();
      if (activeComponent != null) {
        pane.setSelectedComponent(activeComponent.getComponent());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public List<ObjectInfo> getChildrenGraphical() throws Exception {
      List<ObjectInfo> children = super.getChildrenGraphical();
      // remove all Components's except of active
      ComponentInfo activeComponent = getActiveComponent();
      for (Iterator<ObjectInfo> I = children.iterator(); I.hasNext();) {
        ObjectInfo child = I.next();
        if (child instanceof ComponentInfo && child != activeComponent) {
          I.remove();
        }
      }
      // OK, show these children
      return children;
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    // add listeners
    final JTabbedPaneInfo pane = this;
    addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (child instanceof ComponentInfo && parent == pane) {
          final int newIndex = pane.getChildrenComponents().indexOf(child);
          processAtInvocations(new AtInvocationProcessor() {
            public void process(AstEditor editor, MethodInvocation invocation, int index)
                throws Exception {
              if (index >= newIndex) {
                JTabbedPaneAtAccessor.setAtIndex(editor, invocation, index + 1);
              }
            }
          });
        }
      }
    });
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childMoveAfter(ObjectInfo parent,
          ObjectInfo child,
          ObjectInfo nextChild,
          int oldIndex,
          int newIndex) throws Exception {
        if (child instanceof ComponentInfo && parent == pane) {
          moveUpdateAtInvocations((ComponentInfo) child, oldIndex, newIndex);
        }
      }
    });
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (child instanceof ComponentInfo && parent == pane) {
          removeComponentStatements((ComponentInfo) child);
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        // move OUT
        if (child instanceof ComponentInfo && child.getParent() == pane && newParent != pane) {
          removeComponentStatements((ComponentInfo) child);
        }
      }
    });
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo instanceof ComponentInfo && javaInfo.getParent() == pane) {
          ComponentInfo component = (ComponentInfo) javaInfo;
          // prepare complex "Tab" property
          ComplexProperty complexProperty = (ComplexProperty) component.getArbitraryValue(pane);
          if (complexProperty == null) {
            complexProperty = new ComplexProperty("Tab", "(Tab properties)");
            complexProperty.setCategory(PropertyCategory.system(4));
            complexProperty.setModified(true);
            // set sub-properties
            complexProperty.setProperties(getTabProperties(component));
            // remember
            component.putArbitraryValue(pane, complexProperty);
          }
          // add "Tab" property
          properties.add(complexProperty);
        }
      }
    });
  }

  /**
   * @return the array of "Tab" {@link Property}'s for given {@link ComponentInfo}.
   */
  private Property[] getTabProperties(ComponentInfo component) throws Exception {
    // prepare GenericPropertyDescription's
    Map<String, GenericPropertyDescription> idToProperty = Maps.newTreeMap();
    for (MethodDescription method : getDescription().getMethods()) {
      String signature = method.getSignature();
      //
      for (ParameterDescription parameter : method.getParameters()) {
        String defaultSource = parameter.getDefaultSource();
        //
        String propertyId = parameter.getProperty();
        if (propertyId != null && propertyId.startsWith("tab:")) {
          GenericPropertyDescription description = idToProperty.get(propertyId);
          // create new description
          if (description == null) {
            String title = propertyId.substring("tab:".length()).trim();
            description = new GenericPropertyDescription(propertyId, title);
            description.setConverter(parameter.getConverter());
            description.setEditor(parameter.getEditor());
            // remember description
            idToProperty.put(propertyId, description);
          }
          // add accessor
          if (method.getName().endsWith("At")) {
            // setTitleAt(index,value), setIconAt(index,value), etc
            description.addAccessor(new JTabbedPaneAtAccessor(signature,
                this,
                component,
                defaultSource));
          } else if (method.getName().startsWith("add")) {
            // check for correct (invocation) association, with correct signature 
            if (component.getAssociation() instanceof InvocationChildAssociation) {
              InvocationChildAssociation association =
                  (InvocationChildAssociation) component.getAssociation();
              if (AstNodeUtils.getMethodSignature(association.getInvocation()).equals(signature)) {
                description.addAccessor(new InvocationChildAssociationAccessor(parameter.getIndex(),
                    defaultSource));
              }
            }
          }
        }
      }
    }
    // creation properties
    Property[] properties = new Property[idToProperty.size()];
    int index = 0;
    for (GenericPropertyDescription description : idToProperty.values()) {
      properties[index++] =
          new GenericPropertyImpl(component,
              description.getTitle(),
              description.getAccessorsArray(),
              Property.UNKNOWN_VALUE,
              description.getConverter(),
              description.getEditor());
    }
    //
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ComponentInfo} and associates using given method.
   */
  public final void command_CREATE(ComponentInfo component, ComponentInfo nextComponent)
      throws Exception {
    JavaInfoUtils.add(component, getAssociationObject(), this, nextComponent);
  }

  /**
   * Moves existing child {@link ComponentInfo} to given position.
   */
  public final void command_MOVE(ComponentInfo component, ComponentInfo nextComponent)
      throws Exception {
    if (component.getParent() == this) {
      // before itself
      if (component == nextComponent) {
        return;
      }
      // before already next
      {
        List<ComponentInfo> components = getChildrenComponents();
        ComponentInfo current_nextComponent = GenericsUtils.getNextOrNull(components, component);
        if (nextComponent == current_nextComponent) {
          return;
        }
      }
    }
    // do move
    JavaInfoUtils.move(component, getAssociationObject(), this, nextComponent);
  }

  /**
   * Adds {@link ComponentInfo} from other parent to given position.
   */
  public final void command_ADD(ComponentInfo component, ComponentInfo nextComponent)
      throws Exception {
    command_MOVE(component, nextComponent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the standard {@link AssociationObject} for {@link JTabbedPane}.
   */
  private static AssociationObject getAssociationObject() throws Exception {
    return AssociationObjects.invocationChild(
        "%parent%.addTab(\"New tab\", null, %child%, null)",
        false);
  }

  /**
   * Removes the {@link Statement} for given {@link ComponentInfo}, such as association and
   * "set*At()" invocations.
   */
  private void removeComponentStatements(ComponentInfo component) throws Exception {
    // update set*At() invocations
    {
      final int componentIndex = getChildrenComponents().indexOf(component);
      processAtInvocations(new AtInvocationProcessor() {
        public void process(AstEditor editor, MethodInvocation invocation, int index)
            throws Exception {
          if (index == componentIndex) {
            editor.removeEnclosingStatement(invocation);
          } else if (index > componentIndex) {
            JTabbedPaneAtAccessor.setAtIndex(editor, invocation, index - 1);
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "set*At()" invocations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates each <code>set*At()</code> invocation according move component operation.
   */
  private void moveUpdateAtInvocations(ComponentInfo child, int oldIndex, int newIndex)
      throws Exception {
    Assert.isTrue(oldIndex != newIndex);
    ComponentInfo component = child;
    //
    final StatementTarget target[] = new StatementTarget[1];
    target[0] = JTabbedPaneAtAccessor.getAtTarget(component);
    //
    if (oldIndex > newIndex) {
      final int oldIndex2 = getComponentIndexForObject(oldIndex);
      final int newIndex2 = getComponentIndexForObject(newIndex);
      processAtInvocations(new AtInvocationProcessor() {
        public void process(AstEditor editor, MethodInvocation invocation, int index)
            throws Exception {
          if (index >= newIndex2 && index < oldIndex2) {
            JTabbedPaneAtAccessor.setAtIndex(editor, invocation, index + 1);
          }
          if (index == oldIndex2) {
            JTabbedPaneAtAccessor.setAtIndex(editor, invocation, newIndex2);
            // move statement
            Statement statement = AstNodeUtils.getEnclosingStatement(invocation);
            editor.moveStatement(statement, target[0]);
            // update target
            target[0] = new StatementTarget(statement, false);
          }
        }
      });
    } else {
      final int oldIndex2 = getComponentIndexForObject(oldIndex);
      final int newIndex2 = getComponentIndexForObject(newIndex);
      processAtInvocations(new AtInvocationProcessor() {
        public void process(AstEditor editor, MethodInvocation invocation, int index)
            throws Exception {
          if (index > oldIndex2 && index <= newIndex2) {
            JTabbedPaneAtAccessor.setAtIndex(editor, invocation, index - 1);
          }
          if (index == oldIndex2) {
            JTabbedPaneAtAccessor.setAtIndex(editor, invocation, newIndex2);
            // move statement
            Statement statement = AstNodeUtils.getEnclosingStatement(invocation);
            editor.moveStatement(statement, target[0]);
            // update target
            target[0] = new StatementTarget(statement, false);
          }
        }
      });
    }
  }

  /**
   * @return the index of {@link ComponentInfo} for given index of {@link ObjectInfo} child.
   */
  private int getComponentIndexForObject(int objectIndex) {
    List<ObjectInfo> children = getChildren();
    List<ComponentInfo> components = getChildrenComponents();
    while (true) {
      Object o = children.get(objectIndex++);
      int componentIndex = components.indexOf(o);
      if (componentIndex != -1) {
        return componentIndex;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "set*At()" processing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Processes each <code>set*At()</code> invocation using given {@link AtInvocationProcessor}.
   */
  private void processAtInvocations(AtInvocationProcessor processor) throws Exception {
    AstEditor editor = getEditor();
    for (ASTNode node : getRelatedNodes()) {
      if (node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) node.getParent();
        // check invocation
        if (invocation.getName().getIdentifier().endsWith("At")) {
          int index = JTabbedPaneAtAccessor.getAtIndex(invocation);
          processor.process(editor, invocation, index);
        }
      }
    }
  }

  /**
   * Processor for processing <code>set*At()</code> invocations.
   * 
   * @author scheglov_ke
   */
  private interface AtInvocationProcessor {
    void process(AstEditor editor, MethodInvocation invocation, int index) throws Exception;
  }
}
