smtplistener
============

An email catcher.

Note that this requires configuring your MTA, so is not trivial.
Exim in particular is very fussy.

DO NOT DEPLOY ON A MACHINE RESPONSIBLE FOR REAL MAIL.


Configuration
=============

in /etc/hosts add:

    127.0.0.1       smtplistener

Postfix
-------
in /etc/postfix/main.cf add:

    transport_maps = hash:/etc/postfix/transport

in /etc/postfix/transport add:

    smtplistener smtp:localhost:1616


then:

    cd /etc/postfix
    postmap /etc/postfix/transport
    /etc/init.d/postfix restart


Exim
----
in /etc/exim4/exim4.conf add in appropriate sections:

    domainlist relay_to_domains = smtplistener

    begin routers
      send_to_smtplistener:
        driver = manualroute
        route_list = smtplistener localhost
        transport = smtplistener_transport
        self = send

    begin transports
      smtplistener_transport:
        driver = smtp
        port = 1616


    begin retry
    # Address or Domain    Error       Retries
    # -----------------    -----       -------
    smtplistener           *
    *                      *           F,2h,15m; G,16h,1h,1.5; F,4d,6h

