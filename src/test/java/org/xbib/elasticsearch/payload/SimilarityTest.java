package org.xbib.elasticsearch.payload;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.index.similarity.SimilarityService;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.node.internal.InternalNode;
import org.junit.Test;
import org.elasticsearch.index.similarity.PayloadSimilarity;
import org.elasticsearch.index.similarity.PayloadSimilarityProvider;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SimilarityTest {

    @Test
    public void testResolvePayloadSimilaritiesFromMapping() throws IOException {
        Settings indexSettings = ImmutableSettings.settingsBuilder()
                .put("index.similarity.my_similarity.type", "payload")
                .build();
        SimilarityService similarityService = createIndex("foo", indexSettings).similarityService();

        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type")
                .startObject("properties")
                .startObject("field1").field("type", "string").field("similarity", "my_similarity").endObject()
                .endObject()
                .endObject().endObject().string();

        DocumentMapper documentMapper = similarityService.mapperService().documentMapperParser().parse(mapping);
        assertEquals(documentMapper.mappers().name("field1").mapper().similarity().getClass(), PayloadSimilarityProvider.class);
        PayloadSimilarity similarity = (PayloadSimilarity) documentMapper.mappers().name("field1").mapper().similarity().get();
    }

    protected static IndexService createIndex(String index, Settings settings) {
        return createIndex(index, settings, null, null);
    }

    protected static IndexService createIndex(String index, Settings settings, String type, XContentBuilder mappings) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client().admin().indices().prepareCreate(index).setSettings(settings);
        if (type != null && mappings != null) {
            createIndexRequestBuilder.addMapping(type, mappings);
        }
        return createIndex(index, createIndexRequestBuilder);
    }

    protected static IndexService createIndex(String index, CreateIndexRequestBuilder createIndexRequestBuilder) {
        createIndexRequestBuilder.get();
        ClusterHealthResponse health = client().admin().cluster()
                .health(Requests.clusterHealthRequest(index).waitForYellowStatus()
                        .waitForRelocatingShards(0)).actionGet();
        IndicesService instanceFromNode = getInstanceFromNode(IndicesService.class);
        return instanceFromNode.indexServiceSafe(index);
    }

    private static Node newNode() {
        Node build = NodeBuilder.nodeBuilder().local(true).data(true).settings(ImmutableSettings.builder()
                .put(ClusterName.SETTING, "1")
                .put("node.name", "1")
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put("script.disable_dynamic", false)
                .put(EsExecutors.PROCESSORS, 1) // limit the number of threads created
                .put("http.enabled", false)
                .put("index.store.type", "ram")
                .put("config.ignore_system_properties", true) // make sure we get what we set :)
                .put("gateway.type", "none")).build();
        build.start();
        return build;
    }

    protected static <T> T getInstanceFromNode(Class<T> clazz) {
        return ((InternalNode) Holder.NODE).injector().getInstance(clazz);
    }

    protected static Node node() {
        return Holder.NODE;
    }

    public static Client client() {
        return Holder.NODE.client();
    }

    private static class Holder {
        private static Node NODE = newNode();

        private static void reset() {
            assert NODE != null;
            node().stop();
            Holder.NODE = newNode();
        }
    }
}

