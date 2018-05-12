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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.PropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringUtils;

import java.beans.PropertyDescriptor;
import java.text.MessageFormat;
import java.util.List;

/**
 * Abstract model for org.eclipse.core.databinding.property.IProperty based objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class BeanPropertiesCodeSupport extends ObservableCodeSupport {
  private final String m_observeSignature0;
  private final String m_observeSignature1;
  private final String m_observeDetailSignature;
  protected Class<?> m_parserBeanType;
  protected String m_parserPropertyReference;
  protected Class<?> m_parserPropertyType;
  protected boolean m_parserIsPojo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanPropertiesCodeSupport(String parseClass) {
    m_observeSignature0 = parseClass + ".observe(java.lang.Object)";
    m_observeSignature1 =
        parseClass + ".observe(org.eclipse.core.databinding.observable.Realm,java.lang.Object)";
    m_observeDetailSignature = parseClass
        + ".observeDetail(org.eclipse.core.databinding.observable.value.IObservableValue)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setBeanType(Class<?> beanType) throws Exception {
    m_parserBeanType = beanType;
    if (m_parserBeanType != null
        && m_parserPropertyReference != null
        && m_parserPropertyType == null) {
      String propertyName = StringUtils.remove(m_parserPropertyReference, "\"");
      for (PropertyDescriptor descriptor : BeanSupport.getPropertyDescriptors(m_parserBeanType)) {
        if (propertyName.equals(descriptor.getName())) {
          m_parserPropertyType = descriptor.getPropertyType();
          break;
        }
      }
    }
  }

  public final String getParserPropertyReference() {
    return m_parserPropertyReference;
  }

  public final void setParserPropertyReference(String propertyReference) {
    m_parserPropertyReference = propertyReference;
  }

  public final void setParserPropertyType(Class<?> propertyType) {
    m_parserPropertyType = propertyType;
  }

  public final Class<?> getParserPropertyType() {
    return m_parserPropertyType;
  }

  public final boolean parserIsPojo() {
    return m_parserIsPojo;
  }

  public final void setPojoBindable(boolean isPojoBindable) {
    m_parserIsPojo = isPojoBindable;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    BeansObserveTypeContainer container =
        (BeansObserveTypeContainer) DatabindingsProvider.cast(provider).getContainer(
            ObserveType.BEANS);
    // extract bean expression
    Expression beanExpression;
    if (m_observeSignature0.equals(signature)) {
      beanExpression = arguments[0];
    } else if (m_observeSignature1.equals(signature)) {
      beanExpression = arguments[1];
    } else if (m_observeDetailSignature.equals(signature)) {
      ObservableInfo masterObservable =
          BeansObserveTypeContainer.getMasterObservable(editor, resolver, arguments[0]);
      if (masterObservable == null) {
        // try find in bean observes...
        BeanBindableInfo bindableInfo = container.getBindableObject(arguments[0]);
        PropertyBindableInfo detailProperty = null;
        for (PropertyBindableInfo property : bindableInfo.getProperties()) {
          if (DirectObservableInfo.DETAIL_PROPERTY_NAME.equals(
              property.getPresentation().getText())) {
            detailProperty = property;
            break;
          }
        }
        Assert.isNotNull(detailProperty);
        masterObservable = new DirectObservableInfo(bindableInfo, detailProperty);
      }
      Assert.isNotNull(masterObservable);
      //
      return createDetailObservable(masterObservable);
    } else {
      return null;
    }
    // prepare bean model
    BeanBindableInfo bindableObject = container.getBindableObject(beanExpression);
    if (bindableObject == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(Messages.BeanPropertiesCodeSupport_argumentNotFound, beanExpression),
          new Throwable());
      return null;
    }
    // create observable
    return createObservable(editor, bindableObject);
  }

  /**
   * @return {@link ObservableInfo} detail model for given master {@link ObservableInfo}.
   */
  protected ObservableInfo createDetailObservable(ObservableInfo masterObservable)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  protected ObservableInfo createObservable(AstEditor editor, BeanBindableInfo bindableObject)
      throws Exception {
    // prepare property
    BeanPropertyBindableInfo bindableProperty =
        (BeanPropertyBindableInfo) bindableObject.resolvePropertyReference(
            m_parserPropertyReference);
    if (bindableProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.BeanPropertiesCodeSupport_propertyNotFound,
              m_parserPropertyReference,
              bindableObject.getReference()),
          new Throwable());
      bindableProperty = new BeanPropertyBindableInfo(bindableObject.getBeanSupport(),
          null,
          m_parserPropertyReference,
          null,
          m_parserPropertyReference);
    }
    //
    ObservableInfo observable = createObservable(bindableObject, bindableProperty);
    observable.setCodeSupport(this);
    return observable;
  }

  /**
   * @return {@link ObservableInfo} model for given object and property.
   */
  protected abstract ObservableInfo createObservable(BeanBindableInfo bindableObject,
      BeanPropertyBindableInfo bindableProperty) throws Exception;

  /**
   * @return the source code for given {@link DetailBeanObservableInfo} detail observable.
   */
  public String getDetailSourceCode(DetailBeanObservableInfo detailObservable,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    throw new UnsupportedOperationException();
  }
}