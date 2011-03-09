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
package org.eclipse.wb.internal.rcp.databinding.parser;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.asm.ToBytesClassAdapter;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.IByteCodeProcessor;
import org.eclipse.wb.internal.core.utils.reflect.IClassLoaderInitializer;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Initialize class loader for use bindings on runtime.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.parser
 */
public final class BindingContextClassLoaderInitializer implements IClassLoaderInitializer {
  private static final Map<ClassLoader, Object> CLASS_LOADER_TO_THREAD_LOCAL = Maps.newHashMap();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IClassLoaderInitializer INSTANCE = new BindingContextClassLoaderInitializer();

  private BindingContextClassLoaderInitializer() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClassLoaderInitializer
  //
  ////////////////////////////////////////////////////////////////////////////
  public void initialize(final ClassLoader classLoader) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        createDefaultBean(configureBindings(classLoader));
      }
    });
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        setDefaultRealm(classLoader);
      }
    });
  }

  public void deinitialize(final ClassLoader classLoader) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        Object threadLocal = CLASS_LOADER_TO_THREAD_LOCAL.remove(classLoader);
        ReflectionUtils.invokeMethod(threadLocal, "remove()");
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Realm
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void setDefaultRealm(ClassLoader classLoader) throws Exception {
    // prepare Display
    Class<?> displayClass = classLoader.loadClass("org.eclipse.swt.widgets.Display");
    Object display = ReflectionUtils.invokeMethod(displayClass, "getDefault()");
    // create Realm
    Class<?> swtObservables =
        classLoader.loadClass("org.eclipse.jface.databinding.swt.SWTObservables");
    Object realm =
        ReflectionUtils.invokeMethod(
            swtObservables,
            "getRealm(org.eclipse.swt.widgets.Display)",
            display);
    // set default Realm
    Class<?> realmClass = classLoader.loadClass("org.eclipse.core.databinding.observable.Realm");
    ReflectionUtils.invokeMethod(
        realmClass,
        "setDefault(org.eclipse.core.databinding.observable.Realm)",
        realm);
    CLASS_LOADER_TO_THREAD_LOCAL.put(
        classLoader,
        ReflectionUtils.getFieldObject(realmClass, "defaultRealm"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bean/Observable
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createDefaultBean(ProjectClassLoader projectClassLoader) throws Exception {
    // prepare DefaultBean bytes
    ClassLoader localClassLoader = getClass().getClassLoader();
    InputStream stream =
        localClassLoader.getResourceAsStream("org/eclipse/wb/internal/rcp/databinding/parser/DefaultBean.class");
    byte[] bytes = IOUtils.toByteArray(stream);
    stream.close();
    // inject DefaultBean to project class loader
    ReflectUtils.defineClass(
        "org.eclipse.wb.internal.rcp.databinding.parser.DefaultBean",
        bytes,
        projectClassLoader);
  }

  private static ProjectClassLoader configureBindings(ClassLoader classLoader) {
    // prepare project class loader
    CompositeClassLoader compositeClassLoader = (CompositeClassLoader) classLoader;
    List<?> classLoaders = compositeClassLoader.getClassLoaders();
    ProjectClassLoader projectClassLoader =
        (ProjectClassLoader) classLoaders.get(classLoaders.size() - 1);
    // add bytecode processor
    projectClassLoader.add(new IByteCodeProcessor() {
      public void initialize(ProjectClassLoader classLoader) {
      }

      public byte[] process(String className, byte[] bytes) {
        if ("org.eclipse.core.databinding.beans.BeansObservables".equals(className)) {
          return transformBindings(bytes);
        }
        if ("org.eclipse.core.databinding.observable.list.ObservableList".equals(className)) {
          return transformObserves(
              bytes,
              "(Lorg/eclipse/core/databinding/observable/Realm;Ljava/util/List;Ljava/lang/Object;)V",
              "EMPTY_LIST",
              "Ljava/util/List;");
        }
        if ("org.eclipse.core.databinding.observable.set.ObservableSet".equals(className)) {
          return transformObserves(
              bytes,
              "(Lorg/eclipse/core/databinding/observable/Realm;Ljava/util/Set;Ljava/lang/Object;)V",
              "EMPTY_SET",
              "Ljava/util/Set;");
        }
        return bytes;
      }
    });
    return projectClassLoader;
  }

  /**
   * Inject to all public static methods contains parameters {@code Object} bean and {@code String}
   * property:
   * 
   * <pre>
	 * public static %ReturnType% %method%(..., Object bean, ..., String property, ....) {
	 *     if (bean == null) {
	 *         bean = DefaultBean.INSTANCE;
	 *         property = "foo"; // "fooList"; // "fooSet";
	 *     }
	 *     %FOO_BODY_PART%
	 * }
	 * </pre>
   */
  private static byte[] transformBindings(byte[] bytes) {
    ClassReader classReader = new ClassReader(bytes);
    ToBytesClassAdapter codeRewriter = new ToBytesClassAdapter(ClassWriter.COMPUTE_MAXS) {
      @Override
      public MethodVisitor visitMethod(int access,
          String name,
          String desc,
          String signature,
          String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if ((access & ACC_PUBLIC) != 0 && (access & ACC_STATIC) != 0) {
          // find indexes for bean object argument and bean property argument
          final int[] indexes = {-1, -1};
          String[] parameters =
              StringUtils.split(StringUtils.substringBetween(desc, "(", ")"), ";");
          for (int i = 0; i < parameters.length; i++) {
            String parameter = parameters[i];
            if (indexes[0] == -1 && parameter.equals("Ljava/lang/Object")) {
              indexes[0] = i;
            } else if (indexes[0] != -1
                && indexes[1] == -1
                && parameter.equals("Ljava/lang/String")) {
              indexes[1] = i;
            }
          }
          if (indexes[0] != -1 && indexes[1] != -1) {
            // prepare bean property name
            final String[] property = {"foo"};
            if (name.endsWith("List")) {
              property[0] = "fooList";
            } else if (name.endsWith("Set")) {
              property[0] = "fooSet";
            }
            return new MethodAdapter(mv) {
              @Override
              public void visitCode() {
                visitVarInsn(ALOAD, indexes[0]);
                Label label = new Label();
                visitJumpInsn(IFNONNULL, label);
                visitFieldInsn(
                    GETSTATIC,
                    "org/eclipse/wb/internal/rcp/databinding/parser/DefaultBean",
                    "INSTANCE",
                    "Lorg/eclipse/wb/internal/rcp/databinding/parser/DefaultBean;");
                visitVarInsn(ASTORE, indexes[0]);
                visitLdcInsn(property[0]);
                visitVarInsn(ASTORE, indexes[1]);
                visitLabel(label);
                super.visitCode();
              }
            };
          }
        }
        return mv;
      }
    };
    classReader.accept(codeRewriter, 0);
    return codeRewriter.toByteArray();
  }

  /**
   * Inject to constructor with {@code methodDescriptor} signature additional code:
   * 
   * <pre>
	 * constructor(Realm, %Type% argument, Object) {
	 *    if (argument == null) {
	 *        argument = Collections.EMPTY_%field%;
	 *    }
	 *    %FOO_BODY_PART%
	 * }
	 * </pre>
   */
  private static byte[] transformObserves(byte[] bytes,
      final String methodDescriptor,
      final String field,
      final String fieldType) {
    ClassReader classReader = new ClassReader(bytes);
    ToBytesClassAdapter codeRewriter = new ToBytesClassAdapter(ClassWriter.COMPUTE_MAXS) {
      @Override
      public MethodVisitor visitMethod(int access,
          String name,
          String desc,
          String signature,
          String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>") && desc.equals(methodDescriptor)) {
          return new MethodAdapter(mv) {
            @Override
            public void visitCode() {
              visitVarInsn(ALOAD, 2);
              Label label = new Label();
              visitJumpInsn(IFNONNULL, label);
              visitFieldInsn(GETSTATIC, "java/util/Collections", field, fieldType);
              visitVarInsn(ASTORE, 2);
              visitLabel(label);
              super.visitCode();
            }
          };
        }
        return mv;
      }
    };
    classReader.accept(codeRewriter, 0);
    return codeRewriter.toByteArray();
  }
}