package com.bobpaulin.meetup

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON


class MeetupMemberDeduplicator {
    static void main(String... args) {

        def http = new HTTPBuilder()
        def resultSet = new HashSet<String>();
        //Enter your group ids
        def groups = []
        def key = 'Enter your meetup key here';
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
