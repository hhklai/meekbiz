package utils;

import akka.actor.Cancellable;
import play.Logger;
import play.libs.Akka;
import play.libs.Time;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AkkaScheduleUtil {

    public static void scheduleAt(final Time.CronExpression firstTimeSchedule, final FiniteDuration repeatTime, final Runnable task) {
        Date nextValidTimeAfter = firstTimeSchedule.getNextValidTimeAfter(new Date());
        FiniteDuration d = Duration.create(
                nextValidTimeAfter.getTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);

        Cancellable cancellable = Akka.system().scheduler().scheduleOnce(d, new Runnable() {

            @Override
            public void run() {
                task.run();
                Cancellable cancellable = Akka.system().scheduler().schedule(
                        Duration.create(0, TimeUnit.MILLISECONDS),
                        repeatTime,
                        task,
                        Akka.system().dispatcher()
                );
                cancellableList.add(cancellable);
            }
        }, Akka.system().dispatcher());
        cancellableList.add(cancellable);
    }

    public static void stopAllSchedulers() {
        for (Cancellable cancellable :cancellableList) {
            if (!cancellable.isCancelled()) {
                cancellable.cancel();
            }
        }
    }

    private static List<Cancellable> cancellableList = new ArrayList<>();
}
