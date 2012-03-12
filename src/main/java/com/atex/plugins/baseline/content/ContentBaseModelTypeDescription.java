/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.content;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionInfo;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.OutputTemplate;

/**
 * @see example.content.ContentBaseModelTypeDescription
 */
public interface ContentBaseModelTypeDescription {

    public VersionedContentId getContentId() throws CMException;

    public VersionInfo getVersionInfo();

    public String getName() throws CMException;

    public void setName(String name) throws CMException;

    public OutputTemplate getOutputTemplate(String mode) throws CMException;

    public ContentId[] getParentIds();
}
