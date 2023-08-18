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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolverFilter;
import org.eclipse.wb.internal.core.databinding.parser.IModelSupport;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.databinding.emf.Activator;
import org.eclipse.wb.internal.rcp.databinding.emf.Messages;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EObjectBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailValueEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableDetailListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableDetailValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.EmfObservableValueCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.ListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.MapsEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.ValueEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer.EmfBeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer.EmfTreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer.EmfTreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties.EmfListPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties.EmfValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.IMasterDetailProvider;
import org.eclipse.wb.internal.rcp.databinding.model.beans.direct.DirectFieldModelSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Container for EMF objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class EmfObserveTypeContainer extends ObserveTypeContainer {
	public static final ObserveType TYPE = new ObserveType("Eclipse Modeling Framework",
			Activator.getImage("EMF_ObserveType2.png"));
	private List<EObjectBindableInfo> m_observables = Collections.emptyList();
	private JavaInfo m_javaInfoRoot;
	private PropertiesSupport m_propertiesSupport;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EmfObserveTypeContainer() {
		super(TYPE, false, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Accept
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean accept(JavaInfo javaInfoRoot) {
		try {
			IJavaProject javaProject = javaInfoRoot.getEditor().getJavaProject();
			return ProjectUtils.hasType(javaProject, "org.eclipse.emf.ecore.EObject");
		} catch (Throwable e) {
			return false;
		}
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
	// IObserveTypeContainer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<IObserveInfo> getObservables() {
		return CoreUtils.cast(m_observables);
	}

	@Override
	public void synchronizeObserves(JavaInfo root, final AstEditor editor, TypeDeclaration rootNode)
			throws Exception {
		IJavaProject javaProject = editor.getJavaProject();
		ClassLoader classLoader = JavaInfoUtils.getClassLoader(m_javaInfoRoot);
		List<VariableDeclarationFragment> fragments = CoreUtils.getFieldFragments(rootNode);
		m_propertiesSupport = new PropertiesSupport(javaProject, classLoader, fragments);
		//
		for (Iterator<VariableDeclarationFragment> I = fragments.iterator(); I.hasNext();) {
			try {
				VariableDeclarationFragment fragment = I.next();
				Type type = CoreUtils.getType(fragment, false);
				Class<?> eObjectClass = loadClass(AstNodeUtils.getFullyQualifiedName(type, true));
				//
				if (!eObjectClass.isInterface()
						|| !m_propertiesSupport.getEObjectClass().isAssignableFrom(eObjectClass)
						&& !CoreUtils.isAssignableFrom(m_propertiesSupport.getIObservableValue(), eObjectClass)) {
					I.remove();
				}
			} catch (ClassNotFoundException e) {
				I.remove();
			}
		}
		//
		SynchronizeManager.synchronizeObjects(
				m_observables,
				fragments,
				new ISynchronizeProcessor<VariableDeclarationFragment, EObjectBindableInfo>() {
					@Override
					public boolean handleObject(EObjectBindableInfo object) {
						return true;
					}

					@Override
					public VariableDeclarationFragment getKeyObject(EObjectBindableInfo eObject) {
						return eObject.getFragment();
					}

					@Override
					public boolean equals(VariableDeclarationFragment key0, VariableDeclarationFragment key1) {
						return key0 == key1;
					}

					@Override
					public EObjectBindableInfo findObject(Map<VariableDeclarationFragment, EObjectBindableInfo> keyObjectToObject,
							VariableDeclarationFragment key) throws Exception {
						return null;
					}

					@Override
					public EObjectBindableInfo createObject(VariableDeclarationFragment fragment)
							throws Exception {
						try {
							Type type = CoreUtils.getType(fragment, true);
							// prepare bean class
							Class<?> eObjectClass = loadClass(AstNodeUtils.getFullyQualifiedName(type, true));
							return new EObjectBindableInfo(eObjectClass, fragment, m_propertiesSupport, null);
						} catch (ClassNotFoundException e) {
							AbstractParser.addError(
									editor,
									"ClassNotFoundException: " + fragment,
									new Throwable());
							return null;
						}
					}

					@Override
					public void update(EObjectBindableInfo object) throws Exception {
					}
				});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parse
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createObservables(JavaInfo root,
			IModelResolver resolver,
			AstEditor editor,
			TypeDeclaration rootNode) throws Exception {
		m_javaInfoRoot = root;
		m_observables = Lists.newArrayList();
		//
		IJavaProject javaProject = editor.getJavaProject();
		ClassLoader classLoader = JavaInfoUtils.getClassLoader(m_javaInfoRoot);
		List<VariableDeclarationFragment> fragments = CoreUtils.getFieldFragments(rootNode);
		//
		m_propertiesSupport = new PropertiesSupport(javaProject, classLoader, fragments);
		//
		for (VariableDeclarationFragment fragment : fragments) {
			try {
				// prepare type
				Type type = CoreUtils.getType(fragment, true);
				// prepare bean class
				Class<?> eObjectClass = loadClass(AstNodeUtils.getFullyQualifiedName(type, true));
				//
				if (eObjectClass.isInterface()
						&& (m_propertiesSupport.getEObjectClass().isAssignableFrom(eObjectClass) || CoreUtils.isAssignableFrom(
								m_propertiesSupport.getIObservableValue(),
								eObjectClass))) {
					m_observables.add(new EObjectBindableInfo(eObjectClass,
							fragment,
							m_propertiesSupport,
							resolver));
				}
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
		ITypeBinding binding = AstNodeUtils.getTypeBinding(creation);
		if (binding == null) {
			return null;
		}
		//
		// IObservableFactory
		//
		if (AstNodeUtils.isSuccessorOf(
				binding,
				"org.eclipse.core.databinding.observable.masterdetail.IObservableFactory")) {
			String className = AstNodeUtils.getFullyQualifiedName(binding, false);
			//
			if (AstNodeUtils.isSuccessorOf(
					binding,
					"org.eclipse.wb.rcp.databinding.EMFBeansListObservableFactory")) {
				return createObserveFactory(editor, signature, arguments, className, false);
			}
			if (AstNodeUtils.isSuccessorOf(
					binding,
					"org.eclipse.wb.rcp.databinding.EMFEditBeansListObservableFactory")) {
				return createObserveFactory(editor, signature, arguments, className, true);
			}
		}
		//
		// TreeStructureAdvisor
		//
		if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.wb.rcp.databinding.EMFTreeBeanAdvisor")) {
			Assert.isTrue(signature.endsWith("<init>(org.eclipse.emf.ecore.EStructuralFeature,org.eclipse.emf.ecore.EStructuralFeature,org.eclipse.emf.ecore.EStructuralFeature)"));
			// create advisor
			EmfTreeBeanAdvisorInfo advisorInfo = new EmfTreeBeanAdvisorInfo(m_propertiesSupport);
			// prepare parent property
			advisorInfo.setEMFParentProperty(getArgumentReference(editor, arguments[0]));
			// prepare children property
			advisorInfo.setEMFChildrenProperty(getArgumentReference(editor, arguments[1]));
			// prepare hasChildren property
			advisorInfo.setEMFHasChildrenProperty(getArgumentReference(editor, arguments[2]));
			//
			return advisorInfo;
		}
		//
		// LabelProvider
		//
		if (AstNodeUtils.isSuccessorOf(
				binding,
				"org.eclipse.wb.rcp.databinding.EMFTreeObservableLabelProvider")) {
			Assert.isTrue(signature.endsWith("<init>(org.eclipse.core.databinding.observable.set.IObservableSet,org.eclipse.emf.ecore.EStructuralFeature,org.eclipse.emf.ecore.EStructuralFeature)"));
			// prepare allElements observable
			KnownElementsObservableInfo allElementsObservable =
					(KnownElementsObservableInfo) resolver.getModel(arguments[0]);
			if (allElementsObservable == null) {
				AbstractParser.addError(
						editor,
						MessageFormat.format(Messages.EmfObserveTypeContainer_argumentNotFound, arguments[0]),
						new Throwable());
				return null;
			}
			// create label provider
			EmfTreeObservableLabelProviderInfo labelProvider =
					new EmfTreeObservableLabelProviderInfo(AstNodeUtils.getFullyQualifiedName(binding, false),
							allElementsObservable,
							m_propertiesSupport);
			// prepare text property
			labelProvider.setEMFTextProperty(getArgumentReference(editor, arguments[1]));
			// prepare image property
			labelProvider.setEMFImageProperty(getArgumentReference(editor, arguments[2]));
			//
			return labelProvider;
		}
		return null;
	}

	private static String getArgumentReference(AstEditor editor, Expression argument) {
		if (argument instanceof NullLiteral) {
			return null;
		}
		return CoreUtils.getNodeReference(argument);
	}

	EmfBeansObservableFactoryInfo createObserveFactory(AstEditor editor,
			String signature,
			Expression[] arguments,
			String className,
			boolean editingDomain) throws Exception {
		if (editingDomain) {
			m_propertiesSupport.checkEditingDomain(arguments[1]);
			Assert.isTrue(signature.endsWith("<init>(java.lang.Class,org.eclipse.emf.edit.domain.EditingDomain,org.eclipse.emf.ecore.EStructuralFeature)"));
		} else {
			Assert.isTrue(signature.endsWith("<init>(java.lang.Class,org.eclipse.emf.ecore.EStructuralFeature)"));
		}
		// create factory
		EmfBeansObservableFactoryInfo observableFactory =
				EmfBeansObservableFactoryInfo.create(className, m_propertiesSupport);
		// prepare element type
		observableFactory.setElementType(CoreUtils.evaluate(Class.class, editor, arguments[0]));
		// prepare property
		observableFactory.setEMFPropertyReference(CoreUtils.getNodeReference(arguments[editingDomain
		                                                                               ? 2
		                                                                            		   : 1]));
		//
		return observableFactory;
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

	AstObjectInfo createValueProperty(AstEditor editor, Expression[] arguments, int startIndex) {
		EmfValuePropertyCodeSupport codeSupport = new EmfValuePropertyCodeSupport();
		codeSupport.setParserPropertyReference(CoreUtils.getNodeReference(arguments[startIndex]));
		return codeSupport;
	}

	AstObjectInfo createValuePropertyEdit(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createValueProperty(editor, arguments, startIndex + 1);
	}

	private static String getPathPropertyReference(Expression expression) {
		Assert.instanceOf(MethodInvocation.class, expression);
		StringBuffer reference = new StringBuffer("org.eclipse.emf.databinding.FeaturePath.fromList(");
		int index = 0;
		for (Expression argument : DomGenerics.arguments(expression)) {
			if (index > 0) {
				reference.append(", ");
			}
			reference.append(CoreUtils.getNodeReference(argument));
			index++;
		}
		Assert.isTrue(index > 0);
		reference.append(")");
		return reference.toString();
	}

	AstObjectInfo createValuePathProperty(AstEditor editor, Expression[] arguments, int startIndex) {
		EmfValuePropertyCodeSupport codeSupport = new EmfValuePropertyCodeSupport();
		codeSupport.setParserPropertyReference(getPathPropertyReference(arguments[startIndex]));
		return codeSupport;
	}

	AstObjectInfo createValuePathPropertyEdit(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createValuePathProperty(editor, arguments, startIndex + 1);
	}

	AstObjectInfo createListProperty(AstEditor editor, Expression[] arguments, int startIndex) {
		EmfListPropertyCodeSupport codeSupport = new EmfListPropertyCodeSupport();
		codeSupport.setParserPropertyReference(CoreUtils.getNodeReference(arguments[startIndex]));
		return codeSupport;
	}

	AstObjectInfo createListPropertyEdit(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createListProperty(editor, arguments, startIndex + 1);
	}

	AstObjectInfo createListPathProperty(AstEditor editor, Expression[] arguments, int startIndex) {
		EmfListPropertyCodeSupport codeSupport = new EmfListPropertyCodeSupport();
		codeSupport.setParserPropertyReference(getPathPropertyReference(arguments[startIndex]));
		return codeSupport;
	}

	AstObjectInfo createListPathPropertyEdit(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createListPathProperty(editor, arguments, startIndex + 1);
	}

	AstObjectInfo createValue(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		// prepare object
		EObjectBindableInfo eObject = getEObject(arguments[startIndex]);
		if (eObject == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.EmfObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare property
		EPropertyBindableInfo eProperty = getEProperty(eObject, arguments[startIndex + 1]);
		// create observable
		ObservableInfo observable = new ValueEmfObservableInfo(eObject, eProperty);
		observable.setCodeSupport(new EmfObservableValueCodeSupport());
		//
		return observable;
	}

	AstObjectInfo createValueEdit(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createValue(editor, arguments, startIndex + 1);
	}

	AstObjectInfo createList(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		// prepare object
		EObjectBindableInfo eObject = getEObject(arguments[startIndex]);
		if (eObject == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.EmfObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare property
		EPropertyBindableInfo eProperty = getEProperty(eObject, arguments[startIndex + 1]);
		// create observable
		ObservableInfo observable = new ListEmfObservableInfo(eObject, eProperty);
		observable.setCodeSupport(new EmfObservableListCodeSupport());
		//
		return observable;
	}

	AstObjectInfo createListEdit(AstEditor editor, Expression[] arguments, int startIndex)
			throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createList(editor, arguments, startIndex + 1);
	}

	public static ObservableInfo getMasterObservable(AstEditor editor,
			IModelResolver resolver,
			Expression expression) throws Exception {
		// prepare master detail observable
		ObservableInfo masterDetailObservable =
				(ObservableInfo) resolver.getModel(expression, new IModelResolverFilter() {
					@Override
					public boolean accept(IModelSupport modelSupport) throws Exception {
						if (modelSupport instanceof DirectFieldModelSupport) {
							ObservableInfo observable = (ObservableInfo) modelSupport.getModel();
							return observable.getBindableObject().getType() == TYPE;
						}
						return true;
					}
				});
		if (masterDetailObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.EmfObserveTypeContainer_masterObservableNotFound,
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

	private void addEMFPackage(Expression expression) throws Exception {
		if (expression instanceof QualifiedName qualifiedName) {
			String literalsClass =
					AstNodeUtils.getTypeBinding(qualifiedName.getQualifier()).getQualifiedName();
			m_propertiesSupport.addPackage(literalsClass);
		}
	}

	AstObjectInfo createDetailValue(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			IModelResolver resolver) throws Exception {
		// prepare master
		ObservableInfo masterObservable = getMasterObservable(editor, resolver, arguments[startIndex]);
		if (masterObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.EmfObserveTypeContainer_masterObservableArgumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare detail property
		addEMFPackage(arguments[startIndex + 1]);
		String detailPropertyReference = CoreUtils.getNodeReference(arguments[startIndex + 1]);
		// create observable
		DetailValueEmfObservableInfo observeDetailValue =
				new DetailValueEmfObservableInfo(masterObservable, m_propertiesSupport);
		observeDetailValue.setDetailPropertyReference(null, detailPropertyReference);
		observeDetailValue.setCodeSupport(new EmfObservableDetailValueCodeSupport());
		//
		return observeDetailValue;
	}

	AstObjectInfo createDetailValueEdit(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			IModelResolver resolver) throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createDetailValue(editor, arguments, startIndex + 1, resolver);
	}

	AstObjectInfo createDetailList(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			IModelResolver resolver) throws Exception {
		// prepare master
		ObservableInfo masterObservable = getMasterObservable(editor, resolver, arguments[startIndex]);
		if (masterObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.EmfObserveTypeContainer_masterObservableArgumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare detail property
		addEMFPackage(arguments[startIndex + 1]);
		String detailPropertyReference = CoreUtils.getNodeReference(arguments[startIndex + 1]);
		// create observable
		DetailListEmfObservableInfo observeDetailList =
				new DetailListEmfObservableInfo(masterObservable, m_propertiesSupport);
		observeDetailList.setDetailPropertyReference(null, detailPropertyReference);
		observeDetailList.setCodeSupport(new EmfObservableDetailListCodeSupport());
		//
		return observeDetailList;
	}

	AstObjectInfo createDetailListEdit(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			IModelResolver resolver) throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createDetailList(editor, arguments, startIndex + 1, resolver);
	}

	AstObjectInfo createMaps(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			IModelResolver resolver) throws Exception {
		// prepare domain
		ObservableInfo domainObservable = (ObservableInfo) resolver.getModel(arguments[startIndex]);
		if (domainObservable == null) {
			AbstractParser.addError(editor, MessageFormat.format(
					Messages.EmfObserveTypeContainer_argumentNotFound,
					arguments[startIndex]), new Throwable());
			return null;
		}
		// prepare reference properties
		Assert.instanceOf(ArrayCreation.class, arguments[startIndex + 1]);
		ArrayCreation arrayReferenceProperties = (ArrayCreation) arguments[startIndex + 1];
		List<Expression> expressionReferenceProperties =
				DomGenerics.expressions(arrayReferenceProperties.getInitializer());
		List<String> referenceProperties = Lists.newArrayList();
		for (Expression expression : expressionReferenceProperties) {
			Assert.instanceOf(QualifiedName.class, expression);
			referenceProperties.add(CoreUtils.getNodeReference(expression));
		}
		if (!expressionReferenceProperties.isEmpty()) {
			addEMFPackage(expressionReferenceProperties.get(0));
		}
		// create observable
		MapsEmfObservableInfo observeMaps =
				new MapsEmfObservableInfo(domainObservable, m_propertiesSupport);
		observeMaps.setEMFProperties(referenceProperties);
		return observeMaps;
	}

	AstObjectInfo createMapsEdit(AstEditor editor,
			Expression[] arguments,
			int startIndex,
			IModelResolver resolver) throws Exception {
		m_propertiesSupport.checkEditingDomain(arguments[startIndex]);
		return createMaps(editor, arguments, startIndex + 1, resolver);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Helper method for load classes over editor class loader.
	 */
	private Class<?> loadClass(String className) throws Exception {
		return CoreUtils.load(JavaInfoUtils.getClassLoader(m_javaInfoRoot), className);
	}

	/**
	 * @return {@link EObjectBindableInfo} that association with given {@link Expression}.
	 */
	public EObjectBindableInfo getEObject(Expression expression) throws Exception {
		// prepare reference
		String reference = CoreUtils.getNodeReference(expression);
		// find object
		for (EObjectBindableInfo bindableObject : m_observables) {
			EObjectBindableInfo result = (EObjectBindableInfo) bindableObject.resolveReference(reference);
			//
			if (result != null) {
				return result;
			}
		}
		//
		return null;
	}

	/**
	 * @return {@link EPropertyBindableInfo} property that association with given {@link Expression}.
	 */
	private EPropertyBindableInfo getEProperty(EObjectBindableInfo eObject, Expression expression)
			throws Exception {
		// prepare reference
		String reference = CoreUtils.getNodeReference(expression);
		// find property
		EPropertyBindableInfo eProperty = eObject.resolvePropertyReference(reference);
		//
		if (eProperty == null) {
			eProperty = new EPropertyBindableInfo(null, null, null, "", "");
		}
		//
		return eProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Classpath
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean ensureDBLibraries(IJavaProject javaProject) throws Exception {
		String testClass;
		String pluginName;
		{
			// CASE-385522 do not add dependences if no bindings exists
			int bindingsCount = 0;
			for (IObserveInfo observeInfo : getObservables()) {
				if (observeInfo instanceof BindableInfo bindableInfo) {
					bindingsCount += bindableInfo.getBindings().size();
				}
			}
			if (bindingsCount == 0) {
				return false;
			}
		}
		if (m_propertiesSupport.isEditingDomainMode()) {
			testClass = "org.eclipse.emf.databinding.edit.EMFEditObservables";
			pluginName = "org.eclipse.emf.databinding.edit";
		} else {
			testClass = "org.eclipse.emf.databinding.EMFObservables";
			pluginName = "org.eclipse.emf.databinding";
		}
		return ensureDBLibraries(javaProject, testClass, pluginName);
	}

	public static boolean ensureDBLibraries(IJavaProject javaProject,
			String testClass,
			String pluginName) throws Exception {
		boolean addLibrary = !ProjectUtils.hasType(javaProject, testClass);
		if (addLibrary) {
			IProject project = javaProject.getProject();
			// check 'java project' or 'plugin project'
			if (project.hasNature("org.eclipse.pde.PluginNature")) {
				// add to plugin imports
				if (addLibrary) {
					PdeUtils.get(project).addPluginImport(pluginName);
				}
			} else {
				// add to project .classpath
				if (addLibrary) {
					ProjectUtils.addPluginLibraries(javaProject, pluginName);
				}
			}
		}
		return addLibrary;
	}

	@Override
	public void ensureDesignerResources(IJavaProject javaProject) throws Exception {
		String[] classes =
			{"", "EMFListenerSupport", "EMFTreeBeanAdvisor", "EMFTreeObservableLabelProvider"};
		if (m_propertiesSupport.isEditingDomainMode()) {
			classes[0] = "EMFEditBeansListObservableFactory";
		} else {
			classes[0] = "EMFBeansListObservableFactory";
		}
		for (int i = 0; i < classes.length; i++) {
			ProjectUtils.ensureResourceType(
					javaProject,
					Activator.getDefault().getBundle(),
					"org.eclipse.wb.rcp.databinding." + classes[i]);
		}
	}
}