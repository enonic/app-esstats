package com.enonic.app.esstats.shards;


import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.node.Node;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import com.enonic.xp.status.JsonStatusReporter;
import com.enonic.xp.status.StatusReporter;

@Component(immediate = true, service = StatusReporter.class)
@SuppressWarnings({"unused", "WeakerAccess"})
public class ShardsReporter
    extends JsonStatusReporter
{
    private static final Logger LOG = LoggerFactory.getLogger( ShardsReporter.class );

    private Node node;

    @Override
    public JsonNode getReport()
    {
        final ClusterStateRequest clusterStateRequest = new ClusterStateRequest();
        clusterStateRequest.local( true );
        clusterStateRequest.clear().
            nodes( true ).
            metaData( true ).
            routingTable( true );

        final ClusterStateResponse clusterStateReport = node.client().admin().cluster().state( clusterStateRequest ).actionGet();

        IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
        indicesStatsRequest.all();
        final IndicesStatsResponse indicesStatsResponse = node.client().admin().indices().stats( indicesStatsRequest ).actionGet();

        return new ShardsReport( clusterStateReport, indicesStatsResponse ).toJson();
    }

    @Override
    public String getName()
    {
        return "com.enonic.app.esstats.shards";
    }

    @Reference
    public void setNode( final Node node )
    {
        this.node = node;
    }
}
