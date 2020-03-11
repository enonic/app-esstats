package com.enonic.app.esstats.shards;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ShardsReport
{
    private final ClusterStateResponse clusterState;

    private final IndicesStatsResponse indicesStatsResponse;

    ShardsReport( final ClusterStateResponse clusterState, final IndicesStatsResponse indicesStatsResponse )
    {
        this.clusterState = clusterState;
        this.indicesStatsResponse = indicesStatsResponse;
    }

    ObjectNode toJson()
    {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();

        this.clusterState.getState().routingTable().indicesRouting().forEach( ( index, value ) -> json.set( index, toJson( value ) ) );

        return json;
    }

    private ArrayNode toJson( final IndexRoutingTable indexRoutingTable )
    {
        final ArrayNode shardsJson = JsonNodeFactory.instance.arrayNode();

        for ( final IndexShardRoutingTable next : indexRoutingTable )
        {
            next.getShards().forEach( shard -> shardsJson.add( toJson( shard ) ) );
        }

        return shardsJson;
    }

    private ObjectNode toJson( final ShardRouting shard )
    {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put( "shardType", shard.primary() ? "primary" : "replica" );
        json.put( "assignedToNode", shard.assignedToNode() );
        json.put( "state", shard.state().name() );
        json.put( "node", shard.currentNodeId() );
        json.put( "relocatingTo", shard.relocatingNodeId() );

        final IndexStats indexStats = this.indicesStatsResponse.getIndex( shard.index() );

        if ( indexStats != null )
        {
            json.put( "documents", indexStats.getTotal().docs.getCount() );
            json.put( "size", indexStats.getTotal().store.getSize().toString() );
        }

        return json;
    }


}
