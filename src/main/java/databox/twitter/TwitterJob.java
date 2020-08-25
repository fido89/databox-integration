package databox.twitter;

import com.databox.sdk.Databox;
import com.databox.sdk.KPI;
import databox.auth.AuthUser;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;

public class TwitterJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        AuthUser user = (AuthUser) dataMap.get("authUser");
        Databox databox = new Databox(dataMap.getString("databoxApiKey"));

        try {
            List<KPI> kpis = new ArrayList<>();
            kpis.add(new KPI().setKey("followers").setValue(TwitterService.getInstance().getFollowersCount(user)));
            kpis.add(new KPI().setKey("following").setValue(TwitterService.getInstance().getFriends(user)));
            kpis.add(new KPI().setKey("tweets").setValue(TwitterService.getInstance().getTweets(user)));
            kpis.add(new KPI().setKey("likes").setValue(TwitterService.getInstance().getLikes(user)));
            databox.push(kpis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
