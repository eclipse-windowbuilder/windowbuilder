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
package org.eclipse.wb.internal.rcp.databinding.emf.model;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration.IPropertiesFilter;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.CheckButtonEditor;
import org.eclipse.wb.internal.core.utils.binding.providers.BooleanPreferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.rcp.databinding.emf.Activator;
import org.eclipse.wb.internal.rcp.databinding.emf.Messages;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EObjectBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailValueEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableDetailListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableDetailValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.ListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.MapsEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer.EmfBeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer.EmfTreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer.EmfTreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties.EmfListPropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties.EmfValuePropertyDetailCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IGlobalObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory.Type;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ChooseClassAndTreePropertiesUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.IAutomaticWizardStub;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EMF implementation of {@link IGlobalObservableFactory}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class GlobalObservableFactory implements IGlobalObservableFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IGlobalObservableFactory: Observable
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObservableInfo createDetailObservable(SingleSelectionObservableInfo masterObservable,
      BindableInfo object,
      Type type) throws Exception {
    if (masterObservable.isViewer()) {
      // prepare input
      ObservableInfo inputObservable = null;
      BindableInfo property = object.resolvePropertyReference("setInput");
      for (AbstractBindingInfo binding : object.getBindings()) {
        if (binding.getTargetProperty() == property) {
          AbstractViewerInputBindingInfo viewerBinding = (AbstractViewerInputBindingInfo) binding;
          inputObservable = viewerBinding.getInputObservable();
          break;
        }
      }
      // create detail observable
      if (inputObservable instanceof ListEmfObservableInfo) {
        PropertiesSupport propertiesSupport = getPropertiesSupport(inputObservable);
        boolean version_2_5 = propertiesSupport.isEMFProperties();
        ObservableInfo observable = null;
        //
        if (type == Type.OnlyValue) {
          observable = new DetailValueEmfObservableInfo(masterObservable, propertiesSupport);
          //
          if (version_2_5) {
            observable.setCodeSupport(new EmfValuePropertyDetailCodeSupport());
          } else {
            observable.setCodeSupport(new EmfObservableDetailValueCodeSupport());
          }
        } else if (type == Type.OnlyList) {
          observable = new DetailListEmfObservableInfo(masterObservable, propertiesSupport);
          //
          if (version_2_5) {
            observable.setCodeSupport(new EmfListPropertyDetailCodeSupport());
          } else {
            observable.setCodeSupport(new EmfObservableDetailListCodeSupport());
          }
        }
        return observable;
      }
    }
    return null;
  }

  public MapsBeanObservableInfo createObserveMaps(ObservableInfo inputObservable,
      ObservableInfo domainObservable,
      Class<?> elementType,
      boolean[] useViewerSupport) throws Exception {
    if (inputObservable instanceof ListEmfObservableInfo
        || inputObservable instanceof DetailListEmfObservableInfo) {
      useViewerSupport[0] = false;
      MapsEmfObservableInfo observeMaps =
          new MapsEmfObservableInfo(domainObservable, getPropertiesSupport(inputObservable));
      observeMaps.setElementType(elementType);
      return observeMaps;
    }
    return null;
  }

  public BeansObservableFactoryInfo createTreeObservableFactory(ObservableInfo inputObservable,
      boolean asList) throws Exception {
    if (asList
        && (inputObservable instanceof ListEmfObservableInfo
            || inputObservable instanceof DetailListEmfObservableInfo)) {
      return EmfBeansObservableFactoryInfo.create(null, getPropertiesSupport(inputObservable));
    }
    return null;
  }

  public TreeBeanAdvisorInfo createTreeBeanAdvisor(ObservableInfo inputObservable)
      throws Exception {
    if (inputObservable instanceof ListEmfObservableInfo
        || inputObservable instanceof DetailListEmfObservableInfo) {
      return new EmfTreeBeanAdvisorInfo(getPropertiesSupport(inputObservable));
    }
    return null;
  }

  public TreeObservableLabelProviderInfo createTreeLabelProvider(ObservableInfo inputObservable,
      KnownElementsObservableInfo allElementsObservable) throws Exception {
    if (inputObservable instanceof ListEmfObservableInfo
        || inputObservable instanceof DetailListEmfObservableInfo) {
      return new EmfTreeObservableLabelProviderInfo(allElementsObservable,
          getPropertiesSupport(inputObservable));
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGlobalObservableFactory: UI
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configureChooseElementForViewerInput(ObservableInfo inputObservable,
      ChooseClassAndPropertiesConfiguration configuration) throws Exception {
    if (inputObservable instanceof ListEmfObservableInfo
        || inputObservable instanceof DetailListEmfObservableInfo) {
      final PropertiesSupport propertiesSupport = getPropertiesSupport(inputObservable);
      configuration.setBaseClassName("org.eclipse.emf.ecore.EObject");
      configuration.addPropertiesFilter(new IPropertiesFilter() {
        public List<PropertyAdapter> filterProperties(Class<?> choosenClass,
            List<PropertyAdapter> properties) throws Exception {
          properties = new ArrayList<>();
          for (PropertyInfo emfPropertyInfo : propertiesSupport.getProperties(choosenClass)) {
            properties.add(
                new ChooseClassAndTreePropertiesUiContentProvider.ObservePropertyAdapter(null,
                    new EPropertyBindableInfo(propertiesSupport,
                        null,
                        emfPropertyInfo.type,
                        emfPropertyInfo.name,
                        "\"" + emfPropertyInfo.name + "\"")));
          }
          return properties;
        }
      });
    }
  }

  public void configureChooseElementForTreeViewerInput(ObservableInfo inputObservable,
      ChooseClassConfiguration configuration) throws Exception {
    if (inputObservable instanceof ListEmfObservableInfo
        || inputObservable instanceof DetailListEmfObservableInfo) {
      configuration.setBaseClassName("org.eclipse.emf.ecore.EObject");
    }
  }

  public void filterElementPropertiesForTreeViewerInput(ObservableInfo inputObservable,
      Class<?> elementType,
      List<PropertyDescriptor> descriptors) throws Exception {
    if (inputObservable instanceof ListEmfObservableInfo
        || inputObservable instanceof DetailListEmfObservableInfo) {
      descriptors.clear();
      PropertiesSupport propertiesSupport = getPropertiesSupport(inputObservable);
      for (PropertyInfo emfProperty : propertiesSupport.getProperties(elementType)) {
        descriptors.add(createProperty(emfProperty.name, emfProperty.type));
      }
    }
  }

  private static Method m_setPropertyType;

  public static PropertyDescriptor createProperty(String name, Class<?> type) throws Exception {
    // create property
    PropertyDescriptor property = new PropertyDescriptor(name, (Method) null, (Method) null) {
      @Override
      public int hashCode() {
        return System.identityHashCode(this);
      }

      @Override
      public boolean equals(Object object) {
        return this == object;
      }
    };
    // prepare internal setter setPropertyType()
    if (m_setPropertyType == null) {
      m_setPropertyType =
          PropertyDescriptor.class.getDeclaredMethod("setPropertyType", new Class[]{Class.class});
      m_setPropertyType.setAccessible(true);
    }
    // configure property
    m_setPropertyType.invoke(property, new Object[]{type});
    return property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static PropertiesSupport getPropertiesSupport(ObservableInfo observable) {
    if (observable instanceof DetailEmfObservableInfo) {
      DetailEmfObservableInfo detailObservable = (DetailEmfObservableInfo) observable;
      return detailObservable.getPropertiesSupport();
    }
    EObjectBindableInfo eObject = (EObjectBindableInfo) observable.getBindableObject();
    return eObject.getPropertiesSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Automatic Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void automaticWizardConfigure(ChooseClassAndPropertiesConfiguration configuration) {
    configuration.setBaseClassName("org.eclipse.emf.ecore.EObject");
  }

  public List<PropertyAdapter> automaticWizardGetProperties(IJavaProject javaProject,
      ClassLoader classLoader,
      Class<?> eObjectClass) throws Exception {
    if (isEMFObject(classLoader, eObjectClass)) {
      List<PropertyAdapter> properties = new ArrayList<>();
      List<VariableDeclarationFragment> fragments = Collections.emptyList();
      PropertiesSupport propertiesSupport =
          new PropertiesSupport(javaProject, classLoader, fragments);
      //
      for (PropertyInfo emfPropertyInfo : propertiesSupport.getProperties(eObjectClass)) {
        properties.add(new PropertyAdapter(emfPropertyInfo.name, emfPropertyInfo.type));
      }
      return properties;
    }
    return null;
  }

  public IAutomaticWizardStub automaticWizardCreateStub(IJavaProject javaProject,
      ClassLoader classLoader,
      Class<?> eObjectClass) throws Exception {
    if (isEMFObject(classLoader, eObjectClass)) {
      return new AutomaticWizardStub(javaProject, classLoader, eObjectClass);
    }
    return null;
  }

  private static boolean isEMFObject(ClassLoader classLoader, Class<?> eObjectClass) {
    try {
      Class<?> EObjectClass = classLoader.loadClass("org.eclipse.emf.ecore.EObject");
      return eObjectClass.isInterface() && EObjectClass.isAssignableFrom(eObjectClass);
    } catch (Throwable e) {
      return false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Controller
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean moveBean(IObserveInfo observe,
      AstEditor controllerEditor,
      TypeDeclaration controllerRootNode) throws Exception {
    if (observe instanceof EObjectBindableInfo) {
      EObjectBindableInfo eObject = (EObjectBindableInfo) observe;
      VariableDeclarationFragment fragment = eObject.getFragment();
      FieldDeclaration fieldDeclaration = AstNodeUtils.getEnclosingFieldDeclaration(fragment);
      String modifier =
          Modifier.ModifierKeyword.fromFlagValue(fieldDeclaration.getModifiers()).toString();
      //
      controllerEditor.addFieldDeclaration(
          modifier
              + " "
              + eObject.getObjectType().getName()
              + " "
              + fragment.getName().getIdentifier()
              + ";",
          new BodyDeclarationTarget(controllerRootNode, true));
      //
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  public void confgureCodeGenerationPreferencePage(Composite parent, DataBindManager bindManager)
      throws Exception {
    Button generateCodeFor25Button = new Button(parent, SWT.CHECK);
    GridDataFactory.create(generateCodeFor25Button).fillH().grabH();
    generateCodeFor25Button.setText(Messages.GlobalObservableFactory_for25Button);
    //
    bindManager.bind(
        new CheckButtonEditor(generateCodeFor25Button),
        new BooleanPreferenceProvider(Activator.getStore(),
            IPreferenceConstants.GENERATE_CODE_FOR_VERSION_2_5));
  }
}