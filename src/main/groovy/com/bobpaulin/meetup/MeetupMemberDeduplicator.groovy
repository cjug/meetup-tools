package com.bobpaulin.meetup

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON


class MeetupMemberDeduplicator {
    static void main(String... args) {

        if(args.length != 2)
        {
            println "Enter Program Arguments <groups> and <key>"
            println "groups is a comma separated list with no spaces ex 123,4444"
            println "key is your meetup api key"
            println "EX"
            println "MeetupMemberDeduplicator.groovy 123,4444 12423abcadfafqr23"
            return
        }
        def http = new HTTPBuilder()
        def resultSet = new HashSet<String>();
        //Enter your group ids
        def groups = "${args[0]}".split(',')
    def key = "${args[1]}";
        groups.each{
            def url = "https://api.meetup.com/2/members?sign=true&key=${key}&group_id=${it}&photo-host=public&page=200"
            def total = '0'
            while(url?.trim()){
                http.request(url, GET, JSON) { req ->
                    requestContentType = JSON;
                    headers.Accept = 'application/json';

                    // response handler for a successful response code:
                    response.success = { resp, json ->
                        // Check returned status'
                        json['results'].each{
                            resultSet.add(it.id)
                        }
                        url = json['meta']['next']
                        total = json['meta']['total_count']
                    }
                }
            }
            println "total in group ${it} = ${total}"
        }
        println 'Unique Members: ' + resultSet.size()
    }
}
