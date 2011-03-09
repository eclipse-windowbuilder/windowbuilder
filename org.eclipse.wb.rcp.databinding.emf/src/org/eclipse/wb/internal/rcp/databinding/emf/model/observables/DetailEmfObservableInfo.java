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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EObjectBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.ClassInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ChooseClassAndTreePropertiesUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ObservableDetailUiContentProvider;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Abstract model for observable objects <code>EMFObservables.observeDetailXXX(...)</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public abstract class DetailEmfObservableInfo extends DetailBeanObservableInfo {
  private final PropertiesSupport m_propertiesSupport;
  private String m_detailEMFPropertyName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DetailEmfObservableInfo(ObservableInfo masterObservable,
      PropertiesSupport propertiesSupport) {
    super(masterObservable, null, null, null);
    m_propertiesSupport = propertiesSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setDetailPropertyReference(Class<?> detailBeanClass, String detailPropertyReference)
      throws Exception {
    m_detailPropertyReference = detailPropertyReference;
    //
    if (detailBeanClass == null) {
      if (detailPropertyReference.startsWith("org.eclipse.emf.databinding.FeaturePath.fromList(")) {
        detailPropertyReference = StringUtils.substringBetween(detailPropertyReference, "(", ")");
        String[] references = StringUtils.split(detailPropertyReference, ", ");
        detailPropertyReference = references[references.length - 1];
      }
    } else {
      m_detailBeanClass = detailBeanClass;
    }
    // prepare EMF class info
    Object[] result = m_propertiesSupport.getClassInfoForProperty(detailPropertyReference);
    Assert.isNotNull(result);
    // prepare detail class
    if (detailBeanClass == null) {
      ClassInfo classInfo = (ClassInfo) result[0];
      m_detailBeanClass = classInfo.thisClass;
    }
    // prepare EMF property
    PropertyInfo emfPropertyInfo = (PropertyInfo) result[1];
    Assert.isNotNull(emfPropertyInfo.type);
    m_detailPropertyType = emfPropertyInfo.type;
    //
    m_detailEMFPropertyName = "\"" + emfPropertyInfo.name + "\"";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesSupport getPropertiesSupport() {
    return m_propertiesSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    String presentationProperty = StringUtils.defaultIfEmpty(m_detailEMFPropertyName, "?????");
    String presentationPropertyType =
        m_detailPropertyType == null ? "?????" : ClassUtils.getShortClassName(m_detailPropertyType);
    return m_masterObservable.getPresentationText()
        + ".detail"
        + getPresentationPrefix()
        + "("
        + presentationProperty
        + ", "
        + presentationPropertyType
        + ".class)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      BindingUiContentProviderContext context,
      DatabindingsProvider provider) throws Exception {
    m_masterObservable.createContentProviders(providers, context, provider);
    //
    ChooseClassAndPropertiesConfiguration configuration =
        new ChooseClassAndPropertiesConfiguration(getConfiguration());
    configuration.setBaseClassName("org.eclipse.emf.ecore.EObject");
    //
    providers.add(new ObservableDetailUiContentProvider(configuration, this, provider) {
      @Override
      protected List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
        List<PropertyAdapter> properties = Lists.newArrayList();
        for (PropertyInfo emfPropertyInfo : m_propertiesSupport.getProperties(choosenClass)) {
          properties.add(new ChooseClassAndTreePropertiesUiContentProvider.ObservePropertyAdapter(null,
              new EPropertyBindableInfo(m_propertiesSupport,
                  null,
                  emfPropertyInfo.type,
                  emfPropertyInfo.name,
                  emfPropertyInfo.reference)));
        }
        return properties;
      }

      @Override
      protected void setClassNameAndProperties(Class<?> beanClass,
          String beanClassName,
          List<String> properties) throws Exception {
        if (beanClassName == null) {
          setClassName(CoreUtils.getClassName(beanClass));
        } else {
          setClassName(beanClassName);
        }
        //
        EObjectBindableInfo eObject =
            new EObjectBindableInfo(beanClass, null, m_propertiesSupport, null);
        //
        Object[] adapters = new Object[properties.size()];
        for (int i = 0; i < adapters.length; i++) {
          adapters[i] =
              convertPropertyToAdapter(eObject.resolvePropertyReference(properties.get(i)));
        }
        //
        setCheckedAndExpand(adapters);
        calculatePropertiesFinish();
      }

      @Override
      protected ObservePropertyAdapter convertPropertyToAdapter(IObserveInfo observe)
          throws Exception {
        if (observe instanceof EPropertyBindableInfo) {
          EPropertyBindableInfo property = (EPropertyBindableInfo) observe;
          ObservePropertyAdapter adapter =
              new ObservePropertyAdapter(convertPropertyToAdapter(property.getParent()), property);
          adapter.addToParent();
          return adapter;
        }
        return null;
      }
    });
  }
}