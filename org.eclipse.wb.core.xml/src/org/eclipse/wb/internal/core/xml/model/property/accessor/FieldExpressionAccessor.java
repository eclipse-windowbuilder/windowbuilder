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
package org.eclipse.wb.internal.core.xml.model.property.accessor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.AccessorUtils;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.lang.reflect.Field;

/**
 * {@link ExpressionAccessor} based on {@link Field}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public final class FieldExpressionAccessor extends ExpressionAccessor {
  private final Field m_field;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldExpressionAccessor(Field field) {
    super(field.getName());
    m_field = field;
    m_tooltipProvider = AccessorUtils.PropertyTooltipProvider_forField(field);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getDefaultValue(XmlObjectInfo object) throws Exception {
    if (XmlObjectUtils.isImplicit(object) && object.getParent() instanceof XmlObjectInfo) {
      XmlObjectInfo xmlParent = (XmlObjectInfo) object.getParent();
      DocumentElement objectElement = getExistingElement(object);
      if (objectElement == getExistingElement(xmlParent)) {
        return Property.UNKNOWN_VALUE;
      }
    }
    return object.getArbitraryValue(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void visit(XmlObjectInfo object, int state) throws Exception {
    super.visit(object, state);
    if (state == STATE_OBJECT_READY) {
      Object toolkitObject = object.getObject();
      Object value = ReflectionUtils.getFieldObject(toolkitObject, m_field.getName());
      object.putArbitraryValue(this, value);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PropertyTooltipProvider m_tooltipProvider;

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == PropertyTooltipProvider.class) {
      return adapter.cast(m_tooltipProvider);
    }
    // other
    return super.getAdapter(adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Field getField() {
    return m_field;
  }
}
