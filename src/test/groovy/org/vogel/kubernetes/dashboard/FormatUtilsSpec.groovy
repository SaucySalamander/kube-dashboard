package org.vogel.kubernetes.dashboard

import io.kubernetes.client.models.V1Endpoints
import io.kubernetes.client.models.V1LabelSelector
import org.joda.time.DateTime
import spock.lang.Specification

import static org.vogel.kubernetes.dashboard.FormatUtils.*

class FormatUtilsSpec extends Specification {
    def "test timestamp translation"(DateTime time, String result) {
        expect:
        translateTimestamp(time) == result

        where:
        time                           | result
        DateTime.now().minusYears(2)   | "2y"
        DateTime.now().minusDays(1)    | "1d"
        DateTime.now().minusHours(1)   | "1h"
        DateTime.now().minusMinutes(1) | "1m"
        DateTime.now().minusSeconds(1) | "1s"
    }

    def "test print multiline data"() {
        setup:
        def input = [app: 'application', foo: 'bar']

        expect:
        printMultiline(input) == ['app=application', 'foo=bar']
    }

    def "test print multiline null data"() {
        expect:
        printMultiline(null) == null
    }

    def "test print multiline no data"() {
        setup:
        def input = [:]

        expect:
        printMultiline(input) == null
    }

    def "test formatting an empty label selector"() {
        given:
        def labelSelector = Mock(V1LabelSelector)

        expect:
        formatLabelSelector(labelSelector) == ""
    }

    def "test formatting a null label selector"() {
        expect:
        formatLabelSelector(null) == ""
    }

//    def "test describing a backend"() {
//
//    }

//    def "test formatting an endpoint"() {
//        setup:
//        def endpoint = Mock(V1Endpoints)
//        def subsets = []
//        endpoint.subsets >> subsets
//
//        expect:
//        FormatUtils.formatEndpoints(endpoint, null) == "<none>"
//    }

    def "test formatting an endpoint with no endpoint"() {
        expect:
        formatEndpoints(null, null) == "<none>"
    }

    def "test formatting an endpoint with no subsets"() {
        setup:
        def endpoint = Mock(V1Endpoints)
        def subsets = []
        endpoint.subsets >> subsets

        expect:
        formatEndpoints(endpoint, null) == "<none>"
    }

    def "test access modes as a string"() {
        setup:
        def accessModes = ["ReadWriteOnce", "ReadOnlyMany", "ReadWriteMany", "ReadOnlyMany", "ReadWriteOnce"]

        expect:
        getAccessModesAsString(accessModes) == "RWO,ROX,RWX"
    }

    def "test access modes as a string RWO"() {
        setup:
        def accessModes = ["ReadWriteOnce"]

        expect:
        getAccessModesAsString(accessModes) == "RWO"
    }

    def "test access modes as a string ROX"() {
        setup:
        def accessModes = ["ReadOnlyMany"]

        expect:
        getAccessModesAsString(accessModes) == "ROX"
    }

    def "test access modes as a string RWX"() {
        setup:
        def accessModes = ["ReadWriteMany"]

        expect:
        getAccessModesAsString(accessModes) == "RWX"
    }

    def "test join a list into string with commas"() {
        setup:
        def list = ["foo", "bar", "baz"]

        expect:
        joinListWithCommas(list) == "foo,bar,baz"
    }

    def "test join a list into string with commas only one item"() {
        setup:
        def list = ["foo"]

        expect:
        joinListWithCommas(list) == "foo"
    }

    def "test join a list into string with commas empty list"() {
        setup:
        def list = []

        expect:
        joinListWithCommas(list) == ""
    }
}
