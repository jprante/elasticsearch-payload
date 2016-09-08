package org.elasticsearch.index.similarity;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

public class PayloadSimilarity extends ClassicSimilarity {

    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        if (payload != null) {
            return PayloadHelper.decodeFloat(payload.bytes, payload.offset);
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "PayloadSimilarity";
    }
}
