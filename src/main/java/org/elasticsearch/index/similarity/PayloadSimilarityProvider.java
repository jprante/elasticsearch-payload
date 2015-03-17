package org.elasticsearch.index.similarity;

import org.elasticsearch.common.inject.Inject;

public class PayloadSimilarityProvider extends AbstractSimilarityProvider {

    private final PayloadSimilarity similarity = new PayloadSimilarity();

    @Inject
    public PayloadSimilarityProvider(String name) {
        super(name);
    }

    @Override
    public PayloadSimilarity get() {
        return similarity;
    }
}
