/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.jobsub

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException

@Slf4j
class JobsubOutputCompositeService {
    //private static final log = Logger.getLogger(JobsubOutputCompositeService.class)
    def jobsubSavedOutputService
    def sessionFactory

    // GET /api/jobsub-pending-print/<id value>
    /*
     auth=`echo -n 'grails_user:u_pick_it' | base64`
     response=`curl -X GET -H "Accept: application/json" -H "Authorization: Basic $auth" -H "Content-Type: APPLICATION/OCTET-STREAM"
        -H "Content-Disposition: Attachment;Filename="saradms_6256.lis" "http://localhost:8080/BannerGeneralSsb/api/jobsub-pending-print/3"`
     echo "response:  $response"

   */
    def show(Map params) {
        def job
        log.debug "Begin show Jobsub Saved output  - Request parameters: ${params}"
        params = validateParams(params)

        def jobsub = JobsubSavedOutput.get(params.id)
        def jobsubOutput = fetchJobOutputFile(params.id)
        InputStream is = jobsubOutput.getBinaryStream();

        log.debug "Completed show Jobsub Saved output  - Request parameters: ${params} Job: ${jobsub.job} File name: ${jobsub.fileName} File Size: ${jobsubOutput.length()} bytes"

        return is
    }

    /***
     * formats:
     * get list for single printer:  GET /api/jobsub-pending-print?printer=xxx
     * get list for list of printers: GET /api/jobsub-pending-print?printer=xxx,yyyy
     * get all: GET /api/jobsub-pending-print?printer=%
     *
     * curl command:
     * auth=`echo -n 'grails_user:u_pick_it' | base64`
     * response=`curl -X GET -H "Accept: application/json" -H "Authorization: Basic $auth"  "http://localhost:8080/BannerGeneralSsb/api/jobsub-pending-print?printer=saas1"`
     * echo "response:  $response"
     *
     */
    def list(Map params) {
        log.debug "Begin Pending Print List - Request parameters: ${params}"
        def returnList = [] as List
        params = validateParams(params)

        if(params.printer) {
            returnList = JobsubExternalPrinter.fetchPendingPrintByPrinter(params.printer)
        }
        else {
            returnList = JobsubExternalPrinter.fetchPendingPrintByPrinter('')
        }
        log.debug "Completed Pending Print List - Request parameters: ${params} Total found: ${returnList?.size()}"
        return returnList
    }


    Long count(Map params) {
        int total = 0
        log.debug "Begin count list Jobsub Saved output  - Request parameters: ${params}"
        params = validateParams(params)
        total = JobsubExternalPrinter.fetchPendingPrintByPrinter(params.printer)?.size()
        log.debug "Pending print count:End: $total"
        return total
    }

    // put /api/jobsub-pending-print/<id>
    // @content:  map of update values
    /*
    curl -X PUT -H "Content-Type: application/json" -H "Accept: application/json" -H "Authorization: Basic bWhvY2tldHQ6dV9waWNrX2l0"
        -d '{"job":"SARADMS"}' "http://localhost:8080/BannerGeneralSsb/api/jobsubPendingPrint/8"
     */


    def update(def content, def params) {

        log.debug "Begin Jobsub Output Composite Service, Print date update   ${content} params ${params}"
        params = validateParams(params)

        def jobsub = JobsubSavedOutput.get(params.id)
        log.debug "Job ${jobsub?.job} to update Print date"
        if (!jobsub) {
            throw new ApplicationException(JobsubOutputCompositeService, "@@JobsubOutputCompositeService.invalidJob@@")
        }
        jobsub.printDate = new Date()
        log.debug "Job ${jobsub.job} id ${params.id} update print date with ${jobsub.printDate}"
        jobsubSavedOutputService.update([domainModel: jobsub])
        log.debug "End Jobsub Output Composite Service, Print date update ${content} params ${params}"
    }


    def validateParams(def params) {
        log.debug "Jobsub Output Composite Service, begin validate params ${params}"
        if (params.containsKey("pluralizedResourceName")) {
            if (params.pluralizedResourceName == "jobsub-pending-print") {
            } else {
                throw new ApplicationException(JobsubOutputCompositeService, new BusinessLogicValidationException("JobsubOutputCompositeService.pluralizedResourceName.unknown.BusinessLogicValidationException", [""]))
            }
        } else {
            throw new ApplicationException(JobsubOutputCompositeService, new BusinessLogicValidationException("JobsubOutputCompositeService.pluralizedResourceName.required.BusinessLogicValidationException", [""]))
        }

        if (params.containsKey("id")) {
            def validJob = JobsubExternalPrinter.get(params.id)
            if (!validJob) {
                throw new ApplicationException(JobsubOutputCompositeService, "@@JobsubOutputCompositeService.invalidPendingPrintJob@@")
            }
        }

        if (params.containsKey("printer")) {
            log.debug "Jobsub Output Composite Service, printer params: ${params.printer}"
            if (params.printer instanceof List) {
                log.debug "Printer is a list ${params.printer}"
            } else {
                if (params.printer =~ ",") {
                    def printers = buildArray(params.printer)
                    params.printer = printers
                }
            }
            log.debug "Printer: ${params.printer}"
        }
        log.debug "Jobsub Output Composite Service, validated params ${params}"
        return params

    }


    private static def buildArray(comp) {
        comp ? comp.split(",").findAll { !(it.trim() == "") }.collect { it.trim() } : []
    }


    def fetchJobOutputFile(def id) {

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def fileQuery = "select gjrjlis_file , gjrjlis_file_name  from gjrjlis where gjrjlis_surrogate_id = ?"
        def jobfile = sql.rows(fileQuery, [id])[0]

        java.sql.Blob pdfFileBlob = jobfile.gjrjlis_file

        return pdfFileBlob
    }

}
