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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Composite;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

/**
 * Model for {@link CardLayout}.
 * 
 * @author lobas_av
 * @coverage swing.model.layout
 */
public final class CardLayoutInfo extends LayoutInfo {
  private final StackContainerSupport<ComponentInfo> m_stackContainer =
      new StackContainerSupport<ComponentInfo>(this) {
        @Override
        protected boolean isActive() {
          return CardLayoutInfo.this.isActive();
        }

        @Override
        protected ObjectInfo getContainer() {
          return CardLayoutInfo.this.getContainer();
        }

        @Override
        protected List<ComponentInfo> getChildren() {
          return getComponents();
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CardLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CardLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the AWT {@link CardLayout} object for this model.
   */
  private CardLayout getLayout() {
    return (CardLayout) getObject();
  }

  public ComponentInfo getCurrentComponent() {
    return m_stackContainer.getActive();
  }

  public ComponentInfo getPrevComponent() {
    return m_stackContainer.getPrev();
  }

  public ComponentInfo getNextComponent() {
    return m_stackContainer.getNext();
  }

  /**
   * Shows {@link ComponentInfo}, performs "refresh".
   */
  public void show(ComponentInfo component) {
    m_stackContainer.setActive(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    addBroadcastListener(new ObjectInfoChildGraphical() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        // show only current component on design canvas
        if (isManagedObject(object)) {
          ComponentInfo component = (ComponentInfo) object;
          if (isManagedObject(component) && component != getCurrentComponent()) {
            visible[0] = false;
          }
        }
      }
    });
    new LayoutAssistantSupport(this) {
      @Override
      protected AbstractAssistantPage createLayoutPage(Composite parent) {
        return new CardLayoutAssistantPage(parent, m_layout);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    ComponentInfo currentComponent = getCurrentComponent();
    if (currentComponent != null) {
      // prepare swing objects
      CardLayout layout = getLayout();
      Component component = currentComponent.getComponent();
      Container container = component.getParent();
      // show current component
      while (!component.isVisible()) {
        layout.next(container);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    for (ComponentInfo component : getContainer().getChildrenComponents()) {
      Association association = component.getAssociation();
      if (association instanceof InvocationAssociation) {
        MethodInvocation invocation = ((InvocationAssociation) association).getInvocation();
        if (invocation.arguments().size() == 1) {
          getEditor().addInvocationArgument(invocation, 1, getComponentConstraints());
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link ComponentInfo} to this {@link ContainerInfo}.
   */
  public void command_CREATE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    add(component, getComponentConstraints(), nextComponent);
  }

  /**
   * Moves {@link ComponentInfo} to this {@link ContainerInfo}.
   */
  public void command_MOVE(ComponentInfo component, ComponentInfo nextComponent) throws Exception {
    move(component, getComponentConstraints(), nextComponent);
  }

  /**
   * @return unique source of name for {@link CardLayout} constraints.
   */
  private static final String getComponentConstraints() {
    return '"' + getUniqueString() + '"';
  }

  /**
   * @return the unique {@link String} to use as name.
   */
  private static String getUniqueString() {
    return "name_" + System.nanoTime();
  }
}