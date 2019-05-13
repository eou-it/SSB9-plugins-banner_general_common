/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication

import net.hedtech.banner.general.communication.folder.CommunicationFolder
import org.apache.commons.lang.RandomStringUtils

/**
 * CommunicationManagmentTestingSupport.
 */
class CommunicationManagementTestingSupport  {

    def CommunicationFolder folder
    def i_valid_foldername


    CommunicationManagementTestingSupport() {
        setUp()
    }


    public void setUp() {
//        folder = newValidForCreateFolder()
//        folder.save(failOnError: true, flush: true)
//        //Test if the generated entity now has an id assigned
//        assert  folder.id
    }


    def static newValidForCreateFolder(def String folderName = null) {
        def fname
        if (!folderName) {
            fname = randomFolderName()
        }
        def folder = new CommunicationFolder(
                description: "Test Folder",
                internal: false,
                name: fname
        )
        return folder
    }


    def static newValidForCreateFolderWithSave(def String folderName = null) {
        def fname
        if (!folderName) {
            fname = randomFolderName()
        }
        def folder = new CommunicationFolder(
                description: "Test Folder",
                internal: false,
                name: fname
        )
        folder.save(failOnError: true, flush: true)
        return folder
    }


    def static String randomFolderName() {
        String charset = (('A'..'Z') + ('0'..'9')).join()
        Integer length = 30
        RandomStringUtils.random(length, charset.toCharArray())
    }

}
