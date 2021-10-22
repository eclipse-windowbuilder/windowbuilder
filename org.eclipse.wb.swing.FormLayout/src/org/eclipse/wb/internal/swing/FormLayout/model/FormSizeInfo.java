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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.ConstantSize.Unit;
import com.jgoodies.forms.layout.Size;
import com.jgoodies.forms.layout.Sizes;

/**
 * Description for {@link Size}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class FormSizeInfo {
  private final boolean m_horizontal;
  private Size m_componentSize;
  private FormSizeConstantInfo m_constantSize;
  private boolean m_hasLowerSize;
  private FormSizeConstantInfo m_lowerSize;
  private FormSizeConstantInfo m_upperSize;
  private boolean m_hasUpperSize;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormSizeInfo(Size size, boolean horizontal) throws Exception {
    m_horizontal = horizontal;
    Class<?> sizeClass = size.getClass();
    String sizeClassName = sizeClass.getName();
    if (sizeClassName.equals("com.jgoodies.forms.layout.ConstantSize")) {
      m_constantSize = createConstant(size);
    } else if (sizeClassName.equals("com.jgoodies.forms.layout.Sizes$ComponentSize")) {
      m_componentSize = size;
    } else {
      Assert.isTrue(sizeClassName.equals("com.jgoodies.forms.layout.BoundedSize"));
      // component
      {
        Size basic = (Size) ReflectionUtils.getFieldByName(sizeClass, "basis").get(size);
        Assert.isTrue(basic == Sizes.DEFAULT || basic == Sizes.MINIMUM || basic == Sizes.PREFERRED);
        m_componentSize = basic;
      }
      // lower bound
      {
        Size lower = (Size) ReflectionUtils.getFieldByName(sizeClass, "lowerBound").get(size);
        setLowerSize(createConstant(lower));
      }
      // upper bound
      {
        Size upper = (Size) ReflectionUtils.getFieldByName(sizeClass, "upperBound").get(size);
        setUpperSize(createConstant(upper));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link FormSizeConstantInfo} for given {@link ConstantSize} instance.
   */
  private static FormSizeConstantInfo createConstant(Size size) throws Exception {
    if (size != null) {
      Class<?> sizeClass = size.getClass();
      Assert.instanceOf(ConstantSize.class, size);
      //
      double value = ReflectionUtils.getFieldByName(sizeClass, "value").getDouble(size);
      Unit unit = (Unit) ReflectionUtils.getFieldByName(sizeClass, "unit").get(size);
      return new FormSizeConstantInfo(value, unit);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return getDisplayString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Specification
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the text to display this {@link FormSizeInfo} to user.
   */
  public String getDisplayString() {
    if (m_componentSize == null) {
      return m_constantSize.getSource(true, m_horizontal);
    } else {
      String size;
      if (m_componentSize == Sizes.DEFAULT) {
        size = "default";
      } else if (m_componentSize == Sizes.PREFERRED) {
        size = "preferred";
      } else {
        Assert.isTrue(m_componentSize == Sizes.MINIMUM);
        size = "minimum";
      }
      // add bounds
      if (m_hasLowerSize) {
        size = m_lowerSize.getSource(true, m_horizontal) + "<" + size;
      }
      if (m_hasUpperSize) {
        size = size + "<" + m_upperSize.getSource(true, m_horizontal);
      }
      // return final size
      return size;
    }
  }

  /**
   * @return <code>true</code> if this {@link FormSizeInfo} can be represented as encoded
   *         specification.
   */
  public boolean isString() {
    return !(m_hasLowerSize && m_hasUpperSize);
  }

  /**
   * @return the encoded specification or {@link Sizes} invocation code depending on result of
   *         {@link #isString()}.
   */
  public String getSource() {
    if (isString()) {
      if (m_componentSize == null) {
        return m_constantSize.getSource(true, m_horizontal);
      } else {
        String size;
        if (m_componentSize == Sizes.DEFAULT) {
          size = "default";
        } else if (m_componentSize == Sizes.PREFERRED) {
          size = "pref";
        } else {
          Assert.isTrue(m_componentSize == Sizes.MINIMUM);
          size = "min";
        }
        // add bounds
        if (m_hasLowerSize) {
          size = "max(" + m_lowerSize.getSource(true, m_horizontal) + ";" + size + ")";
        }
        if (m_hasUpperSize) {
          size = "min(" + m_upperSize.getSource(true, m_horizontal) + ";" + size + ")";
        }
        // return final size
        return size;
      }
    } else {
      Assert.isTrue(!isString());
      String source = "com.jgoodies.forms.layout.Sizes.bounded(";
      // add component size
      if (m_componentSize == Sizes.DEFAULT) {
        source += "com.jgoodies.forms.layout.Sizes.DEFAULT";
      } else if (m_componentSize == Sizes.PREFERRED) {
        source += "com.jgoodies.forms.layout.Sizes.PREFERRED";
      } else {
        Assert.isTrue(m_componentSize == Sizes.MINIMUM);
        source += "com.jgoodies.forms.layout.Sizes.MINIMUM";
      }
      // add lower bound
      source += ", ";
      source += m_lowerSize.getSource(false, m_horizontal);
      // add upper bound
      source += ", ";
      source += m_upperSize.getSource(false, m_horizontal);
      // finalize
      source += ")";
      return source;
    }
  }

  /**
   * @return the {@link Size} value.
   */
  public Size getSize() {
    if (m_componentSize == null) {
      return m_constantSize.getSize(m_horizontal);
    } else {
      Size size = m_componentSize;
      // add bounds
      if (m_hasLowerSize && m_hasUpperSize) {
        return Sizes.bounded(
            size,
            m_lowerSize.getSize(m_horizontal),
            m_upperSize.getSize(m_horizontal));
      } else if (m_hasLowerSize) {
        return Sizes.bounded(size, m_lowerSize.getSize(m_horizontal), null);
      } else if (m_hasUpperSize) {
        return Sizes.bounded(size, null, m_upperSize.getSize(m_horizontal));
      }
      // return pure component size
      return size;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the component size.
   */
  public Size getComponentSize() {
    return m_componentSize;
  }

  /**
   * Sets the component size.
   */
  public void setComponentSize(Size componentSize) {
    m_componentSize = componentSize;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constant
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the constant size.
   */
  public FormSizeConstantInfo getConstantSize() {
    return m_constantSize;
  }

  /**
   * Sets the constant size.
   */
  public void setConstantSize(FormSizeConstantInfo constantSize) {
    m_constantSize = constantSize;
    if (m_constantSize != null) {
      m_componentSize = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Lower bound
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if there is lower bound size.
   */
  public boolean hasLowerSize() {
    return m_hasLowerSize;
  }

  /**
   * Sets the flag of lower bound size.
   */
  public void setLowerSize(boolean hasLowerSize) {
    m_hasLowerSize = hasLowerSize;
  }

  /**
   * @return the lower bound size for bounded size.
   */
  public FormSizeConstantInfo getLowerSize() {
    return m_lowerSize;
  }

  /**
   * Sets the lower bound size for bounded size.
   */
  public void setLowerSize(FormSizeConstantInfo lowerSize) {
    m_lowerSize = lowerSize;
    m_hasLowerSize = m_lowerSize != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Upper bound
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if there is upper bound size.
   */
  public boolean hasUpperSize() {
    return m_hasUpperSize;
  }

  /**
   * Sets the flag of upper bound size.
   */
  public void setUpperSize(boolean hasUpperSize) {
    m_hasUpperSize = hasUpperSize;
  }

  /**
   * @return the upper bound size for bounded size.
   */
  public FormSizeConstantInfo getUpperSize() {
    return m_upperSize;
  }

  /**
   * Sets the upper bound size for bounded size.
   */
  public void setUpperSize(FormSizeConstantInfo upperSize) {
    m_upperSize = upperSize;
    m_hasUpperSize = m_upperSize != null;
  }
}
