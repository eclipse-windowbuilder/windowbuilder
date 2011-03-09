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
package org.eclipse.wb.internal.swing.databinding.model.bindings;

import org.eclipse.wb.internal.core.utils.check.Assert;

/**
 * Model for {@link org.jdesktop.beansbinding.AutoBinding.UpdateStrategy}.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class UpdateStrategyInfo {
  public static final String[] VALUES = {"READ_ONCE", "READ", "READ_WRITE"};
  private Value m_strategyValue;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateStrategyInfo(String value) {
    setStrategyValue(value);
    Assert.isNotNull(m_strategyValue);
  }

  public UpdateStrategyInfo(Value strategyValue) {
    m_strategyValue = strategyValue;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Generate source code association with this object.
   */
  public String getStrategySourceCode() {
    switch (m_strategyValue) {
      case READ_ONCE :
        return "org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_ONCE";
      case READ :
        return "org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ";
      case READ_WRITE :
        return "org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE";
    }
    Assert.fail("Undefine value strategy value: " + m_strategyValue);
    return null;
  }

  /**
   * @return the current value for this strategy.
   */
  public String getStrategyValue() {
    switch (m_strategyValue) {
      case READ_ONCE :
        return "READ_ONCE";
      case READ :
        return "READ";
      case READ_WRITE :
        return "READ_WRITE";
    }
    Assert.fail("Undefine value strategy value: " + m_strategyValue);
    return null;
  }

  /**
   * Sets strategy value.
   */
  public void setStrategyValue(String value) {
    if (value.endsWith("READ_ONCE")) {
      m_strategyValue = Value.READ_ONCE;
    } else if (value.endsWith("READ")) {
      m_strategyValue = Value.READ;
    } else if (value.endsWith("READ_WRITE")) {
      m_strategyValue = Value.READ_WRITE;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Values
  //
  ////////////////////////////////////////////////////////////////////////////
  public static enum Value {
    READ_ONCE, READ, READ_WRITE
  }
}