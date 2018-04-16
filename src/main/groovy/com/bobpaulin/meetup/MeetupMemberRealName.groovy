package com.bobpaulin.meetup

@Grab(group='net.sf.json-lib', module='json-lib', version='2.3', classifier='jdk15')
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )
import groovyx.net.http.HTTPBuilder
import java.util.concurrent.CountDownLatch
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON


class MeetupMemberRealName {
    static void main(String... args) {
        
        if(args.length != 4)
        {
            println "This program prints the real name of group members either from thier"
            println "meetup name or from a custom profile question"
            println "Enter Program Arguments <groupName> <eventId> <questionNumbr> and <key>"
            println "groupName is the name of your meetup group (same as your meetup path)"
            println "eventId is the id of the event your trying to get from meetup"
            println "questionNumber is the id of the question number that includes the users real name"
            println "key is your meetup api key"
            println "EX"
            println "MeetupMemberRealName.groovy ChicagoJUG 234243 4444444 12423abcadfafqr23"
            return
        }

        def http = new HTTPBuilder()
        def groupName = "${args[0]}"
        def eventId = "${args[1]}"
        def idToNameMapping = new HashMap<String>()
        def attendeeNames = new ArrayList<String>()
        def requestCompleteLatch = new CountDownLatch(1)
        def questionNumber = "${args[2]}"
        def key = "${args[3]}"
        
        def url = "https://api.meetup.com/${groupName}/members?&sign=true&key=${key}&photo-host=public&page=200"
        while(url?.trim()){
                http.request(url, GET, JSON) { req ->
                    requestContentType = JSON;
                    headers.Accept = 'application/json';

                    // response handler for a successful response code:
                    response.success = { resp, json ->
                        // Check returned status'
                        
                        json.each{
                            def personId = it.id
                            it.group_profile.answers.each{
                                if("${it.question_id}".equals(questionNumber))
                                {
                                    if(it.answer != null && it.answer?.trim())
                                    {
                                        idToNameMapping.put(personId, it.answer);
                                    }
                                }
                            }
                            
                        }
                        def linkHeaderVal = resp.headers['Link'].value;
                        if(linkHeaderVal.endsWith("rel=\"next\""))
                        {
                            url = linkHeaderVal.substring(1,linkHeaderVal.indexOf('>'))
                        }
                        else
                        {
                            url = ""
                        }
                    }
                }
        }
        
        url = "https://api.meetup.com/${groupName}/events/${eventId}/rsvps?&sign=true&key=${key}&photo-host=public"
        http.request(url, GET, JSON) { req ->
            requestContentType = JSON;
            headers.Accept = 'application/json';

            // response handler for a successful response code:
            response.success = { resp, json ->
                // Check returned status'
                json.each{
                	def attendeeResponse = it.response
                	if(attendeeResponse == 'yes' || attendeeResponse == 'waitlist')
                	{
	                    def memberId = it.member.id
	                    def fullName = idToNameMapping.get(memberId)
	                    if(fullName)
	                    {
	                        attendeeNames.add(fullName)
	                    }
	                    else
	                    {
	                        attendeeNames.add(it.member.name)
	                    }
	                }
                }
                attendeeNames.each {  
                    println it
                }
            }
        }
        
        
    }
}
