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
package org.eclipse.wb.internal.core.model.property;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertyGetValue;
import org.eclipse.wb.core.model.broadcast.GenericPropertyGetValueEx;
import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IAccessibleExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.Expression;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Concrete implementation of {@link GenericProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public final class GenericPropertyImpl extends GenericProperty {
  private final GenericPropertyImpl m_this = this;
  private final ExpressionAccessor[] m_accessors;
  private final Object m_defaultValue;
  private final ExpressionConverter m_converter;
  private GenericPropertyDescription m_description;
  private Class<?> m_type;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates {@link GenericPropertyImpl} with all explicit elements.
   */
  public GenericPropertyImpl(JavaInfo javaInfo,
      String title,
      ExpressionAccessor[] accessors,
      Object defaultValue,
      ExpressionConverter converter,
      PropertyEditor propertyEditor) {
    super(javaInfo, title, propertyEditor);
    m_accessors = accessors;
    m_defaultValue = defaultValue;
    m_converter = converter;
  }

  /**
   * Creates identical copy of given {@link GenericPropertyImpl}.
   */
  public GenericPropertyImpl(GenericPropertyImpl property) {
    this(property, property.getTitle());
  }

  /**
   * Creates copy of {@link GenericPropertyImpl}, with new title.
   */
  public GenericPropertyImpl(GenericPropertyImpl property, String title) {
    this(property.m_javaInfo,
        title,
        property.m_accessors,
        property.m_defaultValue,
        property.m_converter,
        property.getEditor());
    setCategory(property.getCategory());
    setDescription(property.getDescription());
    setType(property.getType());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return getExpressionInfo() != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  public Class<?> getType() {
    return m_type;
  }

  /**
   * Sets the type of this property. Use this if it has no {@link GenericPropertyDescription}.
   */
  public void setType(Class<?> type) {
    m_type = type;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the optional {@link GenericPropertyDescription} for "standard bean property".
   */
  public GenericPropertyDescription getDescription() {
    return m_description;
  }

  /**
   * Sets the optional {@link GenericPropertyDescription}.
   */
  public void setDescription(GenericPropertyDescription description) {
    m_description = description;
    if (m_description != null) {
      m_type = m_description.getType();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue() throws Exception {
    // allow broadcast listeners to set value
    {
      Object[] valueArray = new Object[]{UNKNOWN_VALUE};
      m_javaInfo.getBroadcast(GenericPropertyGetValue.class).invoke(this, valueArray);
      if (valueArray[0] != UNKNOWN_VALUE) {
        return valueArray[0];
      }
    }
    // get value from Expression
    Expression expression = getExpression();
    if (expression != null) {
      Object value = JavaInfoEvaluationHelper.getValue(expression);
      // allow broadcast listeners to change value
      {
        Object[] valueArray = new Object[]{value};
        m_javaInfo.getBroadcast(GenericPropertyGetValueEx.class).invoke(
            this,
            expression,
            valueArray);
        value = valueArray[0];
      }
      // final result
      return value;
    }
    // return default value
    return getDefaultValue();
  }

  @Override
  public Object getDefaultValue() throws Exception {
    // if has forced default value from property, use it
    if (m_defaultValue != UNKNOWN_VALUE) {
      return m_defaultValue;
    }
    // check for default value from accessor
    Object accessor_defaultValue = m_accessors[0].getDefaultValue(m_javaInfo);
    if (accessor_defaultValue != UNKNOWN_VALUE) {
      return accessor_defaultValue;
    }
    // no value
    return UNKNOWN_VALUE;
  }

  @Override
  public void setValue(final Object value) throws Exception {
    if (process_NLSSupport_specialFunctionality(value)) {
      return;
    }
    // "normal" property
    ExecutionUtils.run(m_javaInfo, new RunnableEx() {
      public void run() throws Exception {
        setValueEx(value);
      }
    });
  }

  private boolean process_NLSSupport_specialFunctionality(final Object value) throws Exception {
    if (value == UNKNOWN_VALUE) {
      return false;
    }
    if (!NlsSupport.isStringProperty(this)) {
      return false;
    }
    final NlsSupport support = NlsSupport.get(m_javaInfo);
    // check if key name is used
    if (value instanceof String) {
      String stringValue = (String) value;
      String keyPrefix =
          m_javaInfo.getDescription().getToolkit().getPreferences().getString(
              IPreferenceConstants.P_NLS_KEY_AS_VALUE_PREFIX);
      if (!StringUtils.isEmpty(keyPrefix) && stringValue.startsWith(keyPrefix)) {
        final String key = stringValue.substring(keyPrefix.length());
        final AbstractSource source = support.getKeySource(key);
        if (source != null) {
          ExecutionUtils.run(m_javaInfo, new RunnableEx() {
            public void run() throws Exception {
              source.useKey(m_this, key);
            }
          });
          return true;
        }
      }
    }
    // check for externalized String property
    {
      final Expression expression = getExpression();
      if (expression != null) {
        if (support.isExternalized(expression)) {
          ExecutionUtils.run(m_javaInfo, new RunnableEx() {
            public void run() throws Exception {
              String string = value == UNKNOWN_VALUE ? null : (String) value;
              support.setValue(expression, string);
            }
          });
          return true;
        }
      }
    }
    // no NLS
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression() throws Exception {
    ExpressionInfo expressionInfo = getExpressionInfo();
    return expressionInfo != null ? expressionInfo.m_expression : null;
  }

  @Override
  public void setExpression(String source, Object value) throws Exception {
    // validate expression
    {
      String[] sourceToValidate = new String[]{source};
      Object[] valueToValidate = new Object[]{value};
      boolean[] validationStatus = new boolean[]{true};
      m_javaInfo.getBroadcastJava().setPropertyExpression(
          this,
          sourceToValidate,
          valueToValidate,
          validationStatus);
      if (!validationStatus[0]) {
        return;
      }
      source = sourceToValidate[0];
      value = valueToValidate[0];
    }
    // replace patterns
    if (source != null && source.contains("%this%")) {
      source = StringUtils.replace(source, "%this%", TemplateUtils.getExpression(m_javaInfo));
    }
    // do set expression
    ExpressionInfo expressionInfo = getExpressionInfo();
    if (expressionInfo != null) {
      ExpressionAccessor accessor = expressionInfo.m_accessor;
      setExpressionUsingAccessor(accessor, source, value);
    } else {
      for (ExpressionAccessor accessor : getAccessors()) {
        boolean success = setExpressionUsingAccessor(accessor, source, value);
        if (success) {
          break;
        }
      }
    }
    // set value
    rememberValueIntoExpression(value);
    // done
    m_javaInfo.getBroadcastJava().propertyValueWasSet(this);
  }

  /**
   * We should remember value into current {@link Expression}, because sometimes we want to use
   * {@link #getValue()} directly after {@link #setExpression(String, Object)}, without
   * {@link JavaInfo#refresh()}.
   */
  private void rememberValueIntoExpression(Object value) throws Exception {
    Expression expression = getExpression();
    if (expression != null) {
      if (!JavaInfoEvaluationHelper.hasValue(expression)) {
        JavaInfoEvaluationHelper.setValue(expression, value);
      }
    }
  }

  /**
   * Sets the source/value using given {@link ExpressionAccessor}.
   *
   * @return <code>true</code> if expression was successfully set.
   */
  private boolean setExpressionUsingAccessor(ExpressionAccessor accessor,
      String source,
      Object value) throws Exception {
    // check for default value
    if (value != UNKNOWN_VALUE) {
      if (ObjectUtils.equals(getDefaultValue(accessor), value)) {
        source = null;
      }
    }
    // do set
    return accessor.setExpression(m_javaInfo, source);
  }

  /**
   * @return the forced default value or default from {@link ExpressionAccessor} that will be used
   *         to set new value.
   */
  private Object getDefaultValue(ExpressionAccessor accessor) throws Exception {
    if (m_defaultValue != UNKNOWN_VALUE) {
      return m_defaultValue;
    }
    return accessor.getDefaultValue(m_javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Property getComposite(Property[] properties) {
    return GenericPropertyComposite.create(properties);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    // get PropertyTooltipProvider from first accessor
    if (adapter == PropertyTooltipProvider.class) {
      return m_accessors[0].getAdapter(adapter);
    }
    return super.getAdapter(adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the {@link ExpressionAccessor}'s that should be used now. In the past we've used
   * accessors prepared directly in {@link JavaInfo} during {@link GenericPropertyImpl} creation.
   * However we can not do this because {@link CreationSupport} can be changed during life of
   * {@link JavaInfo}, so it can contribute different {@link ExpressionAccessor}'s.
   *
   * @return the current {@link ExpressionAccessor}'s.
   */
  public List<ExpressionAccessor> getAccessors() throws Exception {
    List<ExpressionAccessor> accessors = Lists.newArrayList();
    // add "static" accessors
    Collections.addAll(accessors, m_accessors);
    // add accessors from CreationSupport
    if (m_description != null) {
      m_javaInfo.getCreationSupport().addAccessors(m_description, accessors);
    }
    // remove non-accessible accessors
    for (Iterator<ExpressionAccessor> I = accessors.iterator(); I.hasNext();) {
      ExpressionAccessor accessor = I.next();
      IAccessibleExpressionAccessor accessibleAccessor =
          accessor.getAdapter(IAccessibleExpressionAccessor.class);
      if (accessibleAccessor != null && !accessibleAccessor.isAccessible(m_javaInfo)) {
        I.remove();
      }
    }
    // OK, final accessors
    return accessors;
  }

  /**
   * @return the {@link ExpressionInfo} for existing property {@link Expression} or
   *         <code>null</code> if not {@link Expression} was found.
   */
  private ExpressionInfo getExpressionInfo() throws Exception {
    for (ExpressionAccessor accessor : getAccessors()) {
      Expression expression = accessor.getExpression(m_javaInfo);
      if (expression != null) {
        return new ExpressionInfo(accessor, expression);
      }
    }
    return null;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Holder of information about {@link Expression} and {@link ExpressionAccessor} that provided it.
   *
   * @author scheglov_ke
   */
  private static final class ExpressionInfo {
    ExpressionAccessor m_accessor;
    Expression m_expression;

    public ExpressionInfo(ExpressionAccessor accessor, Expression expression) {
      m_accessor = accessor;
      m_expression = expression;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the Java source that that has same value as current value of this
   *         {@link GenericPropertyImpl}, or <code>null</code> if no such source can be provided.
   */
  public String getClipboardSource() throws Exception {
    if (m_description != null && m_description.hasTrueTag("noClipboard")) {
      return null;
    }
    if (m_editor instanceof IClipboardSourceProvider) {
      return ((IClipboardSourceProvider) m_editor).getClipboardSource(this);
    }
    if (m_converter != null) {
      return m_converter.toJavaSource(m_javaInfo, getValue());
    }
    // no clipboard source
    return null;
  }

  private void setValueEx(final Object value) throws Exception {
    // validate value
    Object validatedValue;
    {
      Object[] valueToValidate = new Object[]{value};
      boolean[] validationStatus = new boolean[]{true};
      m_javaInfo.getBroadcast(GenericPropertySetValue.class).invoke(
          m_this,
          valueToValidate,
          validationStatus);
      if (!validationStatus[0]) {
        return;
      }
      validatedValue = valueToValidate[0];
    }
    // prepare source
    String source;
    if (validatedValue == UNKNOWN_VALUE) {
      source = null;
    } else if (m_editor instanceof IValueSourcePropertyEditor) {
      IValueSourcePropertyEditor sourceEditor = (IValueSourcePropertyEditor) m_editor;
      source = sourceEditor.getValueSource(validatedValue);
    } else {
      Assert.isNotNull(m_converter);
      source = m_converter.toJavaSource(m_javaInfo, validatedValue);
    }
    // set source
    setExpression(source, validatedValue);
    // auto externalize
    if (value != UNKNOWN_VALUE) {
      if (!JavaInfoUtils.getState(m_javaInfo).isLiveComponent()) {
        NlsSupport.autoExternalize(m_this);
      }
    }
  }
}
