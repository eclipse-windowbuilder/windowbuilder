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
package org.eclipse.wb.internal.rcp.databinding.model.beans;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.Expression;

import java.util.Map;

/**
 * Creators for parse all methods <code>BeansObservables.observeXXX(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
final class CreatorManager {
  public static final Map<String, LocalModelCreator> CONSTRUCTOR_CREATORS = Maps.newHashMap();
  public static final Map<String, LocalModelCreator> METHOD_CREATORS = Maps.newHashMap();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ILocalModelCreator VALUE_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createValue(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator VALUE_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createValueProperty(
          editor,
          arguments,
          modelCreator.startIndex0,
          modelCreator.startIndex1,
          modelCreator.startIndex2,
          modelCreator.isPojo);
    }
  };
  private static final ILocalModelCreator LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createList(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator LIST_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createListProperty(
          editor,
          arguments,
          modelCreator.startIndex0,
          modelCreator.startIndex1,
          modelCreator.startIndex2,
          modelCreator.isPojo);
    }
  };
  private static final ILocalModelCreator SET_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createSet(editor, arguments, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator SET_PROPERTY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createSetProperty(
          editor,
          arguments,
          modelCreator.startIndex0,
          modelCreator.startIndex1,
          modelCreator.startIndex2,
          modelCreator.isPojo);
    }
  };
  private static final ILocalModelCreator DETAIL_VALUE_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createDetailValue(
          editor,
          arguments,
          resolver,
          modelCreator.startIndex0,
          modelCreator.startIndex1,
          modelCreator.startIndex2,
          modelCreator.isPojo);
    }
  };
  private static final ILocalModelCreator DETAIL_LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createDetailList(
          editor,
          arguments,
          resolver,
          modelCreator.startIndex0,
          modelCreator.isPojo);
    }
  };
  private static final ILocalModelCreator DETAIL_SET_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createDetailSet(
          editor,
          arguments,
          resolver,
          modelCreator.startIndex0,
          modelCreator.isPojo);
    }
  };
  private static final ILocalModelCreator MAPS_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createMaps(editor, arguments, resolver);
    }
  };
  private static final ILocalModelCreator MAP_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createMap(editor, arguments, resolver);
    }
  };
  private static final ILocalModelCreator SINGLE_SELECTION_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createSingleSelection(editor, arguments, resolver);
    }
  };
  private static final ILocalModelCreator MULTI_SELECTION_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createMultiSelection(editor, arguments, resolver);
    }
  };
  private static final ILocalModelCreator CHECKED_ELEMENTS_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createCheckedElements(editor, arguments, resolver);
    }
  };
  private static final ILocalModelCreator WRITABLE_LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createWritableList(editor, arguments, resolver, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator WRITABLE_SET_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createWritableSet(editor, arguments, resolver, modelCreator.startIndex0);
    }
  };
  private static final ILocalModelCreator SELF_LIST_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createSelfList(editor, arguments);
    }
  };
  private static final ILocalModelCreator SELF_SET_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createSelfSet(editor, arguments);
    }
  };
  private static final ILocalModelCreator LIST_FACTORY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createListFactory(
          editor,
          arguments,
          modelCreator.startIndex0,
          modelCreator.isPojo);
    }
  };
  private static final ILocalModelCreator SET_FACTORY_CREATOR = new ILocalModelCreator() {
    public AstObjectInfo create(BeansObserveTypeContainer container,
        AstEditor editor,
        Expression[] arguments,
        IModelResolver resolver,
        ModelCreator<BeansObserveTypeContainer> modelCreator) throws Exception {
      return container.createSetFactory(
          editor,
          arguments,
          modelCreator.startIndex0,
          modelCreator.isPojo);
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creators
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void methodCreator(LocalModelCreator modelCreator, String... signatures) {
    for (String signature : signatures) {
      METHOD_CREATORS.put(signature, modelCreator);
    }
  }

  static {
    methodCreator(
        new LocalModelCreator(0, VALUE_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeValue(java.lang.Object,java.lang.String)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeValue(java.lang.Object,java.lang.String)");
    methodCreator(
        new LocalModelCreator(1, VALUE_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeValue(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeValue(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String)");
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.value(java.lang.String)",
        new LocalModelCreator(0, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.value(java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.value(java.lang.Class,java.lang.String)",
        new LocalModelCreator(1, -1, 0, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.value(java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, 0, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.value(java.lang.String)",
        new LocalModelCreator(0, true, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.value(java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, true, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.value(java.lang.Class,java.lang.String)",
        new LocalModelCreator(1, -1, 0, true, VALUE_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.value(java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, 0, true, VALUE_PROPERTY_CREATOR));
    //
    methodCreator(
        new LocalModelCreator(0, LIST_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeList(java.lang.Object,java.lang.String)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeList(java.lang.Object,java.lang.String)");
    methodCreator(
        new LocalModelCreator(0, LIST_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeList(java.lang.Object,java.lang.String,java.lang.Class)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeList(java.lang.Object,java.lang.String,java.lang.Class)");
    methodCreator(
        new LocalModelCreator(1, LIST_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeList(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeList(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String)");
    methodCreator(
        new LocalModelCreator(1, LIST_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeList(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String,java.lang.Class)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeList(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String,java.lang.Class)");
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.list(java.lang.String)",
        new LocalModelCreator(0, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.list(java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.list(java.lang.Class,java.lang.String)",
        new LocalModelCreator(1, -1, 0, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.list(java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, 0, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.list(java.lang.String)",
        new LocalModelCreator(0, true, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.list(java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, true, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.list(java.lang.Class,java.lang.String)",
        new LocalModelCreator(1, -1, 0, true, LIST_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.list(java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, 0, true, LIST_PROPERTY_CREATOR));
    //
    methodCreator(
        new LocalModelCreator(0, SET_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeSet(java.lang.Object,java.lang.String)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeSet(java.lang.Object,java.lang.String)");
    methodCreator(
        new LocalModelCreator(0, SET_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeSet(java.lang.Object,java.lang.String,java.lang.Class)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeSet(java.lang.Object,java.lang.String,java.lang.Class)");
    methodCreator(
        new LocalModelCreator(1, SET_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeSet(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeSet(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String)");
    methodCreator(
        new LocalModelCreator(1, SET_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeSet(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String,java.lang.Class)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeSet(org.eclipse.core.databinding.observable.Realm,java.lang.Object,java.lang.String,java.lang.Class)");
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.set(java.lang.String)",
        new LocalModelCreator(0, SET_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.set(java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, SET_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.set(java.lang.Class,java.lang.String)",
        new LocalModelCreator(1, -1, 0, SET_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeanProperties.set(java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, 0, SET_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.set(java.lang.String)",
        new LocalModelCreator(0, true, SET_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.set(java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, true, SET_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.set(java.lang.Class,java.lang.String)",
        new LocalModelCreator(1, -1, 0, true, SET_PROPERTY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoProperties.set(java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, 0, true, SET_PROPERTY_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailValue(org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, DETAIL_VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.observeDetailValue(org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 1, true, DETAIL_VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailValue(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, DETAIL_VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.observeDetailValue(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 2, true, DETAIL_VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailValue(org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, 2, 1, DETAIL_VALUE_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailValue(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.Class,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, 3, 2, DETAIL_VALUE_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailList(org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, DETAIL_LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.observeDetailList(org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, true, DETAIL_LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailList(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, DETAIL_LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.observeDetailList(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, true, DETAIL_LIST_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailSet(org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, DETAIL_SET_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.observeDetailSet(org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(0, true, DETAIL_SET_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.observeDetailSet(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, DETAIL_SET_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.observeDetailSet(org.eclipse.core.databinding.observable.Realm,org.eclipse.core.databinding.observable.value.IObservableValue,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, true, DETAIL_SET_CREATOR));
    //
    methodCreator(
        new LocalModelCreator(MAPS_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeMaps(org.eclipse.core.databinding.observable.set.IObservableSet,java.lang.Class,java.lang.String[])",
        "org.eclipse.core.databinding.beans.PojoObservables.observeMaps(org.eclipse.core.databinding.observable.set.IObservableSet,java.lang.Class,java.lang.String[])");
    methodCreator(
        new LocalModelCreator(MAP_CREATOR),
        "org.eclipse.core.databinding.beans.BeansObservables.observeMap(org.eclipse.core.databinding.observable.set.IObservableSet,java.lang.Class,java.lang.String)",
        "org.eclipse.core.databinding.beans.PojoObservables.observeMap(org.eclipse.core.databinding.observable.set.IObservableSet,java.lang.Class,java.lang.String)");
    //
    METHOD_CREATORS.put(
        "org.eclipse.jface.databinding.viewers.ViewersObservables.observeSingleSelection(org.eclipse.jface.viewers.ISelectionProvider)",
        new LocalModelCreator(SINGLE_SELECTION_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.jface.databinding.viewers.ViewersObservables.observeMultiSelection(org.eclipse.jface.viewers.ISelectionProvider)",
        new LocalModelCreator(MULTI_SELECTION_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(org.eclipse.jface.viewers.ICheckable,java.lang.Object)",
        new LocalModelCreator(CHECKED_ELEMENTS_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.listFactory(java.lang.String,java.lang.Class)",
        new LocalModelCreator(LIST_FACTORY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.listFactory(org.eclipse.core.databinding.observable.Realm,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, LIST_FACTORY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.listFactory(java.lang.String,java.lang.Class)",
        new LocalModelCreator(true, LIST_FACTORY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.listFactory(org.eclipse.core.databinding.observable.Realm,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, true, LIST_FACTORY_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.setFactory(java.lang.String,java.lang.Class)",
        new LocalModelCreator(SET_FACTORY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.BeansObservables.setFactory(org.eclipse.core.databinding.observable.Realm,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, SET_FACTORY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.setFactory(java.lang.String,java.lang.Class)",
        new LocalModelCreator(true, SET_FACTORY_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.beans.PojoObservables.setFactory(org.eclipse.core.databinding.observable.Realm,java.lang.String,java.lang.Class)",
        new LocalModelCreator(1, true, SET_FACTORY_CREATOR));
    //
    CONSTRUCTOR_CREATORS.put(
        "org.eclipse.core.databinding.observable.list.WritableList.<init>(java.util.List,java.lang.Object)",
        new LocalModelCreator(0, WRITABLE_LIST_CREATOR));
    CONSTRUCTOR_CREATORS.put(
        "org.eclipse.core.databinding.observable.list.WritableList.<init>(org.eclipse.core.databinding.observable.Realm,java.util.List,java.lang.Object)",
        new LocalModelCreator(1, WRITABLE_LIST_CREATOR));
    //
    CONSTRUCTOR_CREATORS.put(
        "org.eclipse.core.databinding.observable.set.WritableSet.<init>(java.util.Collection,java.lang.Object)",
        new LocalModelCreator(0, WRITABLE_SET_CREATOR));
    CONSTRUCTOR_CREATORS.put(
        "org.eclipse.core.databinding.observable.set.WritableSet.<init>(org.eclipse.core.databinding.observable.Realm,java.util.Collection,java.lang.Object)",
        new LocalModelCreator(1, WRITABLE_SET_CREATOR));
    //
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.property.Properties.selfList(java.lang.Object)",
        new LocalModelCreator(SELF_LIST_CREATOR));
    METHOD_CREATORS.put(
        "org.eclipse.core.databinding.property.Properties.selfSet(java.lang.Object)",
        new LocalModelCreator(SELF_SET_CREATOR));
  }
}