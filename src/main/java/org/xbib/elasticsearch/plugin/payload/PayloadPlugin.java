package org.xbib.elasticsearch.plugin.payload;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.similarity.SimilarityModule;
import org.elasticsearch.index.similarity.PayloadSimilarityProvider;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.plugins.Plugin;
import org.xbib.elasticsearch.index.analysis.PayloadTokenFilterFactory;
import org.xbib.elasticsearch.index.query.PayloadTermQueryParser;

public class PayloadPlugin extends Plugin {

    @Override
    public String name() {
        return "payload";
    }

    @Override
    public String description() {
        return "A payload plugin";
    }

    public void onModule(AnalysisModule module) {
        module.addTokenFilter("payload_tokenfilter", PayloadTokenFilterFactory.class);
    }

    public void onModule(SimilarityModule module) {
        module.addSimilarity("payload_similarity", PayloadSimilarityProvider.class);
    }

    public void onModule(IndicesModule module) {
        module.registerQueryParser(PayloadTermQueryParser.class);
    }

}
