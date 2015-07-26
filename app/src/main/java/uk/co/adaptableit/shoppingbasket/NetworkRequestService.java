package uk.co.adaptableit.shoppingbasket;

import android.app.Application;

import com.octo.android.robospice.GoogleHttpClientSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.googlehttpclient.json.GsonObjectPersisterFactory;

public class NetworkRequestService extends GoogleHttpClientSpiceService {
    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new GsonObjectPersisterFactory(application, application.getCacheDir()));

        return cacheManager;
    }
}
