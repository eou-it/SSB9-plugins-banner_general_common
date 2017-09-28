/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.jobsub

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import org.apache.log4j.Logger

class JobsubOutputCompositeService {
    private static final log = Logger.getLogger(JobsubOutputCompositeService.class)
    def jobsubSavedOutputService
    def sessionFactory

    // GET /api/jobsubPendingPrint/<id value>
    def show(Map params) {
        def job
        validateParams(params)
        log.debug "Begin show Jobsub Saved output  - Request parameters: ${params}"
        //if (params.pluralizedResourceName == "jobsub-pending-print") {
        job = jobsubSavedOutputService.get(params.id)
        job.jobsubOutput = fetchJobOutputFile(params.id)

        return job
    }

    // GET /api/jobsubPendingPrint?printer=xxx
    def list(Map params) {
        log.debug "Begin Pending Print List - Request parameters: ${params}"
        def returnList = [] as List
        validateParams(params)

        returnList = JobsubExternalPrinter.fetchPendingPrintByPrinter(params.printer)

        return returnList
    }


    Long count(Map params) {
        int total = 0
        validateParams(params)
        total = JobsubExternalPrinter.fetchPendingPrintByPrinter(params.printer)?.size()
        log.debug "Pending print count:End: $total"
        return total
    }

    // put /api/jobsubPendingPrint/<id>
    // @content:  map of update values
    /*
    curl -X PUT -H "Content-Type: application/json" -H "Accept: application/json" -H "Authorization: Basic bWhvY2tldHQ6dV9waWNrX2l0"
        -d '{"job":"SARADMS"}' "http://localhost:8080/BannerGeneralSsb/api/jobsubPendingPrint/8"
     */


    def update(def content, def params) {
        validateParams(params)
        log.debug "Jobsub Output Composite Service, Print date update   ${content} params ${params}"
        def jobsub = JobsubSavedOutput.get(params.id)
        log.debug "Job ${jobsub?.job} to update Print date"
        if (!jobsub) {
            throw new ApplicationException(JobsubOutputCompositeService, "@@JobsubOutputCompositeService.invalidJob@@")
        }
        jobsub.printDate = new Date()
        log.debug "Job ${jobsub.job} id ${params.id} update print date with ${jobsub.printDate}"
        jobsubSavedOutputService.update([domainModel: jobsub])

    }


    def validateParams(def params) {
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
            if (params.printer == "%") {

            } else if (params.printer =~ ",") {
                def printers = buildArray(params.printer)
                params.printer = printers
            } else {
                if (params.printer) {

                } else {
                    throw new ApplicationException(JobsubOutputCompositeService, "@@JobsubOutputCompositeService.missingPrinter@@")
                }
            }
        }
        return params

    }

    private static def buildArray(comp) {
        comp ? comp.split(",").findAll {!(it.trim() == "")}.collect {it.trim()} : []
    }


    def fetchJobOutputFile(def id) {

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def fileQuery = "select gjrjlis_file , gjrjlis_file_name  from gjrjlis where gjrjlis_surrogate_id = ?"
        def jobfile = sql.rows(fileQuery, [id])[0]

        java.sql.Blob pdfFileBlob = jobfile.gjrjlis_file
        def outfilename = jobfile.gjrjlis_file_name
        File outfile = new File(outfilename)
        FileOutputStream pdf = null;
        InputStream is = null;
        pdf = new FileOutputStream(outfile);
        is = pdfFileBlob.getBinaryStream();
        int b = 0;
        while ((b = is.read()) != -1) {
            pdf.write(b);
        }

        return pdf

    }
}
