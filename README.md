# Elasticsearch Payload Plugin

Demo of payload term query in Elasticsearch.

The payloads in this plugin are floating point numbers used to 
manipulate the term weight during scoring.

## Versions

| Elasticsearch  | Plugin         | Release date |
| -------------- | -------------- | ------------ |
| 2.4.0          | 2.4.0.0        | Sep  8, 2016 |

## Installation


    ./bin/plugin install 'http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-payload/2.4.0.0/elasticsearch-payload-2.4.0.0-plugin.zip'

Do not forget to restart the node after installing.

## Issues

All feedback is welcome! If you find issues, please post them at [Github](https://github.com/jprante/elasticsearch-payload/issues)


## Example

For a Java API example, see SimilarityTest.java

This is a sequence of curl commands to demonstrate the usage of the plugin.

    curl -XDELETE 'localhost:9200/test'

    curl -XPUT 'localhost:9200/test' -d '
    {
      "settings" : {
         "number_of_shards" : "1",
         "number_of_replicas" : "0",
         "similarity" : {
             "my_payload_similarity" : {
                 "type" : "payload_similarity"
             }
         },
         "analysis" : {
                "analyzer" : {
                    "my_payload_analyzer" : {
                        "type" : "custom",
                        "tokenizer" : "whitespace",
                        "filter" : [ "lowercase", "delimited_payload_filter" ]
                    }
                 }
         }
      }
    }
    '

    curl -XPUT 'localhost:9200/test/docs/_mapping' -d '
    {
          "properties" : {
            "content" : {
              "type": "string",
              "analyzer" : "my_payload_analyzer",
              "term_vector": "with_positions_offsets_payloads",
              "similarity" : "my_payload_similarity"
            }
          }
    }
    '

    curl -XPUT 'localhost:9200/test/docs/1' -d '
    {
        "content" : "foo bar baz"
    }
    '

    curl -XPUT 'localhost:9200/test/docs/2' -d '
    {
        "content" : "foo bar|2.0 baz"
    }
    '

    curl -XPUT 'localhost:9200/test/docs/3' -d '
    {
        "content" : "foo bar|3.0 baz"
    }
    '

    curl -XGET 'localhost:9200/test/_refresh'

    curl -XGET 'localhost:9200/test/_settings?pretty'
    curl -XGET 'localhost:9200/test/_mappings?pretty'
    curl -XGET 'localhost:9200/test/_analyze?analyzer=my_payload_analyzer&text=foo%20bar%7C2.0%20baz&pretty'

    curl -XGET 'localhost:9200/test/docs/_search?pretty' -d '
    {
        "explain" : true,
        "query": {
            "payload_term": {
                "content" : "bar"
            }
        }
    }
    '


# License

Elasticsearch Payload Plugin

Copyright (C) 2015, 2016 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
you may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.