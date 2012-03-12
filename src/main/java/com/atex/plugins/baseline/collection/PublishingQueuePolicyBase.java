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
import java.util.List;

import com.atex.plugins.baseline.content.ContentBasePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.ContentListModel;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelDomain;
import com.polopoly.model.ModelFactory;
import com.polopoly.siteengine.dispatcher.SiteEngine;
import com.polopoly.siteengine.dispatcher.SiteEngineApplication;
import com.polopoly.siteengine.standard.feed.FeedProvider;
import com.polopoly.siteengine.standard.feed.Feedable;

/**
 * Base policy class for publishing queues. The get content list method creates
 * a {@link ContentListModel} from the source content list.
 * 
 * This enable convenient access patterns for the content list in template
 * languages like velocity.
 * <p>
 * The following velocity code exemplifies the access ("a" being the object of
 * the current class):
 * 
 * <pre>
 * #foreach($entry in $a.contentList)
 *   $entry.content.name        #name of content
 *   $entry.meta.name           #name of the meta data
 * #end
 * </pre>
 * 
 * Where <code>$a.contentList</code> is a iterable list of content reference
 * models where, for each {@link ContentReference} entry in the list,
 * <code>$entry.content</code> is the model for the referenced content and
 * <code>$entry.meta</code> is the model for the meta data content.
 * <p>
 * This class also handles indexing of site containment.
 * @see example.collection.PublishingQueuePolicyBase
 */
public class PublishingQueuePolicyBase extends ContentBasePolicy
        implements ContentListProvider, FeedProvider, ModelTypeDescriptionPublishingQueue {
    /**
     * Gets the content list as a ContentListModel. If no ModelDomain is
     * available, the unwrapped ContentList is returned.
     */
    public final ContentList getContentList() throws CMException {
        ContentList contentList = getSourceContentList();

        SiteEngineApplication application = SiteEngine.getApplication();

        if (application != null) {
            ModelFactory modelFactory = application.getModelFactory();
            ModelDomain modelDomain = application.getModelDomain();

            if (modelFactory != null && modelDomain != null) {

                // Create a content list model from the content list, which both
                // implements ContentList interface and the ModelList interface,
                // simplifying access patterns in template languages like
                // velocity or
                // JSP.
                contentList = (ContentListModel) modelFactory.createModel(modelDomain, contentList);
            } else {
                logger.fine("Using unwrapped content list. ModelFactory: '" + modelFactory + "', ModelDomain: '" + modelDomain
                        + "'");
            }
        } else {
            logger.fine("SiteEngineApplication was null. Using unwrapped content list.");
        }
        return contentList;
    }

    /**
     * Get the source content list. The default is that the source content list
     * is the contents default content list. By overriding this method, other
     * content lists can be used as source for the {@link ContentListProvider},
     * for example to create the content list using searches or other kinds of
     * dynamic creation.
     * 
     * @return the original content list.
     * @throws CMException
     */
    protected ContentList getSourceContentList() throws CMException {
        ContentList contentList = super.getContentList();
        return contentList;
    }

    /**
     * Method to get all {@link Feedable} objects contained in this queue
     * 
     * @return List of {@link Feedable}s
     */
    public final List<Feedable> getFeedables() throws CMException {
        List<Feedable> feedables = new ArrayList<Feedable>();

        ContentListRead contentList = getContentList();
        PolicyCMServer cmServer = getCMServer();
        for (int j = 0; j < contentList.size(); j++) {
            ContentId contentId = contentList.getEntry(j).getReferredContentId();
            if (contentId.getMajor() == 1) {
                try {
                    Policy content = cmServer.getPolicy(contentId);
                    if (content instanceof Feedable) {
                        feedables.add((Feedable) content);
                    }
                } catch (ContentOperationFailedException e) {
                    // The content may be non-public or similar (or maybe even
                    // removed!).
                    // We can safely ignore this and proceed with any other
                    // feedables.
                    // Logging this could lead to serious spamming of the logs
                    // so we don't.
                }
            }
        }
        return feedables;
    }
}
