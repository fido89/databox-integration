package databox.facebook;

import com.databox.sdk.Databox;
import com.databox.sdk.KPI;
import databox.auth.AuthUser;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;

public class FacebookJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        AuthUser user = (AuthUser) dataMap.get("authUser");
        String databoxApiKey = dataMap.getString("databoxApiKey");
        Databox databox = new Databox(databoxApiKey);

        try {
            List<KPI> kpis = new ArrayList<>();
            kpis.add(new KPI().setKey("friends").setValue(FacebookService.getInstance().getFriendsCount(user)));
            kpis.add(new KPI().setKey("pageLikes").setValue(FacebookService.getInstance().getPageLikes(user)));
            kpis.add(new KPI().setKey("photosUploaded").setValue(FacebookService.getInstance().getUploadedPhotos(user)));
            databox.push(kpis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
