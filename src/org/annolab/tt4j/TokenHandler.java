/*******************************************************************************
 * Copyright (c) 2009-2010 Richard Eckart de Castilho.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Richard Eckart de Castilho - initial API and implementation
 ******************************************************************************/
package org.annolab.tt4j;

/**
 * A token handler receives a notification for each tagged token.
 *
 * @author Richard Eckart de Castilho
 *
 * @param <O> the token type.
 */
public
interface TokenHandler<O>
{
	/**
	 * Process a token that TreeTagger has analyzed.
	 *
	 * @param token the one of the token objects passed to
	 *     {@link TreeTaggerWrapper#process(java.util.Collection)}
	 * @param pos the Part-of-Speech tag as produced by TreeTagger or <code>null</code>.
	 * @param lemma the lemma as produced by TreeTagger or <code>null</code>.
	 */
	void token(
			O token,
			String pos,
			String lemma);
}
