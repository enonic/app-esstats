package com.enonic.app.esstats.recovery;


import org.elasticsearch.action.admin.indices.recovery.RecoveryRequest;
import org.elasticsearch.action.admin.indices.recovery.RecoveryResponse;
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
public class RecoveryReporter
    extends JsonStatusReporter
{
    private static final Logger LOG = LoggerFactory.getLogger( RecoveryReporter.class );

    private Node node;

    @Override
    public JsonNode getReport()
    {
        final RecoveryRequest recoveryRequest = new RecoveryRequest();
        recoveryRequest.detailed( false );
        recoveryRequest.activeOnly( false );
        recoveryRequest.listenerThreaded( false );

        final RecoveryResponse recoveryResponse = node.client().admin().indices().recoveries( recoveryRequest ).actionGet();

        return new RecoveryReport( recoveryResponse ).toJson();
    }

    @Override
    public String getName()
    {
        return "com.enonic.app.esstats.recovery";
    }

    @Reference
    public void setNode( final Node node )
    {
        this.node = node;
    }
}
