/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ClassLoaderLocalMap;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.mvel2.optimizers.OptimizerFactory;
import org.mvel2.optimizers.dynamic.DynamicClassLoader;
import org.mvel2.optimizers.impl.asm.ASMAccessorOptimizer;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utils for using MVEL.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ScriptUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// With JavaInfo context
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Evaluates given script.
	 */
	public static Object evaluate(ClassLoader contextClassLoader, String script) {
		Map<String, Object> variables = Collections.emptyMap();
		return evaluate(contextClassLoader, script, variables);
	}

	/**
	 * Evaluates given script, with one variable.
	 */
	public static Object evaluate(ClassLoader contextClassLoader,
			String script,
			String name_1,
			Object value_1) {
		Map<String, Object> variables = new HashMap<>();
		variables.put(name_1, value_1);
		return evaluate(contextClassLoader, script, variables);
	}

	/**
	 * Evaluates given script, with two variables.
	 */
	public static Object evaluate(ClassLoader contextClassLoader,
			String script,
			String name_1,
			Object value_1,
			String name_2,
			Object value_2) {
		Map<String, Object> variables = new HashMap<>();
		variables.put(name_1, value_1);
		variables.put(name_2, value_2);
		return evaluate(contextClassLoader, script, variables);
	}

	/**
	 * Evaluates given script with variables.
	 */
	public static Object evaluate(ClassLoader contextClassLoader,
			String script,
			Map<String, Object> variables) {
		Map<String, Object> contextCache = getContextCache(contextClassLoader);
		try {
			Object expression = compile(contextCache, script, contextClassLoader);
			return evaluate(expression, variables);
		} finally {
			clearMemoryLeaks();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Simple
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Evaluates given script.
	 */
	public static Object evaluate(String script) {
		return evaluate(script, new HashMap<>());
	}

	/**
	 * Evaluates simple script on given context value.
	 */
	public static Object evaluate(String script, Object ctx) {
		Object expression = compile(script);
		return MVEL.executeExpression(expression, ctx);
	}

	/**
	 * Evaluates given script with single variable.
	 */
	public static Object evaluate(String script, String name_1, Object value_1) {
		Object expression = compile(script);
		Map<String, Object> variables = new HashMap<>();
		variables.put(name_1, value_1);
		return evaluate(expression, variables);
	}

	/**
	 * Evaluates given script with two variables.
	 */
	public static Object evaluate(String script,
			String name_1,
			Object value_1,
			String name_2,
			Object value_2) {
		Object expression = compile(script);
		Map<String, Object> variables = new HashMap<>();
		variables.put(name_1, value_1);
		variables.put(name_2, value_2);
		return evaluate(expression, variables);
	}

	/**
	 * Evaluates given script with variables.
	 */
	public static Object evaluate(String script, Map<String, Object> variables) {
		Object expression = compile(script);
		return evaluate(expression, variables);
	}

	private static Object evaluate(Object expression, Map<String, Object> _variables) {
		Map<String, Object> variables = new HashMap<>(_variables);
		return MVEL.executeExpression(expression, variables);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * MVEL uses <code>WeakHashMap</code> and can cause temporary memory leaks. To solve it we should
	 * call <code>size()</code> periodically to force <code>expungeStaleEntries()</code>, or just
	 * <code>clear()</code> after each use.
	 * <p>
	 * MVEL uses {@link ThreadLocal}, but sometimes "forgets" to clear it.
	 * <p>
	 * http://jira.codehaus.org/browse/MVEL-149
	 * <p>
	 * http://jira.codehaus.org/browse/MVEL-150
	 */
	public static void clearMemoryLeaks() {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				clearCaches(org.mvel2.util.ParseTools.class);
				clearCaches(org.mvel2.PropertyAccessor.class);
				if (ASMAccessorOptimizer.getMVELClassLoader() instanceof DynamicClassLoader) {
					OptimizerFactory.setDefaultOptimizer(OptimizerFactory.DYNAMIC);
				}
			}

			private void clearCaches(Class<?> clazz) throws Exception {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					if (field.getName().endsWith("_CACHE")) {
						if (Map.class.isAssignableFrom(field.getType())) {
							field.setAccessible(true);
							Map<?, ?> map = (Map<?, ?>) field.get(0);
							if (map != null) {
								map.clear();
							}
						}
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Compilation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Map<String, Object> m_compiledExpressions =
			new ReferenceMap<>(ReferenceStrength.HARD, ReferenceStrength.WEAK);

	/**
	 * @return the weak cache for given {@link ClassLoader}.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> getContextCache(ClassLoader context) {
		Class<ScriptUtils> key = ScriptUtils.class;
		Map<String, Object> cache = (Map<String, Object>) ClassLoaderLocalMap.get(context, key);
		if (cache == null) {
			cache = new HashMap<>();
			ClassLoaderLocalMap.put(context, key, cache);
		}
		return cache;
	}

	/**
	 * @return the compiled expression, with standard WindowBuilder functions and imports.
	 */
	private static Object compile(String script) {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		return compile(m_compiledExpressions, script, contextClassLoader);
	}

	private static Object compile(Map<String, Object> cache,
			String script,
			ClassLoader contextClassLoader) {
		Object expression = cache.get(script);
		if (expression == null) {
			ParserConfiguration parserConfiguration = new ParserConfiguration();
			parserConfiguration.setClassLoader(contextClassLoader);
			ParserContext context = new ParserContext(parserConfiguration);
			context.addImport("ReflectionUtils", ReflectionUtils.class);
			expression = MVEL.compileExpression(script, context);
			cache.put(script, expression);
		}
		return expression;
	}
}
