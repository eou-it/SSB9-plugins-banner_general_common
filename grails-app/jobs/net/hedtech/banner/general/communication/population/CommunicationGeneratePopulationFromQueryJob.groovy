package net.hedtech.banner.general.communication.population


class CommunicationGeneratePopulationFromQueryJob {
    static triggers = {
      simple repeatInterval: 5000l // execute job once in 5 seconds
    }

    def execute() {
        println "${new Date()} Hello World"
    }
}
