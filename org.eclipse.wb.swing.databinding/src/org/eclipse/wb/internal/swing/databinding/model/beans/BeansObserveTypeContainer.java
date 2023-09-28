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
package org.eclipse.wb.internal.swing.databinding.model.beans;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsRootInfo;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.components.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.BeanPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.ElPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Container with type {@link ObserveType#BEANS}. Works on <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class BeansObserveTypeContainer extends ObserveTypeContainer {
	private static final String OBJECT_PROPERTY_CREATE =
			"org.jdesktop.beansbinding.ObjectProperty.create()";
	private static final String BEAN_PROPERTY_CREATE_1 =
			"org.jdesktop.beansbinding.BeanProperty.create(java.lang.String)";
	private static final String BEAN_PROPERTY_CREATE_2 =
			"org.jdesktop.beansbinding.BeanProperty.create(org.jdesktop.beansbinding.Property,java.lang.String)";
	private static final String EL_PROPERTY_CREATE_1 =
			"org.jdesktop.beansbinding.ELProperty.create(java.lang.String)";
	private static final String EL_PROPERTY_CREATE_2 =
			"org.jdesktop.beansbinding.ELProperty.create(org.jdesktop.beansbinding.Property,java.lang.String)";
	private List<IObserveInfo> m_observes = Collections.emptyList();
	private JavaInfo m_javaInfoRoot;

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
	// IObserveInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<IObserveInfo> getObservables() {
		return m_observes;
	}

	@Override
	public void synchronizeObserves(JavaInfo root, final AstEditor editor, TypeDeclaration rootNode)
			throws Exception {
		final BeanSupport beanSupport = new BeanSupport();
		//
		IObserveInfo virtualObserve = m_observes.remove(0);
		// fields
		SynchronizeManager.synchronizeObjects(
				m_observes,
				CoreUtils.getFieldFragments(rootNode),
				new ISynchronizeProcessor<VariableDeclarationFragment, IObserveInfo>() {
					@Override
					public boolean handleObject(IObserveInfo object) {
						return object instanceof FieldBeanObserveInfo;
					}

					@Override
					public VariableDeclarationFragment getKeyObject(IObserveInfo observe) {
						FieldBeanObserveInfo beanObserve = (FieldBeanObserveInfo) observe;
						return beanObserve.getFragment();
					}

					@Override
					public boolean equals(VariableDeclarationFragment key0, VariableDeclarationFragment key1) {
						return key0 == key1;
					}

					@Override
					public IObserveInfo findObject(Map<VariableDeclarationFragment, IObserveInfo> keyObjectToObject,
							VariableDeclarationFragment key) throws Exception {
						return null;
					}

					@Override
					public IObserveInfo createObject(VariableDeclarationFragment fragment) throws Exception {
						Type type = CoreUtils.getType(fragment, true);
						//
						if (CoreUtils.isIncludeType(type)) {
							// prepare bean type
							ITypeBinding binding = AstNodeUtils.getTypeBinding(type);
							if (binding != null) {
								try {
									IGenericType beanObjectType = GenericUtils.getObjectType(editor, binding);
									// prepare association component
									JavaInfo component = getJavaInfoRepresentedBy(fragment.getName().getIdentifier());
									//
									return new FieldBeanObserveInfo(beanSupport, fragment, beanObjectType, component);
								} catch (ClassNotFoundException e) {
									AbstractParser.addError(m_javaInfoRoot.getEditor(), "ClassNotFoundException: "
											+ fragment, new Throwable());
								}
							}
						}
						return null;
					}

					@Override
					public void update(IObserveInfo object) throws Exception {
					}
				});
		// local variables
		int observableSize = m_observes.size();
		int localVariableIndex = observableSize;
		//
		for (int i = 0; i < observableSize; i++) {
			if (m_observes.get(i) instanceof LocalVariableObserveInfo) {
				localVariableIndex = i;
				break;
			}
		}
		//
		SynchronizeManager.synchronizeObjects(
				m_observes.subList(localVariableIndex, observableSize),
				CoreUtils.getLocalFragments(rootNode, DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME),
				new ISynchronizeProcessor<VariableDeclarationFragment, IObserveInfo>() {
					@Override
					public boolean handleObject(IObserveInfo object) {
						return true;
					}

					@Override
					public VariableDeclarationFragment getKeyObject(IObserveInfo observe) {
						LocalVariableObserveInfo beanObserve = (LocalVariableObserveInfo) observe;
						return beanObserve.getFragment();
					}

					@Override
					public boolean equals(VariableDeclarationFragment fragment0,
							VariableDeclarationFragment fragment1) {
						return fragment0.getName().getIdentifier().equals(fragment1.getName().getIdentifier());
					}

					@Override
					public IObserveInfo findObject(Map<VariableDeclarationFragment, IObserveInfo> keyObjectToObject,
							VariableDeclarationFragment key) throws Exception {
						return null;
					}

					@Override
					public IObserveInfo createObject(VariableDeclarationFragment fragment) throws Exception {
						try {
							ITypeBinding binding = CoreUtils.getType(fragment, true);
							// prepare bean class
							IGenericType beanObjectType = GenericUtils.getObjectType(editor, binding);
							//
							return new LocalVariableObserveInfo(beanSupport, fragment, beanObjectType);
						} catch (ClassNotFoundException e) {
							AbstractParser.addError(m_javaInfoRoot.getEditor(), "ClassNotFoundException: "
									+ fragment, new Throwable());
							return null;
						}
					}

					@Override
					public void update(IObserveInfo object) throws Exception {
					}
				});
		//
		m_observes.add(0, virtualObserve);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createObservables(JavaInfo root,
			IModelResolver resolver,
			AstEditor editor,
			TypeDeclaration rootNode) throws Exception {
		m_javaInfoRoot = root;
		//
		m_observes = new ArrayList<>();
		// add virtual
		m_observes.add(new VirtualObserveInfo());
		// handle fields
		BeanSupport beanSupport = new BeanSupport();
		for (VariableDeclarationFragment fragment : CoreUtils.getFieldFragments(rootNode)) {
			try {
				Type type = CoreUtils.getType(fragment, true);
				//
				if (CoreUtils.isIncludeType(type)) {
					// prepare bean type
					ITypeBinding binding = AstNodeUtils.getTypeBinding(type);
					if (binding != null) {
						IGenericType beanObjectType = GenericUtils.getObjectType(editor, binding);
						// prepare association component
						JavaInfo component = getJavaInfoRepresentedBy(fragment.getName().getIdentifier());
						m_observes.add(new FieldBeanObserveInfo(beanSupport,
								fragment,
								beanObjectType,
								component));
					}
				}
			} catch (ClassNotFoundException e) {
				AbstractParser.addError(editor, "ClassNotFoundException: " + fragment, new Throwable());
			} catch (NoClassDefFoundError e) {
				AbstractParser.addError(editor, "NoClassDefFoundError: " + fragment, new Throwable());
			} catch (Throwable e) {
				throw ReflectionUtils.propagate(e);
			}
		}
		// handle initDataBindings() local variables
		for (VariableDeclarationFragment fragment : CoreUtils.getLocalFragments(
				rootNode,
				DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME)) {
			try {
				ITypeBinding binding = CoreUtils.getType(fragment, true);
				// prepare bean class
				IGenericType beanObjectType = GenericUtils.getObjectType(editor, binding);
				//
				m_observes.add(new LocalVariableObserveInfo(beanSupport, fragment, beanObjectType));
			} catch (ClassNotFoundException e) {
				AbstractParser.addError(editor, "ClassNotFoundException: " + fragment, new Throwable());
			} catch (NoClassDefFoundError e) {
				AbstractParser.addError(editor, "NoClassDefFoundError: " + fragment, new Throwable());
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
		return null;
	}

	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver) throws Exception {
		// ObjectProperty.create()
		if (OBJECT_PROPERTY_CREATE.equals(signature)) {
			IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 1);
			return new ObjectPropertyInfo(types[0]);
		}
		// BeanProperty.create(String)
		if (BEAN_PROPERTY_CREATE_1.equals(signature)) {
			IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 2);
			String path = CoreUtils.evaluate(String.class, editor, arguments[0]);
			return new BeanPropertyInfo(types[0], types[1], null, path);
		}
		// BeanProperty.create(Property, String)
		if (BEAN_PROPERTY_CREATE_2.equals(signature)) {
			IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 2);
			PropertyInfo baseProperty = (PropertyInfo) resolver.getModel(arguments[0]);
			if (baseProperty == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.BeansObserveTypeContainer_errArgumentNotFound,
						arguments[0]), new Throwable());
				return null;
			}
			GenericUtils.assertEquals(baseProperty.getSourceObjectType(), types[0]);
			String path = CoreUtils.evaluate(String.class, editor, arguments[1]);
			return new BeanPropertyInfo(types[0], types[1], baseProperty, path);
		}
		// ELProperty.create(String)
		if (EL_PROPERTY_CREATE_1.equals(signature)) {
			IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 2);
			String path = CoreUtils.evaluate(String.class, editor, arguments[0]);
			return new ElPropertyInfo(types[0], types[1], null, path);
		}
		// ELProperty.create(Property, String)
		if (EL_PROPERTY_CREATE_2.equals(signature)) {
			IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 2);
			PropertyInfo baseProperty = (PropertyInfo) resolver.getModel(arguments[0]);
			if (baseProperty == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.BeansObserveTypeContainer_errArgumentNotFound,
						arguments[0]), new Throwable());
				return null;
			}
			GenericUtils.assertEquals(baseProperty.getSourceObjectType(), types[0]);
			String path = CoreUtils.evaluate(String.class, editor, arguments[1]);
			return new ElPropertyInfo(types[0], types[1], baseProperty, path);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public ObserveInfo resolve(Expression expression) throws Exception {
		String reference = CoreUtils.getNodeReference(expression);
		for (ObserveInfo observe : CoreUtils.<ObserveInfo>cast(m_observes)) {
			if (reference.equals(observe.getReference())) {
				return observe;
			}
		}
		return null;
	}

	public ObserveInfo resolve(JavaInfo javaInfo) throws Exception {
		String reference = JavaInfoReferenceProvider.getReference(javaInfo);
		Assert.isNotNull(reference);
		for (ObserveInfo observe : CoreUtils.<ObserveInfo>cast(m_observes)) {
			if (reference.equals(observe.getReference())) {
				return observe;
			}
		}
		return null;
	}

	private JavaInfo getJavaInfoRepresentedBy(final String variable) {
		final JavaInfo result[] = new JavaInfo[1];
		m_javaInfoRoot.accept(new ObjectInfoVisitor() {
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