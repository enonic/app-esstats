package com.enonic.app.esstats.recovery;

import java.util.List;

import org.elasticsearch.action.admin.indices.recovery.RecoveryResponse;
import org.elasticsearch.action.admin.indices.recovery.ShardRecoveryResponse;
import org.elasticsearch.cluster.routing.RestoreSource;
import org.elasticsearch.indices.recovery.RecoveryState;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class RecoveryReport
{

    private final RecoveryResponse recovery;

    RecoveryReport( final RecoveryResponse recovery )
    {
        this.recovery = recovery;
    }

    ObjectNode toJson()
    {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();

        this.recovery.shardResponses().forEach( ( index, value ) -> json.set( index, toJson( value ) ) );

        return json;
    }

    private ArrayNode toJson( final List<ShardRecoveryResponse> shardsRecovery )
    {
        final ArrayNode shardsJson = JsonNodeFactory.instance.arrayNode();

        shardsRecovery.forEach( shardResponse -> shardsJson.add( toJson( shardResponse ) ) );

        return shardsJson;
    }

    private ObjectNode toJson( final ShardRecoveryResponse shardRecovery )
    {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();
        final RecoveryState recoveryState = shardRecovery.recoveryState();
        json.put( "primary", recoveryState.getPrimary() );
        json.put( "type", shardRecovery.recoveryState().getType().name() );
        json.put( "stage", recoveryState.getStage().name() );
        json.put( "timeUsed", recoveryState.getTimer().time() );

        if ( recoveryState.getRestoreSource() != null )
        {
            final ObjectNode restoreStateJson = JsonNodeFactory.instance.objectNode();
            final RestoreSource restoreSource = recoveryState.getRestoreSource();
            if ( restoreSource != null )
            {
                restoreStateJson.put( "index", restoreSource.index() );
                restoreStateJson.put( "snapshot", restoreSource.snapshotId() != null ? restoreSource.snapshotId().getSnapshot() : null );

            }
            json.put( "recovered", recoveryState.getTranslog().recoveredPercent() );
            json.put( "total", recoveryState.getTranslog().totalOperations() );
            json.set( "recovery", restoreStateJson );
        }

        return json;
    }
}
