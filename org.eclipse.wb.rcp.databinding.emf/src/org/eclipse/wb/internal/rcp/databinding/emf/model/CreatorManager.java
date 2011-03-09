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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.rcp.databinding.model.beans.ModelCreator;

import org.eclipse.jdt.core.dom.Expression;

import java.util.Map;

/**
 * Creators for parse all methods <code>EMFObservables.observeXXX(...)</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
final class CreatorManager {
  public static final Map<String, LocalModelCreator> METHOD_CREATORS = Maps.newHashMap();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ILocalModelCreator VALUE_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createValueProperty(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator EDIT_VALUE_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createValuePropertyEdit(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator VALUE_PATH_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createValuePathProperty(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator EDIT_PATH_VALUE_PROPERTY_CREATOR =
      new ILocalModelCreator() {
        public AstObjectInfo create(EmfObserveTypeContainer container,
            AstEditor editor,
            Expression[] arguments,
            IModelResolver resolver,
            ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
          return container.createValuePathPropertyEdit(editor, arguments, modelCreator.startIndex0);
        }
      };
  private static final ILocalModelCreator VALUE_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createValue(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator EDIT_VALUE_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createValueEdit(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createList(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator EDIT_LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createListEdit(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator LIST_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createListProperty(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator EDIT_LIST_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createListPropertyEdit(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator LIST_PATH_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createListPathProperty(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator EDIT_LIST_PATH_PROPERTY_CREATOR =
      new ILocalModelCreator() {
        public AstObjectInfo create(EmfObserveTypeContainer container,
            AstEditor editor,
            Expression[] arguments,
            IModelResolver resolver,
            ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
          return container.createListPathPropertyEdit(editor, arguments, modelCreator.startIndex0);
        }
      };
  private static final ILocalModelCreator DETAIL_VALUE_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createDetailValue(editor, arguments, modelCreator.startIndex0, resolver);
    }
  };
  private static final ILocalModelCreator EDIT_DETAIL_VALUE_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createDetailValueEdit(editor, arguments, modelCreator.startIndex0, resolver);
    }
  };
  private static final ILocalModelCreator DETAIL_LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createDetailList(editor, arguments, modelCreator.startIndex0, resolver);
    }
  };
  private static final ILocalModelCreator EDIT_DETAIL_LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createDetailListEdit(editor, arguments, modelCreator.startIndex0, resolver);
    }
  };
  private static final ILocalModelCreator MAPS_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createMaps(editor, arguments, modelCreator.startIndex0, resolver);
    }
  };
  private static final ILocalModelCreator EDIT_MAPS_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(EmfObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<EmfObserveTypeContainer> modelCreator) throws Exception {
      return container.createMapsEdit(editor, arguments, modelCreator.startIndex0, resolver);
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creators
  //
  ////////////////////////////////////////////////////////////////////////////
  static {
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFObservables.observeValue(org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFObservables.observeValue(org.eclipse.core.databinding.observable.Realm,org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditObservables.observeValue(org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, EDIT_VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditObservables.observeValue(org.eclipse.core.databinding.observable.Realm,org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, EDIT_VALUE_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFProperties.value(org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFProperties.value(org.eclipse.emf.databinding.FeaturePath)",
        new LocalModelCreator(0, VALUE_PATH_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditProperties.value(org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, EDIT_VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditProperties.value(org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.databinding.FeaturePath)",
        new LocalModelCreator(0, EDIT_PATH_VALUE_PROPERTY_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFObservables.observeList(org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFObservables.observeList(org.eclipse.core.databinding.observable.Realm,org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditObservables.observeList(org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, EDIT_LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditObservables.observeList(org.eclipse.core.databinding.observable.Realm,org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.ecore.EObject,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, EDIT_LIST_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFProperties.list(org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFProperties.list(org.eclipse.emf.databinding.FeaturePath)",
        new LocalModelCreator(0, LIST_PATH_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditProperties.list(org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(0, EDIT_LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditProperties.list(org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.databinding.FeaturePath)",
        new LocalModelCreator(0, EDIT_LIST_PATH_PROPERTY_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFObservables.observeDetailValue(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, DETAIL_VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditObservables.observeDetailValue(org.eclipse.core.databinding.observable.Realm,org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.core.databinding.observable.value.IObservableValue,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, EDIT_DETAIL_VALUE_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFObservables.observeDetailList(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, DETAIL_LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditObservables.observeDetailList(org.eclipse.core.databinding.observable.Realm,org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.core.databinding.observable.value.IObservableValue,org.eclipse.emf.ecore.EStructuralFeature)",
        new LocalModelCreator(1, EDIT_DETAIL_LIST_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.EMFObservables.observeMaps(org.eclipse.core.databinding.observable.set.IObservableSet,org.eclipse.emf.ecore.EStructuralFeature[])",
        new LocalModelCreator(0, MAPS_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.emf.databinding.edit.EMFEditObservables.observeMaps(org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.core.databinding.observable.set.IObservableSet,org.eclipse.emf.ecore.EStructuralFeature[])",
        new LocalModelCreator(0, EDIT_MAPS_CREATOR));
  }
}