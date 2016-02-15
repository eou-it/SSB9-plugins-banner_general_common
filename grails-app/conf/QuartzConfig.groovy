if (System.properties['grails.test.phase'] == 'concurrent') {
    quartz {
        autoStartup = true
        waitForJobsToCompleteOnShutdown = true
        exposeSchedulerInRepository = true

        props {
            scheduler.skipUpdateCheck = true
            scheduler.instanceName = 'communication_management'
            scheduler.instanceId = 'AUTO'
            scheduler.idleWaitTime = 1000

            jobStore.class = 'net.hedtech.banner.general.asynchronous.scheduling.BannerDataSourceJobStoreCMT'
            jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.oracle.OracleDelegate'
            jobStore.tablePrefix = 'GCRQRTZ_'
//        jobStore.isClustered = false
//        jobStore.clusterCheckinInterval = 5000
//        jobStore.dataSource = 'jdbc/bannerDataSource'
            jobStore.useProperties = false

//        dataSource.quartzDS.driver = 'oracle.jdbc.driver.OracleDriver'
//        dataSource.quartzDS.URL = 'jdbc:oracle:thin:@localhost:1521:ban83'
//        dataSource.quartzDS.user = 'baninst1'
//        dataSource.quartzDS.password = 'u_pick_it'
//        dataSource.quartzDS.maxConnections = 30
//        dataSource.quartzDS.validationQuery = 'select \'x\' from dual'

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

//environments {
//    test {
//        quartz {
//             {
//
//
//
//
//
//
//            }
//        }
//    }
//}