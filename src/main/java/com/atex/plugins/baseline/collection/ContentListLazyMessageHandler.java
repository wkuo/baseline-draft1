/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.collection;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.util.MessageUtil;
import com.polopoly.util.LocaleUtil;

/**
 * Handler for content list messages. Writes a warning message if content list
 * is truncated.
 * @see example.collection.ContentListLazyMessageHandler
 */
public class ContentListLazyMessageHandler {

    /**
     * Setup warning messages for content list
     * 
     * @param oc
     * @param contentListProvider
     *            content list provider providing the content list.
     * @throws CMException
     */
    public void setupWarningMessages(OrchidContext oc, ContentListProvider contentListProvider) throws CMException {
        ContentList contentList = contentListProvider.getContentList();
        if (contentList instanceof ContentListLazy) {
            ContentListLazy contentListLazy = (ContentListLazy) contentList;
            if (!contentListLazy.isComplete()) {
                String warningMessage = LocaleUtil.format("p.siteengine.search.result.truncated", oc.getMessageBundle());

                MessageUtil.setWarningMessage(oc, warningMessage);
            }
        }
    }
}
