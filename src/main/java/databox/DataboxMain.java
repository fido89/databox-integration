package databox;

import databox.auth.AuthUser;
import databox.facebook.FacebookJob;
import databox.facebook.FacebookService;
import databox.twitter.TwitterJob;
import databox.twitter.TwitterService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

import static org.quartz.JobBuilder.newJob;

public class DataboxMain {

    public static void main(String[] args) throws Exception {
        Properties appProps = new Properties();
        appProps.load(DataboxMain.class.getClassLoader().getResourceAsStream("application.properties"));

        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        FacebookService.getInstance().init(appProps.getProperty("facebookClientId"), appProps.getProperty("facebookClientSecret"));
        TwitterService.getInstance().init(appProps.getProperty("twitterApiKey"), appProps.getProperty("twitterApiSecret"));

        AuthUser twitterUser = TwitterService.getInstance().authorize();
        AuthUser facebookUser = FacebookService.getInstance().authorize();

        JobDataMap facebookData = new JobDataMap();
        facebookData.put("authUser", facebookUser);
        facebookData.put("databoxApiKey", appProps.getProperty("facebookDataboxApiKey"));
        JobDetail facebookJob = newJob(FacebookJob.class)
                .withIdentity("facebookJob")
                .usingJobData(facebookData)
                .build();

        JobDataMap twitterData = new JobDataMap();
        twitterData.put("authUser", twitterUser);
        twitterData.put("databoxApiKey", appProps.getProperty("twitterDataboxApiKey"));
        JobDetail twitterJob = newJob(TwitterJob.class)
                .withIdentity("twitterJob")
                .usingJobData(twitterData)
                .build();

        CronTrigger facebookTrigger = TriggerBuilder.newTrigger()
                .withIdentity("facebookTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(appProps.getProperty("facebookTriggerInterval")))
                .forJob("facebookJob")
                .build();

        Trigger twitterTrigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(appProps.getProperty("twitterTriggerInterval")))
                .build();

        scheduler.scheduleJob(facebookJob, facebookTrigger);
        scheduler.scheduleJob(twitterJob, twitterTrigger);
        scheduler.start();
    }
}
