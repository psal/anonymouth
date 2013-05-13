package org.annolab.tt4j;

/**
 * A {@link TokenHandler} can implement this interface to get probability information when
 * {@link TreeTaggerWrapper#setProbabilityThreshold(Double)} is used.
 * 
 * @author Richard Eckart de Castilho
 */
public 
interface ProbabilityHandler<O> extends TokenHandler<O>
{
    /**
     * Process the probabilities for the last token provided to {@link TokenHandler#token}.
     *
     * @param pos the Part-of-Speech tag as produced by TreeTagger or <code>null</code>.
     * @param lemma the lemma as produced by TreeTagger or <code>null</code>.
     * @param probability the probability of the tag/lemma.
     */
    void probability(
            String pos,
            String lemma,
            double probability);
}
