/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import ru.spb.rybin.eclipsereplacement.CoreException;

public interface ICProjectConverter {
	ICProjectDescription convertProject(IProject project, IProjectDescription eclipseProjDes, String oldOwnerId, ICProjectDescription des) throws CoreException;
	
	boolean canConvertProject(IProject project, String oldOwnerId, ICProjectDescription des);
}