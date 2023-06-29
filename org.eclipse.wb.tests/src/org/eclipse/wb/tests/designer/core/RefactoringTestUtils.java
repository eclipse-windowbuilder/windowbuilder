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
package org.eclipse.wb.tests.designer.core;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.DeleteDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import java.util.Map;

/**
 * Utils for testing/using refactorings.
 *
 * @author scheglov_ke
 */
public final class RefactoringTestUtils {
	private static final String ATTRIBUTE_RESOURCES = "resources";
	private static final String ATTRIBUTE_ELEMENTS = "elements";
	private static final String ATTRIBUTE_SUGGEST_ACCESSORS = "accessors";
	private static final String ATTRIBUTE_DELETE_SUBPACKAGES = "subPackages";

	/**
	 * Renames {@link IType}.
	 */
	public static void renameType(IType typeToRename, String newName) throws Exception {
		RenameSupport renameSupport =
				RenameSupport.create(typeToRename, newName, RenameSupport.UPDATE_REFERENCES);
		renameSupport.preCheck();
		renameSupport.perform(DesignerPlugin.getShell(), DesignerPlugin.getActiveWorkbenchWindow());
	}

	/**
	 * Renames {@link IMethod}.
	 */
	public static void renameMethod(IMethod methodToRename, String newName) throws Exception {
		RenameSupport renameSupport =
				RenameSupport.create(methodToRename, newName, RenameSupport.UPDATE_REFERENCES);
		renameSupport.preCheck();
		renameSupport.perform(DesignerPlugin.getShell(), DesignerPlugin.getActiveWorkbenchWindow());
	}

	/**
	 * Moves enclosing {@link ICompilationUnit} into new package.
	 */
	public static void moveType(IType type, IPackageFragment newPackage) throws Exception {
		moveCompilationUnit(type.getCompilationUnit(), newPackage);
	}

	/**
	 * Moves {@link ICompilationUnit} into new package.
	 */
	public static void moveCompilationUnit(ICompilationUnit compilationUnit,
			IPackageFragment newPackage) throws Exception {
		// prepare RefactoringDescriptor
		MoveDescriptor refactoringDescriptor;
		{
			refactoringDescriptor =
					(MoveDescriptor) RefactoringCore.getRefactoringContribution(IJavaRefactorings.MOVE).createDescriptor();
			refactoringDescriptor.setMoveResources(
					new IFile[]{},
					new IFolder[]{},
					new ICompilationUnit[]{compilationUnit});
			refactoringDescriptor.setDestination(newPackage);
			refactoringDescriptor.setUpdateReferences(true);
		}
		// perform refactoring
		performRefactoring(refactoringDescriptor);
	}

	/**
	 * Deletes enclosing {@link ICompilationUnit}.
	 */
	public static void deleteType(IType type) throws Exception {
		deleteCompilationUnit(type.getCompilationUnit());
	}

	/**
	 * Deletes {@link ICompilationUnit}.
	 */
	public static void deleteCompilationUnit(ICompilationUnit compilationUnit) throws Exception {
		// prepare RefactoringDescriptor
		DeleteDescriptor refactoringDescriptor;
		{
			Map<String, String> arguments = Maps.newHashMap();
			arguments.put(ATTRIBUTE_DELETE_SUBPACKAGES, "false");
			arguments.put(ATTRIBUTE_SUGGEST_ACCESSORS, "false");
			arguments.put(ATTRIBUTE_RESOURCES, "0");
			arguments.put(ATTRIBUTE_ELEMENTS, "1");
			arguments.put("element1", compilationUnit.getHandleIdentifier());
			refactoringDescriptor =
					(DeleteDescriptor) RefactoringCore.getRefactoringContribution(IJavaRefactorings.DELETE).createDescriptor(
							IJavaRefactorings.DELETE,
							compilationUnit.getJavaProject().getElementName(),
							"Delete " + compilationUnit.getElementName(),
							"",
							arguments,
							0);
		}
		// perform refactoring
		performRefactoring(refactoringDescriptor);
	}

	/**
	 * Performs refactoring corresponding to given {@link RefactoringDescriptor}.
	 */
	public static void performRefactoring(RefactoringDescriptor refactoringDescriptor)
			throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		// perform refactoring
		RefactoringStatus refactoringStatus = new RefactoringStatus();
		Refactoring refactoring = refactoringDescriptor.createRefactoring(refactoringStatus);
		Assert.isTrue(!refactoringStatus.hasError(), refactoringStatus.toString());
		// check conditions
		refactoring.checkAllConditions(monitor);
		Assert.isTrue(!refactoringStatus.hasError(), refactoringStatus.toString());
		// apply change
		Change change = refactoring.createChange(monitor);
		change.perform(monitor);
	}
}