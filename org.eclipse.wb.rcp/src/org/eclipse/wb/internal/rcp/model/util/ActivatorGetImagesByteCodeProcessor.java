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
package org.eclipse.wb.internal.rcp.model.util;

import org.eclipse.wb.internal.core.utils.asm.ToBytesClassAdapter;
import org.eclipse.wb.internal.core.utils.reflect.IByteCodeProcessor;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.WorkspacePluginInfo;

import org.eclipse.core.resources.IProject;

import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import java.io.InputStream;

/**
 * If subclass of {@link org.eclipse.ui.plugin.AbstractUIPlugin} is not runtime plugin (i.e. just
 * project in workspace), its method {@code ImageDescriptor getImageDescriptor(String path)} should
 * by intercepted and implemented to return appropriate ImageDescriptor.
 *
 * @author lobas_av
 * @coverage rcp.util
 */
public final class ActivatorGetImagesByteCodeProcessor implements IByteCodeProcessor {
  private String m_activatorClassName;
  private String m_activatorProjectPath;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IByteCodeProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initialize(ProjectClassLoader classLoader) {
    createInternalImageManager(classLoader);
    prepareActivatorInformation(classLoader.getJavaProject().getProject());
  }

  @Override
  public byte[] process(String className, byte[] bytes) {
    if (className.equals(m_activatorClassName)) {
      return transformActivatorClass(bytes);
    }
    return bytes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createInternalImageManager(ProjectClassLoader classLoader) {
    try {
      // prepare InternalImageManager bytes
      ClassLoader localClassLoader = getClass().getClassLoader();
      InputStream stream =
          localClassLoader.getResourceAsStream("org/eclipse/wb/internal/rcp/model/util/InternalImageManager.class");
      byte[] bytes = IOUtils.toByteArray(stream);
      stream.close();
      // inject InternalImageManager to project class loader
      ReflectUtils.defineClass(
          "org.eclipse.wb.internal.rcp.model.util.InternalImageManager",
          bytes,
          classLoader);
    } catch (Throwable e) {
    }
  }

  private void prepareActivatorInformation(IProject project) {
    try {
      m_activatorClassName = WorkspacePluginInfo.getBundleActivator(project);
      m_activatorProjectPath = project.getLocation().toPortableString();
    } catch (Throwable e) {
    }
  }

  private byte[] transformActivatorClass(byte[] bytes) {
    final boolean[] apply = {false};
    ClassReader classReader = new ClassReader(bytes);
    ToBytesClassAdapter codeRewriter = new ToBytesClassAdapter(ClassWriter.COMPUTE_MAXS) {
      @Override
      public MethodVisitor visitMethod(int access,
          String name,
          String desc,
          String signature,
          String[] exceptions) {
        final MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        final int pathParameterIndex = (access & ACC_STATIC) != 0 ? 0 : 1;
        // transform Activator.getImageDescriptor()
        if (name.equals("getImageDescriptor")
            && desc.equals("(Ljava/lang/String;)Lorg/eclipse/jface/resource/ImageDescriptor;")) {
          apply[0] = true;
          return new EmptyVisitor() {
            @Override
            public void visitCode() {
              mv.visitLdcInsn(m_activatorProjectPath);
              mv.visitVarInsn(ALOAD, pathParameterIndex);
              mv.visitMethodInsn(
                  INVOKESTATIC,
                  "org/eclipse/wb/internal/rcp/model/util/InternalImageManager",
                  "getImageDescriptor",
                  "(Ljava/lang/String;Ljava/lang/String;)Lorg/eclipse/jface/resource/ImageDescriptor;");
              mv.visitInsn(ARETURN);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
              mv.visitMaxs(maxStack, maxLocals);
            }
          };
        }
        // transform Activator.getImage()
        if (name.equals("getImage")
            && desc.equals("(Ljava/lang/String;)Lorg/eclipse/swt/graphics/Image;")) {
          apply[0] = true;
          return new EmptyVisitor() {
            @Override
            public void visitCode() {
              mv.visitLdcInsn(m_activatorProjectPath);
              mv.visitVarInsn(ALOAD, pathParameterIndex);
              mv.visitMethodInsn(
                  INVOKESTATIC,
                  "org/eclipse/wb/internal/rcp/model/util/InternalImageManager",
                  "getImage",
                  "(Ljava/lang/String;Ljava/lang/String;)Lorg/eclipse/swt/graphics/Image;");
              mv.visitInsn(ARETURN);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
              mv.visitMaxs(maxStack, maxLocals);
            }
          };
        }
        return mv;
      }
    };
    classReader.accept(codeRewriter, 0);
    return apply[0] ? codeRewriter.toByteArray() : bytes;
  }
}