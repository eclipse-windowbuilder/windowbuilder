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
package org.eclipse.wb.internal.rcp.databinding.model.context;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateListStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateSetStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateValueStrategyInfo;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for bindings. This class manage JFace binding source code for method
 * <code>initDataBindings()</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public final class DataBindingContextInfo extends AstObjectInfo {
	private static final String BIND_VALUE_METHOD_1 =
			"org.eclipse.core.databinding.DataBindingContext.bindValue(org.eclipse.core.databinding.observable.value.IObservableValue,org.eclipse.core.databinding.observable.value.IObservableValue,org.eclipse.core.databinding.UpdateValueStrategy,org.eclipse.core.databinding.UpdateValueStrategy)";
	private static final String BIND_VALUE_METHOD_2 =
			"org.eclipse.core.databinding.DataBindingContext.bindValue(org.eclipse.core.databinding.observable.value.IObservableValue,org.eclipse.core.databinding.observable.value.IObservableValue)";
	private static final String BIND_LIST_METHOD_1 =
			"org.eclipse.core.databinding.DataBindingContext.bindList(org.eclipse.core.databinding.observable.list.IObservableList,org.eclipse.core.databinding.observable.list.IObservableList,org.eclipse.core.databinding.UpdateListStrategy,org.eclipse.core.databinding.UpdateListStrategy)";
	private static final String BIND_LIST_METHOD_2 =
			"org.eclipse.core.databinding.DataBindingContext.bindList(org.eclipse.core.databinding.observable.list.IObservableList,org.eclipse.core.databinding.observable.list.IObservableList)";
	private static final String BIND_SET_METHOD_1 =
			"org.eclipse.core.databinding.DataBindingContext.bindSet(org.eclipse.core.databinding.observable.set.IObservableSet,org.eclipse.core.databinding.observable.set.IObservableSet,org.eclipse.core.databinding.UpdateSetStrategy,org.eclipse.core.databinding.UpdateSetStrategy)";
	private static final String BIND_SET_METHOD_2 =
			"org.eclipse.core.databinding.DataBindingContext.bindSet(org.eclipse.core.databinding.observable.set.IObservableSet,org.eclipse.core.databinding.observable.set.IObservableSet)";
	//
	private final List<AbstractBindingInfo> m_bindings = new ArrayList<>();
	private boolean m_addInitializeContext;
	private String m_userTryCatchBlock;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<AbstractBindingInfo> getBindings() {
		return m_bindings;
	}

	public boolean isAddInitializeContext() {
		return m_addInitializeContext;
	}

	public void addInitializeContext(boolean addInitializeContext) {
		m_addInitializeContext = addInitializeContext;
	}

	public String getUserTryCatchBlock() {
		return m_userTryCatchBlock;
	}

	public void setUserTryCatchBlock(String userTryCatchBlock) {
		m_userTryCatchBlock = userTryCatchBlock;
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
		// DataBindingContext.bindValue(IObservableValue, IObservableValue)
		// DataBindingContext.bindValue(IObservableValue, IObservableValue, UpdateValueStrategy, UpdateValueStrategy)
		//
		if (BIND_VALUE_METHOD_1.equals(signature) || BIND_VALUE_METHOD_2.equals(signature)) {
			// prepare target
			ObservableInfo target = (ObservableInfo) resolver.getModel(arguments[0]);
			if (target == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.DataBindingContextInfo_targetArgumentNotFound,
						arguments[0]), new Throwable());
				return null;
			}
			// prepare model
			ObservableInfo model = (ObservableInfo) resolver.getModel(arguments[1]);
			if (model == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.DataBindingContextInfo_modelArgumentNotFound,
						arguments[1]), new Throwable());
				return null;
			}
			// prepare target strategy
			UpdateValueStrategyInfo targetStrategy = null;
			if (arguments.length == 4) {
				targetStrategy = (UpdateValueStrategyInfo) resolver.getModel(arguments[2]);
				checkStrategy(editor, targetStrategy, arguments[2]);
			}
			// prepare model strategy
			UpdateValueStrategyInfo modelStrategy = null;
			if (arguments.length == 4) {
				modelStrategy = (UpdateValueStrategyInfo) resolver.getModel(arguments[3]);
				checkStrategy(editor, modelStrategy, arguments[3]);
			}
			// create binding
			BindingInfo binding = new ValueBindingInfo(target, model, targetStrategy, modelStrategy);
			m_bindings.add(binding);
			//
			return binding;
		}
		//
		// DataBindingContext.bindList(IObservableList, IObservableList)
		// DataBindingContext.bindList(IObservableList, IObservableList, UpdateListStrategy, UpdateListStrategy)
		//
		if (BIND_LIST_METHOD_1.equals(signature) || BIND_LIST_METHOD_2.equals(signature)) {
			// prepare target
			ObservableInfo target = (ObservableInfo) resolver.getModel(arguments[0]);
			if (target == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.DataBindingContextInfo_targetArgumentNotFound,
						arguments[0]), new Throwable());
				return null;
			}
			// prepare model
			ObservableInfo model = (ObservableInfo) resolver.getModel(arguments[1]);
			if (model == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.DataBindingContextInfo_modelArgumentNotFound,
						arguments[1]), new Throwable());
				return null;
			}
			// prepare target strategy
			UpdateListStrategyInfo targetStrategy = null;
			if (arguments.length == 4) {
				targetStrategy = (UpdateListStrategyInfo) resolver.getModel(arguments[2]);
				checkStrategy(editor, targetStrategy, arguments[2]);
			}
			// prepare model strategy
			UpdateListStrategyInfo modelStrategy = null;
			if (arguments.length == 4) {
				modelStrategy = (UpdateListStrategyInfo) resolver.getModel(arguments[3]);
				checkStrategy(editor, modelStrategy, arguments[3]);
			}
			// create binding
			BindingInfo binding = new ListBindingInfo(target, model, targetStrategy, modelStrategy);
			m_bindings.add(binding);
			//
			return binding;
		}
		//
		// DataBindingContext.bindSet(IObservableSet, IObservableSet)
		// DataBindingContext.bindSet(IObservableSet, IObservableSet, UpdateSetStrategy, UpdateSetStrategy)
		//
		if (BIND_SET_METHOD_1.equals(signature) || BIND_SET_METHOD_2.equals(signature)) {
			// prepare target
			ObservableInfo target = (ObservableInfo) resolver.getModel(arguments[0]);
			if (target == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.DataBindingContextInfo_targetArgumentNotFound,
						arguments[0]), new Throwable());
				return null;
			}
			// prepare model
			ObservableInfo model = (ObservableInfo) resolver.getModel(arguments[1]);
			if (model == null) {
				AbstractParser.addError(editor, MessageFormat.format(
						Messages.DataBindingContextInfo_modelArgumentNotFound,
						arguments[1]), new Throwable());
				return null;
			}
			// prepare target strategy
			UpdateSetStrategyInfo targetStrategy = null;
			if (arguments.length == 4) {
				targetStrategy = (UpdateSetStrategyInfo) resolver.getModel(arguments[2]);
				checkStrategy(editor, targetStrategy, arguments[2]);
			}
			// prepare model strategy
			UpdateSetStrategyInfo modelStrategy = null;
			if (arguments.length == 4) {
				modelStrategy = (UpdateSetStrategyInfo) resolver.getModel(arguments[3]);
				checkStrategy(editor, modelStrategy, arguments[3]);
			}
			// create binding
			BindingInfo binding = new SetBindingInfo(target, model, targetStrategy, modelStrategy);
			m_bindings.add(binding);
			//
			return binding;
		}
		return null;
	}

	private static void checkStrategy(AstEditor editor, Object strategy, Expression expression) {
		if (strategy == null) {
			if (expression instanceof NullLiteral) {
			} else {
				AbstractParser.addError(
						editor,
						MessageFormat.format(Messages.DataBindingContextInfo_undefinedStrategy, expression),
						new Throwable());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	public void addSourceCode(AstEditor editor,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		// sets variable
		if (getVariableIdentifier() == null) {
			setVariableIdentifier("bindingContext");
		}
		// create content
		lines.add("org.eclipse.core.databinding.DataBindingContext "
				+ getVariableIdentifier()
				+ " = new org.eclipse.core.databinding.DataBindingContext();");
		// try {
		boolean addTryCatch = Activator.getStore().getBoolean(IPreferenceConstants.INITDB_TRY_CATCH);
		if (m_userTryCatchBlock != null || addTryCatch) {
			lines.add("//");
			lines.add("try {");
		}
		// check add user initialize method
		if (m_addInitializeContext) {
			lines.add("initializeContext(" + getVariableIdentifier() + ");");
		}
		boolean addLineComment = m_addInitializeContext || m_userTryCatchBlock == null && !addTryCatch;
		// add bindings
		for (AbstractBindingInfo binding : m_bindings) {
			if (addLineComment) {
				lines.add("//");
			}
			addLineComment = true;
			binding.addSourceCode(this, lines, generationSupport);
		}
		// } catch () {}
		if (m_userTryCatchBlock != null || addTryCatch) {
			String indent = editor.getGeneration().getIndentation(1);
			int count = lines.size();
			for (int i = 3; i < count; i++) {
				lines.set(i, indent + lines.get(i));
			}
		}
		if (m_userTryCatchBlock != null) {
			lines.add(m_userTryCatchBlock);
		} else if (addTryCatch) {
			lines.add("} catch(Throwable e) {}");
		}
		// add return
		lines.add("//");
		lines.add("return " + getVariableIdentifier() + ";");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void accept(AstObjectInfoVisitor visitor) throws Exception {
		super.accept(visitor);
		// visit to bindings
		for (AbstractBindingInfo binding : m_bindings) {
			binding.accept(visitor);
		}
	}
}