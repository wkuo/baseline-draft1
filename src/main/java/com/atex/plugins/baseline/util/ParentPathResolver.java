/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.structure.SiteRoot;

/**
 * Utility class used to retrieve parent paths from contents (security parent id
 * or, if existing, insert parent id).
 * @see example.util.ParentPathResolver
 */
public class ParentPathResolver {

    private static Logger LOG = Logger.getLogger(ParentPathResolver.class.getName());

    /**
     * Return the content path for the given content.
     * 
     * @param content
     *            the content to retrieve the path for
     * @param cmServer
     *            the cm server to use
     * 
     * @return the content path
     * 
     * @throws CMException
     *             if something goes wrong
     */
    public ContentId[] getParentPath(ContentRead content, PolicyCMServer cmServer) throws CMException {
        List<ContentId> parentList = getParentPathAsList(content, cmServer);

        return parentList.toArray(new ContentId[parentList.size()]);
    }

    public List<ContentId> getParentPathAsList(ContentRead content, PolicyCMServer cmServer) throws CMException {
        List<ContentId> parentList = createArrayList();

        ContentId contentId = content.getContentId().getContentId();
        Policy parent = getPolicyForContent(content, cmServer, contentId);

        // Follow parents until content of type SiteRoot is found
        while (parent != null && !isSiteRoot(parent)) {
            ContentRead parentContent = parent.getContent();
            parentList.add(0, parentContent.getContentId().getContentId());

            // Check if insert parent exists
            ContentId id = parentContent.getContentReference("polopoly.Parent", "insertParentId");

            // Otherwise, use security parent
            if (id == null) {
                id = parentContent.getSecurityParentId();
            }

            // If we get to a content that has no parent, then there is no site
            // root in the path, so we make one with just the original content
            if (id == null || id.getMinor() < 1) {
                parentList.clear();
                parentList.add(contentId);
                break;
            }

            parent = getPolicy(cmServer, id);
        }
        return parentList;
    }

    private Policy getPolicyForContent(ContentRead content, PolicyCMServer cmServer, ContentId contentId) throws CMException {
        Policy parent;
        if (content instanceof Policy) {
            parent = (Policy) content;
        } else {
            parent = cmServer.getPolicy(contentId);
        }
        return parent;
    }

    Policy getPolicy(PolicyCMServer cmServer, ContentId id) throws CMException {
        try {
            return cmServer.getPolicy(id);
        } catch (ContentOperationFailedException e) {
            if (id.getVersion() != VersionedContentId.LATEST_VERSION) {
                return cmServer.getPolicy(id.getLatestVersionId());
            } else
                throw e;
        }
    }

    boolean isSiteRoot(Policy policy) {
        return policy instanceof SiteRoot;
    }

    ArrayList<ContentId> createArrayList() {
        return new ArrayList<ContentId>();
    }

    public ContentId[] getParentPathNoExceptions(Content content, PolicyCMServer cmServer) {
        try {
            return getParentPath(content, cmServer);
        } catch (CMException e) {
            LOG.log(Level.FINE, "Could not get parent path for " + content.getContentId()
                    + " returning self as only element in paren path.", e);
            return new ContentId[] { content.getContentId().getContentId() };
        }
    }

}
