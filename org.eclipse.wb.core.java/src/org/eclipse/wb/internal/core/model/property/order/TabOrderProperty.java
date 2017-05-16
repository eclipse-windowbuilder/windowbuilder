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
package org.eclipse.wb.internal.core.model.property.order;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * Property for editing components tab order.
 *
 * @author lobas_av
 * @coverage core.model.property.order
 */
public abstract class TabOrderProperty extends Property {
  protected final JavaInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabOrderProperty(JavaInfo container) {
    super(TabOrderPropertyEditor.INSTANCE);
    m_container = container;
    //
    m_container.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        handleDeleteOrderElement(parent, child);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getTitle() {
    return "tab order";
  }

  @Override
  public final boolean isModified() throws Exception {
    return getOrderedArray() != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Object getValue() throws Exception {
    TabOrderInfo orderInfo = new TabOrderInfo();
    // prepare all components
    List<? extends AbstractComponentInfo> components = getTabPossibleChildren();
    orderInfo.getInfos().addAll(components);
    // calculate ordering
    ArrayInitializer arrayInitializer = getOrderedArray();
    if (arrayInitializer == null) {
      orderInfo.setDefault();
      // set default ordering
      for (AbstractComponentInfo component : components) {
        if (isDefaultOrdered(component)) {
          orderInfo.addOrderedInfo(component);
        }
      }
    } else {
      // set current ordering
      for (Expression expression : DomGenerics.expressions(arrayInitializer)) {
        JavaInfo component = m_container.getChildRepresentedBy(expression);
        if (component instanceof AbstractComponentInfo) {
          orderInfo.addOrderedInfo((AbstractComponentInfo) component);
        }
      }
      // reorder
      orderInfo.reorder();
    }
    return orderInfo;
  }

  public final String getDisplayText() throws Exception {
    // prepare ordering
    ArrayInitializer arrayInitializer = getOrderedArray();
    // check "no value"
    if (arrayInitializer == null) {
      return "";
    }
    // prepare current value
    StringBuffer textBuffer = new StringBuffer("[");
    for (Expression expression : DomGenerics.expressions(arrayInitializer)) {
      JavaInfo component = m_container.getChildRepresentedBy(expression);
      if (component instanceof AbstractComponentInfo) {
        // check element separator
        if (textBuffer.length() > 1) {
          textBuffer.append(", ");
        }
        // next value
        textBuffer.append(component.getVariableSupport().getTitle());
      }
    }
    textBuffer.append(']');
    return textBuffer.toString();
  }

  @Override
  public final void setValue(final Object value) throws Exception {
    ExecutionUtils.run(m_container, new RunnableEx() {
      public void run() throws Exception {
        setValueEx(value);
      }
    });
  }

  private void setValueEx(final Object value) throws Exception {
    if (value == UNKNOWN_VALUE) {
      // unknown value (maybe delete)
      removePropertyAssociation();
    } else {
      TabOrderInfo orderInfo = (TabOrderInfo) value;
      List<AbstractComponentInfo> orderedInfos = orderInfo.getOrderedInfos();
      // remove existing (may be it was at wrong place)
      removePropertyAssociation();
      // set new value
      if (!orderedInfos.isEmpty()) {
        StringBuffer source = new StringBuffer("{");
        for (AbstractComponentInfo component : orderedInfos) {
          if (source.length() != 1) {
            source.append(", ");
          }
          source.append(getComponentReference(component));
        }
        source.append("}");
        setOrderedArraySource(source.toString());
        // update related nodes
        ArrayInitializer arrayInitializer = getOrderedArray();
        Assert.isNotNull(arrayInitializer);
        List<Expression> arrayArguments = DomGenerics.expressions(arrayInitializer);
        int size = arrayArguments.size();
        Assert.equals(orderedInfos.size(), size);
        for (int i = 0; i < size; i++) {
          orderedInfos.get(i).addRelatedNode(arrayArguments.get(i));
        }
      }
    }
  }

  private static String getComponentReference(AbstractComponentInfo component) throws Exception {
    return TemplateUtils.getExpression(component);
  }

  /**
   * @return the list of all {@link JavaInfo} children supporting tab ordering.
   */
  protected abstract List<? extends AbstractComponentInfo> getTabPossibleChildren()
      throws Exception;

  /**
   * @return <code>true</code> if given info can have focus.
   */
  protected abstract boolean isDefaultOrdered(AbstractComponentInfo component) throws Exception;

  /**
   * @return {@link ArrayInitializer} represented tab order value.
   */
  protected abstract ArrayInitializer getOrderedArray() throws Exception;

  /**
   * Sets new tab order source value.
   */
  protected abstract void setOrderedArraySource(String source) throws Exception;

  /**
   * Remove property code association: for example method invocation (setTabList() for SWT) or
   * others.
   */
  protected abstract void removePropertyAssociation() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  private final void handleDeleteOrderElement(ObjectInfo parent, ObjectInfo child) throws Exception {
    if (hasOrderElement(parent, child)) {
      TabOrderInfo orderInfo = (TabOrderInfo) getValue();
      if (!orderInfo.isDefault() && orderInfo.getOrderedInfos().remove(child)) {
        setValue(orderInfo);
      }
    }
  }

  protected boolean hasOrderElement(ObjectInfo parent, ObjectInfo child) throws Exception {
    return parent == m_container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tooltip
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    // tooltip
    if (adapter == PropertyTooltipProvider.class && getPropertyTooltipText() != null) {
      return adapter.cast(new PropertyTooltipTextProvider() {
        @Override
        protected String getText(Property property) throws Exception {
          return getPropertyTooltipText();
        }
      });
    }
    // other
    return super.getAdapter(adapter);
  }

  /**
   * @return property tooltip text or <code>null</code> otherwise.
   */
  protected abstract String getPropertyTooltipText();
}