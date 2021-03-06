/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX Software Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import ru.spb.rybin.eclipsereplacement.IProgressMonitor;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPDOMIndexerTask {
	public static final String TRACE_ACTIVITY   = CCorePlugin.PLUGIN_ID + "/debug/indexer/activity";  //$NON-NLS-1$
	public static final String TRACE_STATISTICS = CCorePlugin.PLUGIN_ID + "/debug/indexer/statistics";  //$NON-NLS-1$
	public static final String TRACE_INCLUSION_PROBLEMS = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems/inclusion";  //$NON-NLS-1$
	public static final String TRACE_SCANNER_PROBLEMS = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems/scanner";  //$NON-NLS-1$
	public static final String TRACE_SYNTAX_PROBLEMS = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems/syntax";  //$NON-NLS-1$
	public static final String TRACE_PROBLEMS   = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems";  //$NON-NLS-1$

	/**
	 * Called by the framework to perform the task.
	 */
	public void run(IProgressMonitor monitor) throws InterruptedException;

	/**
	 * Returns the indexer the task belongs to.
	 */
	public IPDOMIndexer getIndexer();

	/**
	 * Returns progress information for the task.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public IndexerProgress getProgressInformation();

	/**
	 * Takes files from another task and adds them to this task in front of all not yet processed
	 * files. The urgent work my be rejected if this task is not capable of accepting it, or if
	 * the amount of urgent work is too large compared to the work already assigned to this task.
	 * @param task the task to add the work from.
	 * @return {@code true} if the work was accepted, {@code false} if it was rejected.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=319330"
	 * @since 5.3
	 */
	public boolean acceptUrgentTask(IPDOMIndexerTask task);
}
