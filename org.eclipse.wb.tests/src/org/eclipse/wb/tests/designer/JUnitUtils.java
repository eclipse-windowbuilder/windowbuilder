/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Suite.SuiteClasses;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Utility class for checking e.g. the execution flow and inclusion of test
 * cases.
 */
public class JUnitUtils {
	private static Comparator<Class<?>> COMPARE_BY_NAME = Comparator.comparing(Class::getName);
	private static File TEST_DIR = new File("src");
	private static Set<Class<?>> TEST_ANNOTATIONS = Set.of( //
			Test.class, //
			Ignore.class, //
			Before.class, //
			After.class, //
			BeforeClass.class, //
			AfterClass.class);

	public static Set<Class<?>> getRepeatedlyExecutedClasses() {
		Map<Class<?>, Integer> counts = new HashMap<>();

		for (Class<?> clazz : getAllExecutedClasses(WindowBuilderTests.class)) {
			counts.compute(clazz, (k, v) -> v == null ? 1 : v + 1);
		}

		Set<Class<?>> result = new TreeSet<>(COMPARE_BY_NAME);

		counts.forEach((clazz, count) -> {
			if (count > 1) {
				result.add(clazz);
			}
		});

		return Collections.unmodifiableSet(result);
	}

	public static Map<Class<?>, List<Method>> getUnusedMethod() {
		Map<Class<?>, List<Method>> result = new TreeMap<>(COMPARE_BY_NAME);

		for (Class<?> clazz : getAllClasses(TEST_DIR)) {
			for (Method method : clazz.getMethods()) {
				if (!isTestMethod(method)) {
					result.computeIfAbsent(clazz, key -> new ArrayList<>()).add(method);
				}
			}
		}

		return Collections.unmodifiableMap(result);
	}

	public static Set<Class<?>> getUnusedClasses() {
		Set<Class<?>> allClasses = getAllClasses(TEST_DIR);
		List<Class<?>> allExecutedClasses = getAllExecutedClasses(WindowBuilderTests.class);
		SortedSet<Class<?>> result = new TreeSet<>(COMPARE_BY_NAME);

		result.addAll(allClasses);
		result.removeAll(allExecutedClasses);

		return Collections.unmodifiableSet(result);
	}

	// ########################################
	// ## Getting classes via Test Suite
	// ##

	private static List<Class<?>> getAllExecutedClasses(Class<?> clazz) {
		// A test can be executed more than once
		List<Class<?>> result = new ArrayList<>();
		result.add(clazz);

		SuiteClasses suiteClasses = clazz.getAnnotation(SuiteClasses.class);
		if (suiteClasses != null) {
			for (Class<?> suiteClass : suiteClasses.value()) {
				result.addAll(getAllExecutedClasses(suiteClass));
			}
		}

		return Collections.unmodifiableList(result);
	}

	// ########################################
	// ## Getting classes via I/O
	// ##

	private static Set<Class<?>> getAllClasses(File userDir) {
		SortedSet<Class<?>> result = new TreeSet<>(COMPARE_BY_NAME);

		for (File classFile : getAllClassesByFile(userDir)) {
			Path classPath = userDir.toPath().relativize(classFile.toPath());

			String className = classPath.toString()
					.replaceAll(File.separator, ".")
					.replaceAll(".java$", "");

			try {
				Class<?> clazz = JUnitUtils.class.getClassLoader().loadClass(className);

				// Only consider "real" classes
				if (clazz.isEnum() || clazz.isInterface() || clazz.isAnnotation()) {
					continue;
				}

				// Ignore abstract classes
				if ((clazz.getModifiers() & Modifier.ABSTRACT) > 0) {
					continue;
				}

				result.add(clazz);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return Collections.unmodifiableSet(result);
	}

	private static Set<File> getAllClassesByFile(File current) {
		SortedSet<File> result = new TreeSet<>();

		if (current.isDirectory()) {
			for (File next : current.listFiles()) {
				result.addAll(getAllClassesByFile(next));
			}
		} else if (current.isFile() && current.getName().endsWith(".java")) {
			result.add(current);
		}

		return Collections.unmodifiableSet(result);
	}

	// ########################################
	// ## Misc.
	// ##

	private static boolean isTestMethod(Method method) {
		for (Annotation annotation : method.getAnnotations()) {
			if (TEST_ANNOTATIONS.contains(annotation.annotationType())) {
				return true;
			}
		}
		// Quick & dirty solution to ignore irrelevant methods
		return method.getName().equals("_test_exit") || !method.getName().contains("test");
	}
}
