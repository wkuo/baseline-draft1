package com.atex.plugins.baseline.collection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import com.polopoly.cm.app.policy.ContentListModel;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.model.ModelDomain;
import com.polopoly.model.ModelFactory;
import com.polopoly.siteengine.dispatcher.SiteEngine;
import com.polopoly.siteengine.dispatcher.SiteEngineApplication;

public class PublishingQueuePolicyBaseTest extends TestCase {
    private PublishingQueuePolicyBase target;
    private SiteEngineApplication siteEngineApplication;
    private ModelFactory modelFactory;
    private ModelDomain domain;

    protected void setUp() throws Exception
    {
        target = spy(new PublishingQueuePolicyBase());
        siteEngineApplication = (SiteEngineApplication) mock(SiteEngineApplication.class);
        SiteEngine.setApplication(siteEngineApplication);
        modelFactory = (ModelFactory) mock(ModelFactory.class);

        when(siteEngineApplication.getModelFactory()).thenReturn(modelFactory);

        domain = (ModelDomain) mock(ModelDomain.class);
        when(siteEngineApplication.getModelDomain()).thenReturn(domain);

    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        SiteEngine.setApplication(null);
    }

    public void testGetContentList() throws CMException
    {
        ContentList contentList = (ContentList) mock(ContentList.class);
        doReturn(contentList).when(target).getSourceContentList();

        ContentListModel contentListModel = (ContentListModel) mock(ContentListModel.class);
        when(modelFactory.createModel(domain, contentList)).thenReturn(contentListModel);

        ContentList returnedContentList = target.getContentList();

        assertEquals(contentListModel, returnedContentList);
        
    }

}
