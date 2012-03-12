/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.siteengine.layout.ContentRepresentative;

/**
 * Feedable content list policy.
 * @see example.collection.PublishingQueuePolicyManual
 */
public class PublishingQueuePolicyManual extends PublishingQueuePolicyBase implements ContentRepresentative {
    /**
     * Used for html title and for name in crumb trail
     */
    public String getTitle() throws CMException {
        String title = ((SingleValued) getChildPolicy("title")).getValue();

        if (title == null || title.length() <= 0) {
            title = getName();
        }
        return title;
    }

    public Collection<ContentId> getRepresentedContent() {
        List<ContentId> containedIds = new ArrayList<ContentId>();
        try {
            ListIterator<ContentReference> iterator = getContentList().getListIterator();
            while (iterator.hasNext()) {
                ContentReference ref = iterator.next();
                if (ref != null && ref.getReferredContentId() != null) {
                    containedIds.add(ref.getReferredContentId());
                }
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Failed to own contained content for publishing queue " + this.getContentId(), e);
        }
        return containedIds;
    }

}
