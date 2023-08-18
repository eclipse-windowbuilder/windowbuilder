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
package org.eclipse.wb.internal.dev.builder;

import org.eclipse.wb.internal.dev.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler that updates last modification time for for resources in <code>wbp-meta</code>.
 *
 * @author scheglov_ke
 */
public final class MetaDataModificationHandler implements BuilderHandler {
	////////////////////////////////////////////////////////////////////////////
	//
	// Builder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		final AtomicBoolean done = new AtomicBoolean();
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				if (!done.get()) {
					IResource resource = delta.getResource();
					boolean processed = processResource(resource);
					done.set(processed);
				}
				return true;
			}
		});
	}

	@Override
	public void fullBuild(IProject project, IProgressMonitor monitor) throws CoreException {
		final AtomicBoolean done = new AtomicBoolean();
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (!done.get()) {
					boolean processed = processResource(resource);
					done.set(processed);
				}
				return true;
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IResource processing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link IResource} was interesting and caused check sum
	 *         calculation for full {@link IProject}.
	 */
	private static boolean processResource(IResource resource) {
		if (isInterestingResource(resource)) {
			IFile presentationsFile =
					resource.getProject().getFile(new Path("wbp-meta/.wbp-cache-presentations"));
			if (presentationsFile.exists()) {
				try {
					String oldCheckSum = getCheckSum(presentationsFile);
					String newCheckSum = getMetaDataCheckSum(resource.getProject());
					if (!newCheckSum.equals(oldCheckSum)) {
						setFileContents(presentationsFile, newCheckSum);
					}
				} catch (Throwable e) {
					Activator.log(e);
				}
			}
			return true;
		}
		return false;
	}

	private static boolean isInterestingResource(IResource resource) {
		if (resource instanceof IFile) {
			String path = resource.getFullPath().toPortableString().toLowerCase();
			return path.contains("/wbp-meta/")
					&& (path.endsWith(".wbp-component.xml") || path.endsWith(".png") || path.endsWith(".gif"));
		}
		return false;
	}

	private static String getMetaDataCheckSum(IProject project) throws Exception {
		MessageDigest algorithm = MessageDigest.getInstance("MD5");
		// add files
		SortedSet<IFile> files = getMetaDataFiles(project);
		for (IFile file : files) {
			byte[] bytes = getByteArray(file);
			algorithm.update(bytes);
		}
		// return check sum as hex String
		return getHexDigest(algorithm);
	}

	private static byte[] getByteArray(IFile file) throws Exception {
		InputStream inputStream = file.getContents(true);
		try {
			if (isTextFile(file)) {
				return getTextFileBytes(inputStream);
			}
			return IOUtils.toByteArray(inputStream);
		} catch (Throwable e) {
			return new byte[0];
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private static byte[] getTextFileBytes(InputStream inputStream) throws IOException {
		String content = IOUtils.toString(inputStream);
		String normalizedContent = content.replaceAll("\r\n", "\n");
		return normalizedContent.getBytes();
	}

	private static boolean isTextFile(IFile file) throws Exception {
		IContentDescription contentDescription = file.getContentDescription();
		if (contentDescription != null) {
			IContentType contentType = contentDescription.getContentType();
			while (contentType != null) {
				if ("org.eclipse.core.runtime.text".equals(contentType.getId())) {
					return true;
				}
				contentType = contentType.getBaseType();
			}
		}
		return false;
	}

	/**
	 * @return all interesting files in "wbp-meta", sorted by path.
	 */
	private static SortedSet<IFile> getMetaDataFiles(IProject project) throws CoreException {
		IFolder metaDataFolder = project.getFolder("wbp-meta");
		metaDataFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
		final SortedSet<IFile> files = new TreeSet<>(new Comparator<IFile>() {
			@Override
			public int compare(IFile o1, IFile o2) {
				String location1 = getLocation(o1);
				String location2 = getLocation(o2);
				return location1.compareTo(location2);
			}

			private String getLocation(IFile file) {
				return file.getFullPath().toPortableString().toLowerCase();
			}
		});
		metaDataFolder.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (isInterestingResource(resource)) {
					IFile file = (IFile) resource;
					files.add(file);
				}
				return true;
			}
		});
		return files;
	}

	private static String getHexDigest(final MessageDigest algorithm) {
		StringBuffer hexString = new StringBuffer();
		for (byte b : algorithm.digest()) {
			String s = Integer.toHexString(0xFF & b);
			if (s.length() == 1) {
				hexString.append("0");
			}
			hexString.append(s);
		}
		return hexString.toString();
	}

	private static String getCheckSum(IFile presentationsFile) throws Exception {
		BufferedReader br =
				new BufferedReader(new InputStreamReader(presentationsFile.getContents(true)));
		try {
			return br.readLine();
		} finally {
			IOUtils.closeQuietly(br);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Eclipse IFile utilities
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates or modifies given {@link IFile} with given {@link InputStream}.
	 */
	public static void setFileContents(IFile file, InputStream inputStream) throws CoreException {
		try {
			if (file.exists()) {
				file.setContents(inputStream, false, true, null);
			} else {
				file.create(inputStream, false, null);
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Creates or modifies given {@link IFile} with given {@link String}.
	 */
	public static void setFileContents(IFile file, String contents) throws CoreException {
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		setFileContents(file, inputStream);
	}
}
