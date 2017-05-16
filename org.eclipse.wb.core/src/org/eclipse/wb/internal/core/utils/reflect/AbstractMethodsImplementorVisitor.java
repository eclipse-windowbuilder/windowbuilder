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
package org.eclipse.wb.internal.core.utils.reflect;

import org.eclipse.wb.internal.core.utils.asm.ToBytesClassAdapter;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * {@link ClassAdapter} that "implements" abstract methods by replacing them with methods that throw
 * {@link Error}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class AbstractMethodsImplementorVisitor extends ToBytesClassAdapter {
  private final String m_className;
  private boolean m_interface;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractMethodsImplementorVisitor(String className) {
    super(ClassWriter.COMPUTE_MAXS);
    m_className = className;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class visitor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void visit(int version,
      int access,
      String name,
      String signature,
      String superName,
      String[] interfaces) {
    m_interface = (access & ACC_INTERFACE) != 0;
    if (!m_interface) {
      access &= ~ACC_ABSTRACT;
    }
    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public MethodVisitor visitMethod(int access,
      String name,
      String desc,
      String signature,
      String[] exceptions) {
    // replace abstract method with method that throws java.lang.Error
    if ((access & ACC_ABSTRACT) != 0) {
      if (!m_interface) {
        MethodVisitor mv =
            cv.visitMethod(access & ~ACC_ABSTRACT, name, desc, signature, exceptions);
        mv.visitCode();
        // throw exception
        if (!Type.getReturnType(desc).getClassName().equals("void")) {
          mv.visitTypeInsn(NEW, "java/lang/Error");
          mv.visitInsn(DUP);
          mv.visitLdcInsn("Designer implemented stub for abstract method "
              + m_className
              + "."
              + name
              + desc
              + " however you can not use this method (we just can not know what it should do).");
          mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Error", "<init>", "(Ljava/lang/String;)V");
          mv.visitInsn(ATHROW);
        } else {
          mv.visitInsn(RETURN);
        }
        // end
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return mv;
      }
    }
    // default handling
    return super.visitMethod(access, name, desc, signature, exceptions);
  }
}
