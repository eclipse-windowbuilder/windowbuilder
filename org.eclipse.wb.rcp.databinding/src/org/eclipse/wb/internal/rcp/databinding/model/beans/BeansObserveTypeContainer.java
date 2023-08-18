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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.model.reference.FragmentReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.variable.LocalVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsRootInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.FieldBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.LocalVariableBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.MethodBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.BeanPropertiesCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ListPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SelfListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SelfSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SetPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableDetailValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.WritableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.WritableSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.BeansObservableListFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.BeansObservableSetFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.MultiSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.CheckedElementsObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.MultiSelectionObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SingleSelectionObservableCodeSupport;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Widgets container with type {@link ObserveType#BEANS}. Works on <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class BeansObserveTypeContainer extends ObserveTypeContainer {
	private DatabindingsProvider m_provider;
	private List<BeanBindableInfo> m_observables = Collections.emptyList();
	private JavaInfo m_rootJavaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeansObserveTypeContainer() {
		super(ObserveType.BEANS, false, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void initialize(IDatabindingsProvider provider) throws Exception {
		m_provider = (DatabindingsProvider) provider;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObserveTypeContainer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<IObserveInfo> getObservables() {
		return CoreUtils.cast(m_observables);
	}

	@Override
	public void synchronizeObserves(JavaInfo root, AstEditor editor, TypeDeclaration rootNode)
			throws Exception {
		ClassLoader classLoader = EditorState.get(m_rootJavaInfo.getEditor()).getEditorLoader();
		BeanSupport beanSupport = new BeanSupport(classLoader, null);
		beanSupport.setProvider(m_provider);
		// update
		synchronizeFields(beanSupport, rootNode);
		synchronizeLocalComposites(beanSupport);
		synchronizeLocalVariables(beanSupport, rootNode);
	}

	private void synchronizeFields(final BeanSupport beanSupport, TypeDeclaration rootNode)
			throws Exception {
		SynchronizeManager.synchronizeObjects(
				m_observables,
				CoreUtils.getFieldFragments(rootNode),
				new ISynchronizeProcessor<VariableDeclarationFragment, BeanBindableInfo>() {
					@Override
					public boolean handleObject(BeanBindableInfo bindable) {
						return bindable instanceof FieldBeanBindableInfo
								&& ((FieldBeanBindableInfo) bindable).getFragment() != null;
					}

					@Override
					public VariableDeclarationFragment getKeyObject(BeanBindableInfo bindable) {
						FieldBeanBindableInfo bean = (FieldBeanBindableInfo) bindable;
						return bean.getFragment();
					}

					@Override
					public boolean equals(VariableDeclarationFragment key0, VariableDeclarationFragment key1) {
						return key0 == key1;
					}

					@Override
					public BeanBindableInfo findObject(Map<VariableDeclarationFragment, BeanBindableInfo> keyObjectToObject,
							VariableDeclarationFragment key) throws Exception {
						return null;
					}

					@Override
					public BeanBindableInfo createObject(VariableDeclarationFragment fragment)
							throws Exception {
						try {
							// prepare bean class
							Type type = CoreUtils.getType(fragment, true);
							Class<?> beanClass = loadClass(AstNodeUtils.getFullyQualifiedName(type, true));
							// prepare JavaInfo
							JavaInfo widget = getJavaInfoRepresentedBy(fragment.getName().getIdentifier());
							//
							return new FieldBeanBindableInfo(beanSupport, fragment, beanClass, widget);
						} catch (ClassNotFoundException e) {
							AbstractParser.addError(m_provider.getAstEditor(), "ClassNotFoundException: "
									+ fragment, new Throwable());
							return null;
						}
					}

					@Override
					public void update(BeanBindableInfo bindable) throws Exception {
						FieldBeanBindableInfo bean = (FieldBeanBindableInfo) bindable;
						bean.update(BeansObserveTypeContainer.this);
					}
				});
	}

	private void synchronizeLocalComposites(final BeanSupport beanSupport) throws Exception {
		int observableSize = m_observables.size();
		int localCompositeIndex = -1;
		// find first local Composite
		for (int i = 0; i < observableSize; i++) {
			BeanBindableInfo bindable = m_observables.get(i);
			if (bindable instanceof FieldBeanBindableInfo
					&& ((FieldBeanBindableInfo) bindable).getFragment() == null) {
				localCompositeIndex = i;
				break;
			}
		}
		// or last field
		if (localCompositeIndex == -1) {
			for (int i = 0; i < observableSize; i++) {
				BeanBindableInfo bindable = m_observables.get(i);
				if (bindable instanceof FieldBeanBindableInfo
						&& ((FieldBeanBindableInfo) bindable).getFragment() != null) {
					localCompositeIndex = i + 1;
					break;
				}
			}
		}
		// or sets as first item
		if (localCompositeIndex == -1) {
			localCompositeIndex = 0;
		}
		// update local Composites
		SynchronizeManager.synchronizeObjects(
				m_observables.subList(localCompositeIndex, observableSize),
				getLocalComposites(),
				new ISynchronizeProcessor<JavaInfo, BeanBindableInfo>() {
					@Override
					public boolean handleObject(BeanBindableInfo bindable) {
						return bindable instanceof FieldBeanBindableInfo
								&& ((FieldBeanBindableInfo) bindable).getFragment() == null;
					}

					@Override
					public JavaInfo getKeyObject(BeanBindableInfo bindable) {
						FieldBeanBindableInfo fieldBindable = (FieldBeanBindableInfo) bindable;
						return fieldBindable.getHostJavaInfo();
					}

					@Override
					public boolean equals(JavaInfo key0, JavaInfo key1) {
						return key0 == key1;
					}

					@Override
					public BeanBindableInfo findObject(Map<JavaInfo, BeanBindableInfo> keyObjectToObject,
							JavaInfo key) throws Exception {
						return null;
					}

					@Override
					public BeanBindableInfo createObject(JavaInfo javaInfo) throws Exception {
						// prepare bean class
						Class<?> componentClass = javaInfo.getDescription().getComponentClass();
						// prepare reference provider
						IReferenceProvider javaReferenceProvider =
								new JavaInfoReferenceProvider(javaInfo, m_provider);
						FragmentReferenceProvider referenceProvider =
								new FragmentReferenceProvider(javaReferenceProvider);
						//
						return new FieldBeanBindableInfo(beanSupport,
								null,
								componentClass,
								referenceProvider,
								javaInfo);
					}

					@Override
					public void update(BeanBindableInfo bindable) throws Exception {
						FieldBeanBindableInfo bean = (FieldBeanBindableInfo) bindable;
						bean.update(BeansObserveTypeContainer.this);
					}
				});
	}

	private void synchronizeLocalVariables(final BeanSupport beanSupport, TypeDeclaration rootNode)
			throws Exception {
		int observableSize = m_observables.size();
		int localVariableIndex = observableSize;
		//
		for (int i = 0; i < observableSize; i++) {
			if (m_observables.get(i) instanceof LocalVariableBindableInfo) {
				localVariableIndex = i;
				break;
			}
		}
		// update local variables
		SynchronizeManager.synchronizeObjects(
				m_observables.subList(localVariableIndex, observableSize),
				CoreUtils.getLocalFragments(rootNode, DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME),
				new ISynchronizeProcessor<VariableDeclarationFragment, BeanBindableInfo>() {
					@Override
					public boolean handleObject(BeanBindableInfo object) {
						return true;
					}

					@Override
					public VariableDeclarationFragment getKeyObject(BeanBindableInfo bindable) {
						LocalVariableBindableInfo bean = (LocalVariableBindableInfo) bindable;
						return bean.getFragment();
					}

					@Override
					public boolean equals(VariableDeclarationFragment fragment0,
							VariableDeclarationFragment fragment1) {
						return fragment0.getName().getIdentifier().equals(fragment1.getName().getIdentifier());
					}

					@Override
					public BeanBindableInfo findObject(Map<VariableDeclarationFragment, BeanBindableInfo> keyObjectToObject,
							VariableDeclarationFragment key) throws Exception {
						return null;
					}

					@Override
					public BeanBindableInfo createObject(VariableDeclarationFragment fragment)
							throws Exception {
						try {
							// prepare bean class
							ITypeBinding typeBinding = CoreUtils.getType(fragment, true);
							Class<?> beanClass = loadClass(AstNodeUtils.getFullyQualifiedName(typeBinding, true));
							return new LocalVariableBindableInfo(beanSupport, fragment, beanClass);
						} catch (ClassNotFoundException e) {
							AbstractParser.addError(m_provider.getAstEditor(), "ClassNotFoundException: "
									+ fragment, new Throwable());
							return null;
						}
					}

					@Override
					public void update(BeanBindableInfo object) throws Exception {
					}
				});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	private List<JavaInfo> getLocalComposites() throws Exception {
		final List<JavaInfo> composites = Lists.newArrayList();
		final Class<?> compositeClass = loadClass("org.eclipse.swt.widgets.Composite");
		m_rootJavaInfo.accept(new ObjectInfoVisitor() {
			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				if (objectInfo instanceof JavaInfo javaInfo) {
					VariableSupport variableSupport = javaInfo.getVariableSupport();
					Class<?> componentClass = javaInfo.getDescription().getComponentClass();
					if (componentClass != null
							&& !compositeClass.getName().startsWith("org.eclipse.swt.")
							&& compositeClass.isAssignableFrom(componentClass)
							&& variableSupport instanceof LocalVariableSupport
							&& variableSupport.canConvertLocalToField()
							&& JavaInfoReferenceProvider.getReference(javaInfo) != null) {
						composites.add(javaInfo);
					}
				}
			}
		});
		return composites;
	}

	@Override
	public void createObservables(JavaInfo root,
			final IModelResolver resolver,
			AstEditor editor,
			TypeDeclaration rootNode) throws Exception {
		m_rootJavaInfo = root;
		m_observables = Lists.newArrayList();
		// handle fields
		ClassLoader classLoader = EditorState.get(m_rootJavaInfo.getEditor()).getEditorLoader();
		BeanSupport beanSupport = new BeanSupport(classLoader, resolver);
		beanSupport.setProvider(m_provider);
		//
		for (VariableDeclarationFragment fragment : CoreUtils.getFieldFragments(rootNode)) {
			try {
				// prepare bean class
				Type type = CoreUtils.getType(fragment, true);
				Class<?> beanClass = loadClass(AstNodeUtils.getFullyQualifiedName(type, true));
				// prepare association widget
				JavaInfo widget = getJavaInfoRepresentedBy(fragment.getName().getIdentifier());
				//
				m_observables.add(new FieldBeanBindableInfo(beanSupport, fragment, beanClass, widget));
			} catch (ClassNotFoundException e) {
				AbstractParser.addError(editor, "ClassNotFoundException: " + fragment, new Throwable());
			} catch (Throwable e) {
				throw ReflectionUtils.propagate(e);
			}
		}
		// handle all Composite declared as local variable
		for (JavaInfo javaInfo : getLocalComposites()) {
			// prepare bean class
			Class<?> componentClass = javaInfo.getDescription().getComponentClass();
			// prepare reference provider
			IReferenceProvider javaReferenceProvider =
					new JavaInfoReferenceProvider(javaInfo, m_provider);
			FragmentReferenceProvider referenceProvider =
					new FragmentReferenceProvider(javaReferenceProvider);
			//
			m_observables.add(new FieldBeanBindableInfo(beanSupport,
					null,
					componentClass,
					referenceProvider,
					javaInfo));
		}
		// prepare super class
		Class<?> superClass = Object.class;
		if (rootNode.getSuperclassType() != null) {
			String className = AstNodeUtils.getFullyQualifiedName(rootNode.getSuperclassType(), true);
			try {
				superClass = loadClass(className);
			} catch (ClassNotFoundException e) {
				AbstractParser.addError(editor, "ClassNotFoundException: " + className, new Throwable());
				superClass = Object.class;
			}
		}
		// handle methods from super class
		Set<String> methodNames = Sets.newHashSet();
		//
		BeanInfo beanInfo = Introspector.getBeanInfo(superClass);
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
			if (BeanSupport.isGetter(descriptor)) {
				String methodName = descriptor.getReadMethod().getName();
				if (!DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME.equals(methodName)) {
					methodNames.add(methodName);
					m_observables.add(new MethodBeanBindableInfo(beanSupport,
							null,
							descriptor.getPropertyType(),
							methodName + "()"));
				}
			}
		}
		// handle local methods
		for (MethodDeclaration method : rootNode.getMethods()) {
			String methodName = method.getName().getIdentifier();
			if (!methodNames.contains(methodName)
					&& !DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME.equals(methodName)
					&& methodName.startsWith("get")) {
				IMethodBinding binding = AstNodeUtils.getMethodBinding(method);
				ITypeBinding returnType = binding == null ? null : binding.getReturnType();
				//
				if (CoreUtils.isIncludeTypeBinding(returnType)) {
					String className = AstNodeUtils.getFullyQualifiedName(returnType, true);
					try {
						Class<?> propertyClass = loadClass(className);
						//
						m_observables.add(new MethodBeanBindableInfo(beanSupport,
								null,
								propertyClass,
								methodName + "()"));
					} catch (ClassNotFoundException e) {
						AbstractParser.addError(editor, "ClassNotFoundException: " + className, new Throwable());
					}
				}
			}
		}
		// handle initDataBindings() local variables
		for (VariableDeclarationFragment fragment : CoreUtils.getLocalFragments(
				rootNode,
				DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME)) {
			try {
				// prepare bean class
				ITypeBinding typeBinding = CoreUtils.getType(fragment, true);
				Class<?> beanClass = loadClass(AstNodeUtils.getFullyQualifiedName(typeBinding, true));
				m_observables.add(new LocalVariableBindableInfo(beanSupport, fragment, beanClass));
			} catch (ClassNotFoundException e) {
				AbstractParser.addError(editor, "ClassNotFoundException: " + fragment, new Throwable());
			} catch (Throwable e) {
				throw ReflectionUtils.propagate(e);
			}
		}
	}

	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			ClassInstanceCreation creation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception {
		LocalModelCreator modelCreator = CreatorManager.CONSTRUCTOR_CREATORS.get(signature);
		if (modelCreator != null) {
			return modelCreator.create(this, editor, arguments, resolver);
		}
		return null;
	}

	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver) throws Exception {
		LocalModelCreator modelCreator = CreatorManager.METHOD_CREATORS.get(signature);
		if (modelCreator != null) {
			return modelCreator.create(this, editor, arguments, resolver);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creators
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create {@link ValueBeanObservableInfo}.
	 */
	AstObjectInfo createValue(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		// prepare object
		BeanBindableInfo bindableObject = getBindableObject(arguments[startIndex]);
		if (bindableObject == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare property
		BeanPropertyBindableInfo bindableProperty =
				getBindableProperty(bindableObject, editor, arguments[startIndex + 1]);
		// create observable
		ValueBeanObservableInfo observable =
				new ValueBeanObservableInfo(bindableObject, bindableProperty);
		observable.setCodeSupport(new BeanObservableValueCodeSupport());
		return observable;
	}

	/**
	 * create {@link ListBeanObservableInfo}.
	 */
	AstObjectInfo createList(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		// prepare object
		BeanBindableInfo bindableObject = getBindableObject(arguments[startIndex]);
		if (bindableObject == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare property
		BeanPropertyBindableInfo bindableProperty =
				getBindableProperty(bindableObject, editor, arguments[startIndex + 1]);
		// create observable
		ListBeanObservableInfo observable =
				new ListBeanObservableInfo(bindableObject, bindableProperty);
		observable.setCodeSupport(new BeanObservableListCodeSupport());
		return observable;
	}

	/**
	 * Create {@link SetBeanObservableInfo}.
	 */
	AstObjectInfo createSet(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		// prepare object
		BeanBindableInfo bindableObject = getBindableObject(arguments[startIndex]);
		if (bindableObject == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare property
		BeanPropertyBindableInfo bindableProperty =
				getBindableProperty(bindableObject, editor, arguments[startIndex + 1]);
		// create observable
		SetBeanObservableInfo observable = new SetBeanObservableInfo(bindableObject, bindableProperty);
		observable.setCodeSupport(new BeanObservableSetCodeSupport());
		return observable;
	}

	AstObjectInfo createValueProperty(AstEditor editor,
			Expression[] arguments,
			int propertyIndex,
			int typeIndex,
			int beanTypeIndex,
			boolean isPojo) throws Exception {
		ValuePropertyCodeSupport codeSupport = new ValuePropertyCodeSupport();
		configureBeanProperty(
				editor,
				arguments,
				propertyIndex,
				typeIndex,
				beanTypeIndex,
				isPojo,
				codeSupport);
		return codeSupport;
	}

	AstObjectInfo createListProperty(AstEditor editor,
			Expression[] arguments,
			int propertyIndex,
			int typeIndex,
			int beanTypeIndex,
			boolean isPojo) throws Exception {
		ListPropertyCodeSupport codeSupport = new ListPropertyCodeSupport();
		configureBeanProperty(
				editor,
				arguments,
				propertyIndex,
				typeIndex,
				beanTypeIndex,
				isPojo,
				codeSupport);
		return codeSupport;
	}

	AstObjectInfo createSetProperty(AstEditor editor,
			Expression[] arguments,
			int propertyIndex,
			int typeIndex,
			int beanTypeIndex,
			boolean isPojo) throws Exception {
		SetPropertyCodeSupport codeSupport = new SetPropertyCodeSupport();
		configureBeanProperty(
				editor,
				arguments,
				propertyIndex,
				typeIndex,
				beanTypeIndex,
				isPojo,
				codeSupport);
		return codeSupport;
	}

	void configureBeanProperty(AstEditor editor,
			Expression[] arguments,
			int propertyIndex,
			int typeIndex,
			int beanTypeIndex,
			boolean isPojo,
			BeanPropertiesCodeSupport codeSupport) throws Exception {
		// prepare reference
		String propertyName = CoreUtils.evaluate(String.class, editor, arguments[propertyIndex]);
		codeSupport.setParserPropertyReference("\"" + propertyName + "\"");
		// property class
		codeSupport.setParserPropertyType(CoreUtils.evaluate(Class.class, editor, arguments, typeIndex));
		// pojo
		codeSupport.setPojoBindable(isPojo);
		// bean class
		codeSupport.setBeanType(CoreUtils.evaluate(Class.class, editor, arguments, beanTypeIndex));
	}

	/**
	 * Create {@link DetailValueBeanObservableInfo}.
	 */
	AstObjectInfo createDetailValue(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver,
			int masterIndex,
			int propertyIndex,
			int beanTypeIndex,
			boolean isPojo) throws Exception {
		// prepare master
		ObservableInfo masterObservable = getMasterObservable(editor, resolver, arguments[masterIndex]);
		if (masterObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_masterObservableArgumentNotFound,
					arguments[masterIndex]), new Throwable());
			return null;
		}
		// prepare bean type
		Class<?> beanType = CoreUtils.evaluate(Class.class, editor, arguments, beanTypeIndex);
		// prepare detail property
		String detailPropertyReference =
				"\"" + CoreUtils.evaluate(String.class, editor, arguments[propertyIndex]) + "\"";
		Class<?> detailPropertyType =
				CoreUtils.evaluate(Class.class, editor, arguments[propertyIndex + 1]);
		// create observable
		DetailValueBeanObservableInfo observable =
				new DetailValueBeanObservableInfo(masterObservable,
						beanType,
						detailPropertyReference,
						detailPropertyType);
		observable.setPojoBindable(isPojo);
		observable.setCodeSupport(new BeanObservableDetailValueCodeSupport());
		return observable;
	}

	/**
	 * Create {@link DetailListBeanObservableInfo}.
	 */
	AstObjectInfo createDetailList(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver,
			int startIndex,
			boolean isPojo) throws Exception {
		// prepare master
		ObservableInfo masterObservable = getMasterObservable(editor, resolver, arguments[startIndex]);
		if (masterObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_masterObservableArgumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare detail property
		String detailPropertyReference =
				"\"" + CoreUtils.evaluate(String.class, editor, arguments[startIndex + 1]) + "\"";
		Class<?> detailPropertyType =
				CoreUtils.evaluate(Class.class, editor, arguments[startIndex + 2]);
		// create observable
		DetailListBeanObservableInfo observable =
				new DetailListBeanObservableInfo(masterObservable,
						null,
						detailPropertyReference,
						detailPropertyType);
		observable.setPojoBindable(isPojo);
		observable.setCodeSupport(new BeanObservableDetailListCodeSupport());
		return observable;
	}

	/**
	 * Create {@link DetailSetBeanObservableInfo}.
	 */
	AstObjectInfo createDetailSet(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver,
			int startIndex,
			boolean isPojo) throws Exception {
		// prepare master
		ObservableInfo masterObservable = getMasterObservable(editor, resolver, arguments[startIndex]);
		if (masterObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_masterObservableArgumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare detail property
		String detailPropertyReference =
				"\"" + CoreUtils.evaluate(String.class, editor, arguments[startIndex + 1]) + "\"";
		Class<?> detailPropertyType =
				CoreUtils.evaluate(Class.class, editor, arguments[startIndex + 2]);
		// create observable
		DetailSetBeanObservableInfo observable =
				new DetailSetBeanObservableInfo(masterObservable,
						null,
						detailPropertyReference,
						detailPropertyType);
		observable.setPojoBindable(isPojo);
		observable.setCodeSupport(new BeanObservableDetailSetCodeSupport());
		return observable;
	}

	/**
	 * Create {@link MapsBeanObservableInfo}.
	 */
	AstObjectInfo createMaps(AstEditor editor, Expression[] arguments, IModelResolver resolver)
			throws Exception {
		// prepare domain
		ObservableInfo domainObservable = (ObservableInfo) resolver.getModel(arguments[0]);
		if (domainObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_domainObservableArgumentNotFound,
					arguments[0]), new Throwable());
			return null;
		}
		// prepare element type
		Class<?> elementType = CoreUtils.evaluate(Class.class, editor, arguments[1]);
		// prepare properties
		String[] properties = CoreUtils.evaluate(String[].class, editor, arguments[2]);
		// create observable
		return new MapsBeanObservableInfo(domainObservable, elementType, properties);
	}

	/**
	 * Create {@link MapsBeanObservableInfo}.
	 */
	AstObjectInfo createMap(AstEditor editor, Expression[] arguments, IModelResolver resolver)
			throws Exception {
		// prepare domain
		ObservableInfo domainObservable = (ObservableInfo) resolver.getModel(arguments[0]);
		if (domainObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_domainObservableArgumentNotFound,
					arguments[0]), new Throwable());
			return null;
		}
		// prepare element type
		Class<?> elementType = CoreUtils.evaluate(Class.class, editor, arguments[1]);
		// prepare properties
		String property = CoreUtils.evaluate(String.class, editor, arguments[2]);
		// create observable
		return new MapsBeanObservableInfo(domainObservable, elementType, new String[]{property});
	}

	/**
	 * Create {@link WritableListBeanObservableInfo}.
	 */
	AstObjectInfo createWritableList(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver,
			int startIndex) throws Exception {
		// prepare object
		BeanBindableInfo bindableObject = getBindableObject(arguments[startIndex]);
		if (bindableObject == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare property
		CollectionPropertyBindableInfo bindableProperty =
				(CollectionPropertyBindableInfo) bindableObject.resolvePropertyReference(bindableObject.getReference());
		Assert.isNotNull(bindableProperty);
		// prepare element type
		Class<?> elementType = CoreUtils.evaluate(Class.class, editor, arguments[startIndex + 1]);
		// create observable
		WritableListBeanObservableInfo observable =
				new WritableListBeanObservableInfo(bindableObject, bindableProperty, elementType);
		observable.setCodeSupport(new WritableListCodeSupport());
		return observable;
	}

	/**
	 * Create {@link WritableSetBeanObservableInfo}.
	 */
	AstObjectInfo createWritableSet(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver,
			int startIndex) throws Exception {
		// prepare object
		BeanBindableInfo bindableObject = getBindableObject(arguments[startIndex]);
		if (bindableObject == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare property
		CollectionPropertyBindableInfo bindableProperty =
				(CollectionPropertyBindableInfo) bindableObject.resolvePropertyReference(bindableObject.getReference());
		Assert.isNotNull(bindableProperty);
		// prepare element type
		Class<?> elementType = CoreUtils.evaluate(Class.class, editor, arguments[startIndex + 1]);
		// create observable
		WritableSetBeanObservableInfo observable =
				new WritableSetBeanObservableInfo(bindableObject, bindableProperty, elementType);
		observable.setCodeSupport(new WritableSetCodeSupport());
		return observable;
	}

	AstObjectInfo createSelfList(AstEditor editor, Expression[] arguments) throws Exception {
		SelfListCodeSupport codeSupport = new SelfListCodeSupport();
		codeSupport.setParserPropertyType(CoreUtils.evaluate(Class.class, editor, arguments[0]));
		return codeSupport;
	}

	AstObjectInfo createSelfSet(AstEditor editor, Expression[] arguments) throws Exception {
		SelfSetCodeSupport codeSupport = new SelfSetCodeSupport();
		codeSupport.setParserPropertyType(CoreUtils.evaluate(Class.class, editor, arguments[0]));
		return codeSupport;
	}

	AstObjectInfo createSingleSelection(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver) throws Exception {
		BeanBindableInfo bindableObject =
				m_rootJavaInfo.getChildRepresentedBy(arguments[0]) == null
				? getBindableObject(arguments[0])
						: null;
		if (bindableObject != null) {
			SingleSelectionObservableInfo observable = new SingleSelectionObservableInfo(bindableObject);
			observable.setCodeSupport(new SingleSelectionObservableCodeSupport());
			return observable;
		}
		return null;
	}

	AstObjectInfo createMultiSelection(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver) throws Exception {
		BeanBindableInfo bindableObject =
				m_rootJavaInfo.getChildRepresentedBy(arguments[0]) == null
				? getBindableObject(arguments[0])
						: null;
		if (bindableObject != null) {
			MultiSelectionObservableInfo observable = new MultiSelectionObservableInfo(bindableObject);
			observable.setCodeSupport(new MultiSelectionObservableCodeSupport());
			return observable;
		}
		return null;
	}

	AstObjectInfo createCheckedElements(AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver) throws Exception {
		BeanBindableInfo bindableObject =
				m_rootJavaInfo.getChildRepresentedBy(arguments[0]) == null
				? getBindableObject(arguments[0])
						: null;
		if (bindableObject != null) {
			// prepare element type
			Class<?> elementType = CoreUtils.evaluate(Class.class, editor, arguments[1]);
			//
			CheckedElementsObservableInfo observable =
					new CheckedElementsObservableInfo(bindableObject, elementType);
			observable.setCodeSupport(new CheckedElementsObservableCodeSupport());
			return observable;
		}
		return null;
	}

	AstObjectInfo createListFactory(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			boolean isPojo) throws Exception {
		BeansObservableListFactoryInfo observableFactory = new BeansObservableListFactoryInfo();
		observableFactory.setPojoBindable(isPojo);
		// prepare property
		observableFactory.setPropertyName("\""
				+ CoreUtils.evaluate(String.class, editor, arguments[startIndex])
				+ "\"");
		// prepare element type
		observableFactory.setElementType(CoreUtils.evaluate(
				Class.class,
				editor,
				arguments[startIndex + 1]));
		//
		return observableFactory;
	}

	AstObjectInfo createSetFactory(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			boolean isPojo) throws Exception {
		BeansObservableSetFactoryInfo observableFactory = new BeansObservableSetFactoryInfo();
		observableFactory.setPojoBindable(isPojo);
		// prepare property
		observableFactory.setPropertyName("\""
				+ CoreUtils.evaluate(String.class, editor, arguments[startIndex])
				+ "\"");
		// prepare element type
		observableFactory.setElementType(CoreUtils.evaluate(
				Class.class,
				editor,
				arguments[startIndex + 1]));
		//
		return observableFactory;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link BeanBindableInfo} that association with given {@link Expression}.
	 */
	public BeanBindableInfo getBindableObject(Expression expression) throws Exception {
		// prepare reference
		String reference = CoreUtils.getNodeReference(expression);
		// find object
		for (BeanBindableInfo bindableObject : m_observables) {
			BeanBindableInfo result = (BeanBindableInfo) bindableObject.resolveReference(reference);
			//
			if (result != null) {
				return result;
			}
		}
		//
		return null;
	}

	public BeanBindableInfo resolve(JavaInfo javaInfo) throws Exception {
		// prepare reference
		String reference = JavaInfoReferenceProvider.getReference(javaInfo);
		Assert.isNotNull(reference);
		// find object
		for (BeanBindableInfo bindableObject : m_observables) {
			BeanBindableInfo result = (BeanBindableInfo) bindableObject.resolveReference(reference);
			//
			if (result != null) {
				return result;
			}
		}
		//
		return null;
	}

	/**
	 * @return {@link BeanPropertyBindableInfo} property that association with given
	 *         {@link Expression}.
	 */
	private BeanPropertyBindableInfo getBindableProperty(BeanBindableInfo bindableObject,
			AstEditor editor,
			Expression expression) throws Exception {
		// prepare reference
		String propertyName = CoreUtils.evaluate(String.class, editor, expression);
		String propertyReference = "\"" + propertyName + "\"";
		// find property
		BeanPropertyBindableInfo bindableProperty =
				(BeanPropertyBindableInfo) bindableObject.resolvePropertyReference(propertyReference);
		if (bindableProperty == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_beanPropertyNotFound,
					expression,
					bindableObject.getReference()), new Throwable());
			bindableProperty =
					new BeanPropertyBindableInfo(bindableObject.getBeanSupport(),
							null,
							propertyName,
							null,
							propertyReference);
		}
		return bindableProperty;
	}

	/**
	 * @return {@link ObservableInfo} master that association with given {@link Expression}.
	 */
	public static ObservableInfo getMasterObservable(AstEditor editor,
			IModelResolver resolver,
			Expression expression) throws Exception {
		// prepare master detail observable
		ObservableInfo masterDetailObservable = (ObservableInfo) resolver.getModel(expression);
		if (masterDetailObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.BeansObserveTypeContainer_masterObservableNotFound,
					expression), new Throwable());
			return null;
		}
		Assert.instanceOf(IMasterDetailProvider.class, masterDetailObservable);
		// extract master observable
		IMasterDetailProvider masterDetailProvider = (IMasterDetailProvider) masterDetailObservable;
		ObservableInfo masterObservable = masterDetailProvider.getMasterObservable();
		//
		return masterObservable;
	}

	/**
	 * Helper method for load classes over editor class loader.
	 */
	private Class<?> loadClass(String className) throws Exception {
		return CoreUtils.load(EditorState.get(m_rootJavaInfo.getEditor()).getEditorLoader(), className);
	}

	public JavaInfo getJavaInfoRepresentedBy(final String variable) {
		final JavaInfo result[] = new JavaInfo[1];
		m_rootJavaInfo.accept(new ObjectInfoVisitor() {
			@Override
			public boolean visit(ObjectInfo objectInfo) throws Exception {
				if (result[0] == null && objectInfo instanceof JavaInfo javaInfo) {
					if (variable.equals(JavaInfoReferenceProvider.getReference(javaInfo))) {
						result[0] = javaInfo;
					}
				}
				return result[0] == null;
			}
		});
		return result[0];
	}
}