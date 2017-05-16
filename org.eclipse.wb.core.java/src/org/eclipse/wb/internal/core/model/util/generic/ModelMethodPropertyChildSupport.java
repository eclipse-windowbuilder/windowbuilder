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
package org.eclipse.wb.internal.core.model.util.generic;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;

import org.apache.commons.lang.StringUtils;

/**
 * This helper allows to create top-level {@link Property} that as wrapper for some
 * {@link ObjectInfo} method.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ModelMethodPropertyChildSupport extends ModelMethodPropertyAbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Installation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link ObjectInfo} to create properties according parameters in description.
   */
  public static void install(ObjectInfo object, String prefix) {
    new ModelMethodPropertyChildSupport(object, prefix).install();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModelMethodPropertyChildSupport(ObjectInfo object, String prefix) {
    super(object, prefix);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ParameterProcessor createParameterProcessor() {
    return new ParameterProcessor() {
      private String m_childTypeName;

      @Override
      protected void processParameterPart(String part) throws Exception {
        super.processParameterPart(part);
        if (part.startsWith("child=")) {
          m_childTypeName = StringUtils.removeStart(part, "child=");
        }
      }

      @Override
      protected void processGetterSignature() {
        getterSignature += "(" + m_childTypeName + ")";
      }

      @Override
      protected void processSetterSignature() {
        setterSignature += "(" + m_childTypeName + ",java.lang.Object)";
      }

      @Override
      protected void configureProperty() {
        new PropertyProcessor() {
          @Override
          protected boolean isPropertyTarget(ObjectInfo target) {
            return target.getParent() == object
                && getter.getParameterTypes()[0].isAssignableFrom(target.getClass());
          }

          @Override
          protected Object getValue(ObjectInfo target) throws Exception {
            return getter.invoke(object, target);
          }

          @Override
          protected void setValue(ObjectInfo target, Object value) throws Exception {
            setter.invoke(object, target, value);
          }
        };
      }
    };
  }
}
