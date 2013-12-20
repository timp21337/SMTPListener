smtplistener
============

An email catcher.

Configuration
=============

in /etc/hosts add:

    127.0.0.1       smtplistener

Postfix
-------
in /etc/postfix/transport add:

    smtplistener smtp:localhost:1616

then:

    cd /etc/postfix
    postmap /etc/postfix/transport
    /etc/init.d/postfix restart


