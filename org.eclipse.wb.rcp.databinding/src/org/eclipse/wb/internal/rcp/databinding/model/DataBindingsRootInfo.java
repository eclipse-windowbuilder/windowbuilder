/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.parser.ISubParser;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.ConverterInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.StrategyModelSupport;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateListStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateSetStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateValueStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.ValidatorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manage JFace binding source code (add Realm to main(), add invocation
 * initDataBindings(), configure classpath and etc.) for compilation unit.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public final class DataBindingsRootInfo implements ISubParser {
	public static final String INIT_DATA_BINDINGS_METHOD_NAME = "initDataBindings";
	public static final String[] ACCESS_VALUES = {"public ", "protected ", "private ", ""};
	private MethodDeclaration m_initDataBindings;
	private final DataBindingContextInfo m_contextInfo = new DataBindingContextInfo();

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public MethodDeclaration getInitDataBindings() {
		return m_initDataBindings;
	}

	public void setInitDataBindings(MethodDeclaration initDataBindings) {
		m_initDataBindings = initDataBindings;
	}

	public DataBindingContextInfo getContextInfo() {
		return m_contextInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
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
		// context
		if ("org.eclipse.core.databinding.DataBindingContext.<init>()".equals(signature)) {
			return m_contextInfo;
		}
		// value strategy
		if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.core.databinding.UpdateValueStrategy")) {
			UpdateValueStrategyInfo strategy = new UpdateValueStrategyInfo(creation, arguments);
			resolver.addModelSupport(new StrategyModelSupport(strategy, creation));
			return null;
		}
		// list strategy
		if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.core.databinding.UpdateListStrategy")) {
			UpdateListStrategyInfo strategy = new UpdateListStrategyInfo(creation, arguments);
			resolver.addModelSupport(new StrategyModelSupport(strategy, creation));
			return null;
		}
		// set strategy
		if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.core.databinding.UpdateSetStrategy")) {
			UpdateSetStrategyInfo strategy = new UpdateSetStrategyInfo(creation, arguments);
			resolver.addModelSupport(new StrategyModelSupport(strategy, creation));
			return null;
		}
		// validator
		if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.core.databinding.validation.IValidator")) {
			return new ValidatorInfo(editor, creation);
		}
		// converter
		if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.core.databinding.conversion.IConverter")) {
			return new ConverterInfo(editor, creation);
		}
		//
		return null;
	}

	@Override
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver) throws Exception {
		if (signature.endsWith("initializeContext(org.eclipse.core.databinding.DataBindingContext)")
				&& AstNodeUtils.getLocalMethodDeclaration(invocation) != null) {
			Assert.isNotNull(m_contextInfo);
			m_contextInfo.addInitializeContext(true);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Save model changes to source code.
	 */
	public boolean commit(AstEditor editor,
			TypeDeclaration typeDeclaration,
			JavaInfo rootJavaInfo,
			List<ObserveTypeContainer> containers,
			boolean controller) throws Exception {
		IJavaProject javaProject = editor.getJavaProject();
		CodeGenerationSupport generationSupport = new CodeGenerationSupport(false, m_contextInfo);
		//
		boolean reparse = DataBindingsCodeUtils.ensureDBLibraries(javaProject);
		for (ObserveTypeContainer container : containers) {
			reparse |= container.ensureDBLibraries(javaProject);
		}
		//
		if (ensureDesignerResources()) {
			DataBindingsCodeUtils.ensureDesignerResources(javaProject);
			for (ObserveTypeContainer container : containers) {
				container.ensureDesignerResources(javaProject);
			}
		}
		// remove old method
		if (m_initDataBindings != null) {
			editor.removeBodyDeclaration(m_initDataBindings);
		}
		// prepare source code
		List<String> methodLines = new ArrayList<>();
		m_contextInfo.addSourceCode(editor, methodLines, generationSupport);
		//
		BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, null, false);
		//
		MethodDeclaration lastInfoMethod =
				controller
				? AstNodeUtils.getConstructors(typeDeclaration).get(0)
						: DataBindingsCodeUtils.getLastInfoDeclaration(m_initDataBindings, rootJavaInfo);
		// create new method
		m_initDataBindings =
				editor.addMethodDeclaration(createMethodHeader(lastInfoMethod), methodLines, target);
		// check call initDataBindings() after creation all widgets
		DataBindingsCodeUtils.ensureInvokeInitDataBindings(
				controller ? null : rootJavaInfo,
						editor,
						typeDeclaration,
						lastInfoMethod);
		// check work application main method over Realm
		DataBindingsCodeUtils.ensureEnclosingRealmOfMain(rootJavaInfo.getEditor());
		//
		if (controller) {
			ControllerSupport.doSave(editor, rootJavaInfo);
		}
		//
		return reparse;
	}

	private boolean ensureDesignerResources() {
		for (AbstractBindingInfo binding : m_contextInfo.getBindings()) {
			if (binding instanceof TreeViewerInputBindingInfo treeBinding) {
				if (treeBinding.isDesignerMode()) {
					return true;
				}
			}
		}
		return false;
	}

	private static String createMethodHeader(MethodDeclaration lastInfoMethod) throws Exception {
		int access = Activator.getStore().getInt(IPreferenceConstants.INITDB_GENERATE_ACCESS);
		// check static
		if (Modifier.isStatic(lastInfoMethod.getModifiers())) {
			return ACCESS_VALUES[access]
					+ "static org.eclipse.core.databinding.DataBindingContext "
					+ INIT_DATA_BINDINGS_METHOD_NAME
					+ "()";
		}
		// normal
		return ACCESS_VALUES[access]
				+ "org.eclipse.core.databinding.DataBindingContext "
				+ INIT_DATA_BINDINGS_METHOD_NAME
				+ "()";
	}
}