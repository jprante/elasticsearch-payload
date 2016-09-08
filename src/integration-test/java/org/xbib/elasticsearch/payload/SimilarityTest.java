package org.xbib.elasticsearch.payload;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

import org.elasticsearch.Version;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbib.elasticsearch.index.query.PayloadTermQueryBuilder;
import org.xbib.elasticsearch.plugin.payload.PayloadPlugin;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;

public class SimilarityTest {

    @Test
    public void testPayload() throws IOException {
        NodeTestUtils nodeTestUtils = new NodeTestUtils();
        Node node = nodeTestUtils.createNode();
        Client client = node.client();

        Settings indexSettings = Settings.settingsBuilder()
                .put("index.similarity.payload.type", "payload_similarity")
                .put("analysis.analyzer.payloads.type", "custom")
                .put("analysis.analyzer.payloads.tokenizer", "whitespace")
                .put("analysis.analyzer.payloads.filter.0", "lowercase")
                .put("analysis.analyzer.payloads.filter.1", "delimited_payload_filter")
                .build();
        String mapping = XContentFactory.jsonBuilder().startObject()
                .startObject("bar")
                .startObject("properties")
                .startObject("field1")
                    .field("type", "string")
                    .field("analyzer", "payloads" )
                    .field("term_vector", "with_positions_offsets_payloads")
                    .field("similarity", "payload") // resolves to org.elasticsearch.index.similarity.PayloadSimilarity
                .endObject()
                .endObject()
                .endObject()
                .endObject().string();

        client.admin().indices()
                .prepareCreate("foo")
                .setSettings(indexSettings)
                .addMapping("bar", mapping)
                .execute().actionGet();

        client.prepareIndex("foo", "bar", "1")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .field("field1", "Hello|5.0 World")
                        .endObject()
                )
                .setRefresh(true)
                .execute().actionGet();

        QueryBuilder queryBuilder = new PayloadTermQueryBuilder("field1", "hello");
        SearchResponse searchResponse = client.prepareSearch()
                .setIndices("foo")
                .setTypes("bar")
                .setExplain(true)
                .setQuery(queryBuilder).execute().actionGet();

        System.err.println("payload term query = " + searchResponse.toString());

        queryBuilder = new PayloadTermQueryBuilder("field1", "world");
        searchResponse = client.prepareSearch()
                .setIndices("foo")
                .setTypes("bar")
                .setExplain(true)
                .setQuery(queryBuilder).execute().actionGet();

        System.err.println("non-payload term query = " + searchResponse.toString());

        nodeTestUtils.releaseNode(node);
    }
}

class NodeTestUtils {

    private Node node;

    Node createNode() {
        System.err.println("path.home = " + System.getProperty("path.home"));
        Settings nodeSettings = Settings.settingsBuilder()
                .put("path.home", System.getProperty("path.home"))
                .put("index.number_of_shards", 1)
                .put("index.number_of_replica", 0)
                .build();
        Node node = new MockNode(nodeSettings, PayloadPlugin.class);
        node.start();
        return node;
    }

    void releaseNode(Node node) throws IOException {
        if (node != null) {
            node.close();
            deleteFiles();
        }
    }

    @Before
    public void setupNode() throws IOException {
        node = createNode();
    }

    @After
    public void cleanupNode() throws IOException {
        releaseNode(node);
    }

    private void deleteFiles() throws IOException {
        Path directory = Paths.get(System.getProperty("path.home") + "/data");
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

class MockNode extends Node {

    MockNode(Settings settings, Class<? extends Plugin> classpathPlugin) {
        this(settings, list(classpathPlugin));
    }

    private MockNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
        super(InternalSettingsPreparer.prepareEnvironment(settings, null), Version.CURRENT, classpathPlugins);
    }

    public MockNode(Settings settings) {
        this(settings, list());
    }

    private static Collection<Class<? extends Plugin>> list() {
        return new ArrayList<>();
    }

    private static Collection<Class<? extends Plugin>> list(Class<? extends Plugin> classpathPlugin) {
        Collection<Class<? extends Plugin>> list = new ArrayList<>();
        list.add(classpathPlugin);
        return list;
    }
}
