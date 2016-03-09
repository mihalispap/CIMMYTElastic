#!/bin/bash

curl -XPUT "http://localhost:9201/cimmyt/" -d '{
    "settings" : {
        "number_of_shards" : 3,
        "number_of_replicas" : 0,
        "routing.allocation.include.zone" : "main",
        "query.default_field" : "_all",

        "analysis":{
            "filter": {
                "edge_ngram" : {
                    "side" : "front",
                    "max_gram" : 10,
                    "min_gram" : 2,
                    "type" : "edgeNGram"
                },

                "en_stop_filter": {
                    "type": "stop",
                    "stopwords": ["_english_"]
                },
                "en_stem_filter": {
                    "type": "stemmer",
                    "name": "minimal_english"
                },

                "el_stop_filter": {
                    "type": "stop",
                    "stopwords": ["_greek_"]
                },
                "el_stem_filter": {
                    "type": "stemmer",
                    "name": "greek"
                }
            },
            "analyzer": {
                "en_analyzer": {
                    "type": "custom",
                    "tokenizer": "icu_tokenizer",
                    "filter": ["icu_folding", "icu_normalizer", "en_stop_filter", "en_stem_filter"],
                    "char_filter": ["html_strip"]
                },

                "el_analyzer": {
                    "type": "custom",
                    "tokenizer": "icu_tokenizer",
                    "filter": ["icu_folding", "icu_normalizer", "el_stop_filter", "el_stem_filter"],
                    "char_filter": ["html_strip"]
                }
            }
        }
    },

    "mappings" : {
        "collection" : {
            "dynamic_templates" : [
                {
                    "template_languageBlocks" : {
                        "path_match" : "languageBlocks.*",
                        "mapping" : {
                            "properties" : {
                                "title" : { "type" : "string" },
                                "description" : { "type" : "string" },
                                "keywords" : { "type" : "string" },
                                "coverage" : { "type" : "string", "index_name" : "keyword"}
                            }
                        }
                    }
                },
                {
                    "template_taxonPaths" : {
                        "path_match" : "tokenBlock.taxonPaths.*",
                        "mapping" : {
                            "type" : "string"
                        }
                    }
                },
                {
                    "template_rights" : {
                        "path_match" : "rights.description.*",
                        "mapping" : {
                            "type" : "string"
                        }
                    }
                },
                {
                    "template_learningObjectives" : {
                        "path_match" : "learningObjectives.properties.*",
                        "mapping" : {
                            "type" : "string"
                        }
                    }
                }
            ],

	    "_id" : {
                "path" :"identifier",
                "type": "string",
                "index": "not_analyzed"
            },


            "_source" : {
                "enabled" : true,
                "compress" : true,
                "compress_threshold" : "1536b"
            },

            "_all" : {
                "enabled" : true
            },

            "_routing" : {
                "required" : true,
                "path" : "set"
            },

            "_timestamp" : {
                "enabled" : true,
                "path" : "lastUpdateDate",
                "format" : "date"
            },

            "properties" : {
                "generateThumbnail" : {
                    "type" : "boolean",
                    "include_in_all" : false
                },
                "status" : {
                    "type" : "string"
                },
                "set" : {
                    "type" : "string"
                },
                "creationDate" : {
                    "type" : "date",
                    "format" : "date"
                },
                "lastUpdateDate" : {
                    "type" : "date",
                    "format" : "date",
                    "include_in_all" : false
                },

                "languageBlocks" : {
                    "type" : "object",
                    "properties" : {
                        "en": {
                            "properties" : {
                                "title" : { "type" : "string", "analyzer" : "en_analyzer" },
                                "description" : { "type" : "string", "analyzer" : "en_analyzer" },
                                "keywords" : { "type" : "string", "index_name" : "keyword" },
                                "coverage" : { "type" : "string","analyzer" : "keyword" }
                            }
                        },
                        "el": {
                            "properties" : {
                                "title" : { "type" : "string", "analyzer" : "el_analyzer" },
                                "description" : { "type" : "string", "analyzer" : "el_analyzer" },
                                "keywords" : { "type" : "string", "index_name" : "keyword" },
                                "coverage" : { "type" : "string","analyzer" : "keyword" }
                            }
                        }
                    }
                },

                "tokenBlock" : {
                    "properties" : {
                        "ageRange" : { "type" : "string" ,"analyzer" : "keyword"},
                        "contexts" : { "type" : "string", "index_name" : "context","analyzer" : "keyword"},
                        "endUserRoles" : { "type" : "string", "index_name" : "endUserRole","analyzer" : "keyword" },
                        "learningResourceTypes" : { "type" : "string", "index_name" : "learningResourceType","analyzer" : "keyword" },
                        "taxonPaths" : {
                            "type" : "object"
                        }
                    }
                },

                "expressions" : {
                    "properties" : {
                        "language" : { "type" : "string" },
                        "manifestations" : {
                            "properties" : {
                                "name" : { "type" : "string" },
                                "parameter" : { "type" : "string" },
                                "duration" : { "type" : "string" },
                                "identifier" : { "type" : "string", "index": "not_analyzed" },
                                "items" : {
                                    "properties" : {
                                        "url": { "type" : "string" },
                                        "broken" : {
                                            "type" : "boolean",
                                            "include_in_all" : false
                                        }
                                    }
                                }
                            }
                        }
                    }
                },

                "rights" : {
                    "properties" : {
                        "url" : { "type" : "string" },
                        "description" : {
                            "type" : "object",
                            "properties" : {
                                "en": { "type" : "string", "analyzer" : "en_analyzer" },
                                "el": { "type" : "string", "analyzer" : "el_analyzer" }
                            }
                        }
                    }
                },

                "contributors" : {
                    "properties" : {
                        "date" : {
                            "type" : "string",
                            "include_in_all" : false
                        },
                        "name" : { "type" : "string" },
                        "organization" : { "type" : "string","analyzer" : "keyword"},
                        "role" : { "type" : "string" },
                        "description" : { "type" : "object" }
                    }
                },

                "learningObjectives" : {
                    "properties" : {
                        "properties" : {
                            "type" : "object"
                        }
                    }
                }
            }
        }
    }
}'
