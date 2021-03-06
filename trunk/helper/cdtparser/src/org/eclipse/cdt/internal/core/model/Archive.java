/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;

import ru.spb.rybin.eclipsereplacement.Assert;
import ru.spb.rybin.eclipsereplacement.IProgressMonitor;

public class Archive extends Openable implements IArchive {

	IBinaryArchive binaryArchive;

	public Archive(ICElement parent, IFile file, IBinaryArchive ar) {
		super(parent, file, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	public Archive(ICElement parent, IPath path, IBinaryArchive ar) {
		super (parent, path, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	@Override
	public IBinary[] getBinaries() throws CModelException {
		ICElement[] e = getChildren();
		IBinary[] b = new IBinary[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public CElementInfo createElementInfo() {
		return new ArchiveInfo(this);
	}

	protected ArchiveInfo getArchiveInfo() throws CModelException {
		return (ArchiveInfo)getElementInfo();
	}

	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement, CElementInfo> newElements, IResource underlyingResource)
		throws CModelException {
		return computeChildren(info, underlyingResource);
	}

	public boolean computeChildren(OpenableInfo info, IResource res) {
		IBinaryArchive ar = getBinaryArchive();
		if (ar != null) {
			IBinaryObject[] objects = ar.getObjects();
			for (final IBinaryObject obj : objects) {
				Binary binary = new Binary(this, ar.getPath().append(obj.getName()), obj);
				info.addChild(binary);
			}
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IBinaryArchive.class.equals(adapter)) {
			return getBinaryArchive();
		}
		return super.getAdapter(adapter);
	}

	IBinaryArchive getBinaryArchive() {
		return binaryArchive;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	@Override
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		return super.exists();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#closing(java.lang.Object)
	 */
	@Override
	protected void closing(Object info) throws CModelException {
		ICProject cproject = getCProject();
		CProjectInfo pinfo = (CProjectInfo)CModelManager.getDefault().peekAtInfo(cproject);
		if (pinfo != null && pinfo.vLib != null) {
			pinfo.vLib.removeChild(this);
		}
		super.closing(info);
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		return null;
	}

	@Override
	public String getHandleMemento() {
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {
		Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
		return 0;
	}

}
