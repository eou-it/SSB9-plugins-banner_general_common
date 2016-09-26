if (System.properties['grails.test.phase'] == 'concurrent') {
    quartz {
        autoStartup = true
        waitForJobsToCompleteOnShutdown = true
        exposeSchedulerInRepository = true

        props {
            scheduler.skipUpdateCheck = true
            scheduler.instanceName = 'Banner Quartz Scheduler'
            scheduler.instanceId = 'AUTO'
            scheduler.idleWaitTime = 1000

            jobStore.class = 'net.hedtech.banner.general.scheduler.quartz.BannerDataSourceJobStoreCMT'
            jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.oracle.OracleDelegate'
            jobStore.tablePrefix = 'GCRQRTZ_'
//        jobStore.isClustered = false
//        jobStore.clusterCheckinInterval = 5000
//        jobStore.dataSource = 'jdbc/bannerDataSource'
            jobStore.useProperties = false

//        threadPool {
//            class = 'org.quartz.simpl.SimpleThreadPool'
//            threadCount = 10
//            threadPriority = 7
//        }

//        plugin {
//            shutdownhook {
//                'class' = 'org.quartz.plugins.management.ShutdownHookPlugin'
//                cleanShutdown = true
//            }
//        }
        }
    }
}