/*
 * Copyright (C) 2023 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.accounts.events.rabbitmq;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.google.common.annotations.VisibleForTesting;

import lombok.extern.slf4j.Slf4j;

/**
 * Listens for messages from RabbitMQ and processes received events.
 * <p>
 * This listener processes incoming messages related to OAuth2 account creation
 * events. It ensures that duplicate messages are not logged more than once by
 * maintaining a synchronized set of processed message UIDs.
 * </p>
 *
 * <p>
 * If an error occurs while processing a message, it is logged and silently
 * discarded.
 * </p>
 */
@Slf4j
public class RabbitmqEventsListener implements MessageListener {

    /**
     * The subject indicating that an OAuth2 account creation event has been
     * received.
     */
    public static final String OAUTH2_ACCOUNT_CREATION_RECEIVED = "OAUTH2-ACCOUNT-CREATION-RECEIVED";

    /**
     * A synchronized set to track processed message UIDs and prevent duplicate
     * processing.
     */
    private static final Set<String> synReceivedMessageUid = Collections.synchronizedSet(new HashSet<>());

    /**
     * Processes an incoming RabbitMQ message.
     * <p>
     * If the message contains a subject matching
     * {@code OAUTH2-ACCOUNT-CREATION-RECEIVED} and has not already been processed,
     * it logs the message content.
     * </p>
     *
     * @param message the incoming RabbitMQ message
     */
    @Override
    public void onMessage(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JSONObject jsonObj = new JSONObject(messageBody);
            String uid = jsonObj.getString("uid");
            String subject = jsonObj.getString("subject");

            if (subject.equals(OAUTH2_ACCOUNT_CREATION_RECEIVED) && !synReceivedMessageUid.contains(uid)) {
                String msg = jsonObj.getString("msg");
                synReceivedMessageUid.add(uid);
                log.info(msg);
            }
        } catch (Exception e) {
            log.error("Exception caught when evaluating a message from RabbitMQ. It will be silently discarded.", e);
        }
    }

    /**
     * Returns the set of received message UIDs for testing purposes.
     *
     * @return an unmodifiable view of the received message UIDs
     */
    @VisibleForTesting
    public static Set<String> getSynReceivedMessageUid() {
        return Collections.unmodifiableSet(synReceivedMessageUid);
    }
}
