package uk.co.adaptableit.shoppingbasket2;

import com.octo.android.robospice.request.SpiceRequest;

/**
 * Created by Andrew Clark on 16/07/2015.
 */
public class TestRequest extends SpiceRequest<Boolean> {
    public TestRequest() {
        super(Boolean.class);
    }

    @Override
    public Boolean loadDataFromNetwork() throws Exception {
        return null;
    }
}
