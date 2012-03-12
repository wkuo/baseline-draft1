/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.collection;

import java.util.List;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.siteengine.standard.feed.Feedable;

/**
 * @see example.collection.ModelTypeDescriptionPublishingQueue
 */
public interface ModelTypeDescriptionPublishingQueue extends ModelTypeDescription {

    List<Feedable> getFeedables() throws CMException;

    ContentList getContentList() throws CMException;

}
