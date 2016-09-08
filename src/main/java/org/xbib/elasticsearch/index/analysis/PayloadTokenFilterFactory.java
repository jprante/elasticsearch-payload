package org.xbib.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.FloatEncoder;
import org.apache.lucene.analysis.payloads.IdentityEncoder;
import org.apache.lucene.analysis.payloads.IntegerEncoder;
import org.apache.lucene.analysis.payloads.PayloadEncoder;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsService;

public class PayloadTokenFilterFactory extends AbstractTokenFilterFactory {

    private final PayloadEncoder encoder;

    private final char delimiter;

    @Inject
    public PayloadTokenFilterFactory(Index index,
                                     IndexSettingsService indexSettingsService,
                                     @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        this.encoder = createEncoder(settings.get("encoder", "float"));
        this.delimiter = settings.get("delimiter", "|").charAt(0);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DelimitedPayloadTokenFilter(tokenStream, delimiter, encoder);
    }

    private PayloadEncoder createEncoder(String encoder) {
        if ("float".equals(encoder)) {
            return new FloatEncoder();
        } else if ("integer".equals(encoder)) {
            return new IntegerEncoder();
        } else if ("identity".equals(encoder)) {
            return new IdentityEncoder();
        }
        return null;
    }
}
