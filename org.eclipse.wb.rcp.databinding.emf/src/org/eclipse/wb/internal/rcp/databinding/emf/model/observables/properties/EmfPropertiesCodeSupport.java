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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.emf.Messages;
import org.eclipse.wb.internal.rcp.databinding.emf.model.EmfObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EObjectBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.text.MessageFormat;

/**
 * 
 * @author lobas_av
 * 
 */
public abstract class EmfPropertiesCodeSupport extends ObservableCodeSupport {
  private final String m_observeSignature0;
  private final String m_observeSignature1;
  private final String m_observeDetailSignature;
  protected String m_parserPropertyReference;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EmfPropertiesCodeSupport(String parseClass) {
    m_observeSignature0 = parseClass + ".observe(java.lang.Object)";
    m_observeSignature1 =
        parseClass + ".observe(org.eclipse.core.databinding.observable.Realm,java.lang.Object)";
    m_observeDetailSignature =
        parseClass
            + ".observeDetail(org.eclipse.core.databinding.observable.value.IObservableValue)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setParserPropertyReference(String propertyReference) {
    m_parserPropertyReference = propertyReference;
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
    //
    EmfObserveTypeContainer container =
        (EmfObserveTypeContainer) DatabindingsProvider.cast(provider).getContainer(
            EmfObserveTypeContainer.TYPE);
    // extract bean expression
    Expression beanExpression;
    if (m_observeSignature0.equals(signature)) {
      beanExpression = arguments[0];
    } else if (m_observeSignature1.equals(signature)) {
      beanExpression = arguments[1];
    } else if (m_observeDetailSignature.equals(signature)) {
      ObservableInfo masterObservable =
          EmfObserveTypeContainer.getMasterObservable(editor, resolver, arguments[0]);
      Assert.isNotNull(masterObservable);
      //
      return createDetailObservable(masterObservable, container.getPropertiesSupport());
    } else {
      return null;
    }
    //
    EObjectBindableInfo eObject = container.getEObject(beanExpression);
    if (eObject == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(Messages.EmfPropertiesCodeSupport_argumentNotFound, beanExpression),
          new Throwable());
      return null;
    }
    // create observable
    return createObservable(editor, eObject);
  }

  protected ObservableInfo createObservable(AstEditor editor, EObjectBindableInfo eObject)
      throws Exception {
    EPropertyBindableInfo eProperty = eObject.resolvePropertyReference(m_parserPropertyReference);
    if (eProperty == null) {
      AbstractParser.addError(editor, MessageFormat.format(
          Messages.EmfPropertiesCodeSupport_beanPropertyNotFound,
          m_parserPropertyReference,
          eObject.getReference()), new Throwable());
      eProperty = new EPropertyBindableInfo(null, null, null, "", "");
    }
    //
    ObservableInfo observable = createObservable(eObject, eProperty);
    observable.setCodeSupport(this);
    return observable;
  }

  /**
   * @return {@link ObservableInfo} model for given object and property.
   */
  protected abstract ObservableInfo createObservable(EObjectBindableInfo eObject,
      EPropertyBindableInfo eProperty);

  /**
   * @return {@link ObservableInfo} detail model for given master {@link ObservableInfo}.
   */
  protected abstract ObservableInfo createDetailObservable(ObservableInfo masterObservable,
      PropertiesSupport propertiesSupport) throws Exception;
}