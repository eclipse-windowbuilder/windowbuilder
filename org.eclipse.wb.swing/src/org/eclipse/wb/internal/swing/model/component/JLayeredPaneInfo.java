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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLayeredPane;

/**
 * Model for {@link JLayeredPane}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public class JLayeredPaneInfo extends ContainerInfo {
  private final JLayeredPaneInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JLayeredPaneInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    contributeProperties();
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
      Collections.sort(children, new Comparator<ObjectInfo>() {
        public int compare(ObjectInfo o1, ObjectInfo o2) {
          return getLayer(o2) - getLayer(o1);
        }
      });
      return children;
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private int getLayer(ObjectInfo object) {
    if (object instanceof ComponentInfo) {
      return getLayer((ComponentInfo) object);
    }
    return -1;
  }

  private int getLayer(ComponentInfo component) {
    return ((JLayeredPane) getObject()).getLayer(component.getComponent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes properties to {@link ComponentInfo} children.
   */
  private void contributeProperties() {
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo instanceof ComponentInfo && javaInfo.getParent() == m_this) {
          ComponentInfo component = (ComponentInfo) javaInfo;
          properties.addAll(getContributedProperties(component));
        }
      }

      @SuppressWarnings("unchecked")
      private List<Property> getContributedProperties(ComponentInfo component) {
        List<Property> properties = (List<Property>) component.getArbitraryValue(m_this);
        if (properties == null) {
          properties = Lists.newArrayList();
          properties.add(new LayerProperty(component));
          component.putArbitraryValue(m_this, properties);
        }
        return properties;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayerProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * "Layer" property that is contributed to each {@link ComponentInfo} child of
   * {@link JLayeredPaneInfo}.
   */
  private final class LayerProperty extends JavaProperty {
    private final ComponentInfo m_component;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LayerProperty(ComponentInfo component) {
      super(component, "Layer", IntegerPropertyEditor.INSTANCE);
      m_component = component;
      setCategory(PropertyCategory.system(7));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isModified() throws Exception {
      return true;
    }

    @Override
    public Object getValue() throws Exception {
      return getLayer(m_component);
    }

    @Override
    public void setValue(final Object value) throws Exception {
      ExecutionUtils.run(m_component, new RunnableEx() {
        public void run() throws Exception {
          setValueEx(value);
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // setValue() implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Implementation for {@link #setValue(Object)}.
     */
    private void setValueEx(Object value) throws Exception {
      AstEditor editor = m_this.getEditor();
      // remove layer from association add(Component,Integer)
      if (m_component.getAssociation() instanceof InvocationChildAssociation) {
        InvocationChildAssociation association =
            (InvocationChildAssociation) m_component.getAssociation();
        MethodInvocation invocation = association.getInvocation();
        if (association.getDescription().getSignature().equals(
            "add(java.awt.Component,java.lang.Object)")
            && AstNodeUtils.getFullyQualifiedName(
                DomGenerics.arguments(invocation).get(1),
                isModified()).equals("java.lang.Integer")) {
          editor.removeInvocationArgument(invocation, 1);
        }
      }
      // try to update setLayer(Component,layer) or setLayer(Component,layer,position)
      boolean updated = false;
      updated |= updateExistingLayerInvocation(editor, value, "setLayer(java.awt.Component,int)");
      updated |=
          updateExistingLayerInvocation(editor, value, "setLayer(java.awt.Component,int,int)");
      // if not updated yet, add new setLayer(Component,layer)
      if (!updated && value != UNKNOWN_VALUE) {
        String source = TemplateUtils.format("{0}.setLayer({1}, {2})", m_this, m_component, value);
        Expression newInvocation = m_component.addExpressionStatement(source);
        addRelatedNodes(newInvocation);
      }
    }

    private boolean updateExistingLayerInvocation(AstEditor editor, Object value, String signature)
        throws Exception {
      for (MethodInvocation invocation : getMethodInvocations(signature)) {
        Expression componentExpression = DomGenerics.arguments(invocation).get(0);
        Expression layerExpression = DomGenerics.arguments(invocation).get(1);
        if (m_component.isRepresentedBy(componentExpression)) {
          if (value == UNKNOWN_VALUE) {
            editor.removeEnclosingStatement(invocation);
          } else {
            String layerSource = IntegerConverter.INSTANCE.toJavaSource(m_component, value);
            editor.replaceExpression(layerExpression, layerSource);
          }
          return true;
        }
      }
      return false;
    }
  }
}
